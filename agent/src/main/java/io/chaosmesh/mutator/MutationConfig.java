package io.chaosmesh.mutator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Configuration for mutation rules and behavior.
 */
public class MutationConfig {
    private static final Logger logger = Logger.getLogger(MutationConfig.class.getName());
    private static final java.util.Random RANDOM = new java.util.Random();

    private List<MutationRule> mutations = new ArrayList<>();
    private int controlServerPort = 8080;
    private boolean enabled = true;

    public static class MutationRule {
        public String type;  // constant, operator, string, return
        public TargetInfo target;  // Single target (backward compatible)
        public List<TargetInfo> targets;  // Multiple targets
        public Map<String, Object> mutation;

        public static class TargetInfo {
            public String className;
            public String methodName;
            public String methodDescriptor;  // Optional: (I)I for specific overload

            public boolean matches(String className, String methodName) {
                return matchesPattern(this.className, className) &&
                       matchesPattern(this.methodName, methodName);
            }

            private boolean matchesPattern(String pattern, String value) {
                if (pattern == null || value == null) {
                    return false;
                }
                // Convert wildcard pattern to regex
                String regex = pattern
                    .replace(".", "\\.")
                    .replace("*", ".*")
                    .replace("?", ".");
                return value.matches(regex);
            }
        }

        public List<TargetInfo> getAllTargets() {
            List<TargetInfo> allTargets = new ArrayList<>();
            if (target != null) {
                allTargets.add(target);
            }
            if (targets != null) {
                allTargets.addAll(targets);
            }
            return allTargets;
        }
    }

    public static MutationConfig parse(String agentArgs) throws IOException {
        MutationConfig config = new MutationConfig();

        if (agentArgs == null || agentArgs.isEmpty()) {
            logger.info("No agent arguments provided, using default config");
            return config;
        }

        // Parse arguments: key1=value1,key2=value2
        Map<String, String> args = new HashMap<>();
        for (String arg : agentArgs.split(",")) {
            String[] parts = arg.split("=", 2);
            if (parts.length == 2) {
                args.put(parts[0].trim(), parts[1].trim());
            }
        }

        // Load config file if specified
        if (args.containsKey("config")) {
            String configPath = args.get("config");
            config = loadFromFile(configPath);
            logger.info("Loaded config from file: " + configPath);
        }

        // Handle runtime mutation arguments from chaos-daemon
        // Format: mutator_action=constant,mutator_class=...,mutator_method=...,mutator_from=...,mutator_to=...
        if (args.containsKey("mutator_action")) {
            MutationRule rule = createRuleFromArgs(args);
            if (rule != null) {
                config.mutations.add(rule);
                logger.info("Created mutation rule from args: type=" + rule.type +
                    ", class=" + (rule.target != null ? rule.target.className : "null") +
                    ", method=" + (rule.target != null ? rule.target.methodName : "null"));
            }
        }

        // Override with command-line arguments
        if (args.containsKey("port")) {
            config.controlServerPort = Integer.parseInt(args.get("port"));
        }
        if (args.containsKey("enabled")) {
            config.enabled = Boolean.parseBoolean(args.get("enabled"));
        }

        return config;
    }

