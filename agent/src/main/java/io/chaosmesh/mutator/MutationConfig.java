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
                break;
            default:
                logger.warning("Unknown mutation action: " + action);
        }

        return rule;
    }

    private static MutationConfig loadFromFile(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(path), MutationConfig.class);
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
