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

        // Override with command-line arguments
        if (args.containsKey("port")) {
            config.controlServerPort = Integer.parseInt(args.get("port"));
        }
        if (args.containsKey("enabled")) {
            config.enabled = Boolean.parseBoolean(args.get("enabled"));
        }

        return config;
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