    /**
     * Creates a MutationRule from chaos-daemon style arguments.
     * Supports: mutator_action, mutator_class, mutator_method, mutator_from, mutator_to, mutator_strategy
     */
    private static MutationRule createRuleFromArgs(Map<String, String> args) {
        String action = args.get("mutator_action");
        String className = args.get("mutator_class");
        String methodName = args.get("mutator_method");

        if (action == null || className == null || methodName == null) {
            logger.warning("Missing required arguments: mutator_action, mutator_class, mutator_method");
            return null;
        }

        MutationRule rule = new MutationRule();
        rule.type = action;

        // Create target
        MutationRule.TargetInfo target = new MutationRule.TargetInfo();
        target.className = className;
        target.methodName = methodName;
        if (args.containsKey("mutator_signature")) {
            target.methodDescriptor = args.get("mutator_signature");
        }
        rule.target = target;

        // Create mutation config
        rule.mutation = new HashMap<>();

        switch (action) {
            case "constant":
                // Constant mutation requires from/to values
                if (args.containsKey("mutator_from")) {
                    rule.mutation.put("from", args.get("mutator_from"));
                }
                if (args.containsKey("mutator_to")) {
                    rule.mutation.put("to", args.get("mutator_to"));
                }
                break;
            case "operator":
            case "string":
                // Operator and string mutations use strategy
                if (args.containsKey("mutator_strategy")) {
                    rule.mutation.put("strategy", args.get("mutator_strategy"));
                }
                break;
            case "return":
                // Return mutation uses strategy and optional value
                if (args.containsKey("mutator_strategy")) {
                    rule.mutation.put("strategy", args.get("mutator_strategy"));
                }
                if (args.containsKey("mutator_value")) {
                    rule.mutation.put("value", args.get("mutator_value"));
                }
                // Pre-generate random value if strategy is random
                if ("random".equals(args.get("mutator_strategy"))) {
                    String returnTypeHint = args.get("mutator_return_type");
                    Object randomValue = generateRandomValue(returnTypeHint);
                    rule.mutation.put("randomValue", randomValue);
                    logger.info("Generated random value for " + className + "." + methodName + ": " + randomValue +
                        " (type: " + (randomValue != null ? randomValue.getClass().getSimpleName() : "null") + ")");
                }
                break;
            default:
                logger.warning("Unknown mutation action: " + action);
        }

        return rule;
    }

    private static MutationConfig loadFromFile(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        MutationConfig config = mapper.readValue(new File(path), MutationConfig.class);
        // Pre-generate random values for random strategy mutations
        resolveRandomValues(config);
        return config;
    }

    /**
     * Pre-generates random values for mutations with strategy=random.
     * This ensures the random value is determined at config load time and logged,
     * making it traceable and reproducible for debugging.
     */
    private static void resolveRandomValues(MutationConfig config) {
        for (MutationRule rule : config.mutations) {
            if ("return".equals(rule.type) && rule.mutation != null) {
                String strategy = (String) rule.mutation.get("strategy");
                if ("random".equals(strategy)) {
                    // Generate random value based on return type hint or use generic approach
                    String returnTypeHint = (String) rule.mutation.get("returnType");
                    Object randomValue = generateRandomValue(returnTypeHint);
                    rule.mutation.put("randomValue", randomValue);

                    // Log the generated value for traceability
                    String targetInfo = rule.target != null
                        ? rule.target.className + "." + rule.target.methodName
                        : (rule.targets != null && !rule.targets.isEmpty()
                            ? rule.targets.get(0).className + "." + rule.targets.get(0).methodName + " (+" + (rule.targets.size() - 1) + " more)"
                            : "unknown");
                    logger.info("Generated random value for " + targetInfo + ": " + randomValue +
                        " (type: " + (randomValue != null ? randomValue.getClass().getSimpleName() : "null") + ")");
                }
            }
        }
    }

    /**
     * Generates a random value based on the return type hint.
     * If no hint is provided, generates values that can be used for multiple types.
     */
    private static Object generateRandomValue(String returnTypeHint) {
        if (returnTypeHint == null) {
            // Default: generate a map with all possible random values
            // The mutator will pick the appropriate one based on actual return type
            Map<String, Object> values = new HashMap<>();
            values.put("int", RANDOM.nextInt());
            values.put("long", RANDOM.nextLong());
            values.put("float", RANDOM.nextFloat());
            values.put("double", RANDOM.nextDouble());
            values.put("string", java.util.UUID.randomUUID().toString());
            return values;
        }

        switch (returnTypeHint.toLowerCase()) {
            case "int":
            case "integer":
            case "short":
            case "byte":
            case "char":
                return RANDOM.nextInt();
            case "long":
                return RANDOM.nextLong();
            case "float":
                return RANDOM.nextFloat();
            case "double":
                return RANDOM.nextDouble();
            case "string":
                return java.util.UUID.randomUUID().toString();
            case "boolean":
                return null; // Boolean uses XOR negation, no pre-generated value needed
            default:
                return null; // Objects return null
        }
    }

    // Getters
    public List<MutationRule> getMutations() {
        return mutations;
    }

    public int getControlServerPort() {
        return controlServerPort;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "MutationConfig{" +
                "mutations=" + mutations.size() +
                ", port=" + controlServerPort +
                ", enabled=" + enabled +
                '}';
    }
}
