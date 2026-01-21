package io.chaosmesh.mutator;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Java agent entry point for runtime mutation.
 * Supports both premain (startup) and agentmain (dynamic attachment) modes.
 *
 * Supports re-attachment with "enabled=false" to disable mutations without restart.
 */
public class MutatorAgent {
    private static final Logger logger = Logger.getLogger(MutatorAgent.class.getName());
    private static MutationManager mutationManager;
    private static Instrumentation globalInstrumentation;
    private static boolean initialized = false;

    /**
     * Premain entry point - called during JVM startup with -javaagent flag
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        logger.info("MutatorAgent starting in premain mode");
        initialize(agentArgs, inst);
    }

    /**
     * Agentmain entry point - called when agent is dynamically attached
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        logger.info("MutatorAgent starting in agentmain mode");

        // Check if this is a re-attachment for control purposes
        if (initialized && mutationManager != null) {
            handleReattachment(agentArgs);
        } else {
            initialize(agentArgs, inst);
        }
    }

    /**
     * Handle re-attachment to update mutation state.
     * This allows controlling the agent without needing curl or other tools.
     */
    private static void handleReattachment(String agentArgs) {
        logger.info("Agent already initialized, handling re-attachment with args: " + agentArgs);

        try {
            // Parse the arguments to check for control commands
            java.util.Map<String, String> args = parseArgs(agentArgs);

            // Handle enabled flag
            if (args.containsKey("enabled")) {
                boolean enabled = Boolean.parseBoolean(args.get("enabled"));
                mutationManager.setEnabled(enabled);
                logger.info("Re-attachment: mutations " + (enabled ? "enabled" : "disabled"));
            }

            // Handle clear command - clear all mutations
            if (args.containsKey("clear") && Boolean.parseBoolean(args.get("clear"))) {
                mutationManager.clearAllMutations();
                logger.info("Re-attachment: all mutations cleared");
            }

            // Handle new mutation rules if provided
            if (args.containsKey("mutator_action")) {
                MutationConfig config = MutationConfig.parse(agentArgs);
                for (MutationConfig.MutationRule rule : config.getMutations()) {
                    mutationManager.addMutation(rule);
                    logger.info("Re-attachment: added new mutation rule");
                }
            }

        } catch (Exception e) {
            logger.warning("Failed to handle re-attachment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static java.util.Map<String, String> parseArgs(String agentArgs) {
        java.util.Map<String, String> args = new java.util.HashMap<>();
        if (agentArgs == null || agentArgs.isEmpty()) {
            return args;
        }
        for (String arg : agentArgs.split(",")) {
            String[] parts = arg.split("=", 2);
            if (parts.length == 2) {
                args.put(parts[0].trim(), parts[1].trim());
            }
        }
        return args;
    }

    private static void initialize(String agentArgs, Instrumentation inst) {
        try {
            globalInstrumentation = inst;

            // Parse agent arguments
            MutationConfig config = MutationConfig.parse(agentArgs);
            logger.info("Loaded mutation config: " + config);

            // Initialize mutation manager
            mutationManager = new MutationManager(inst, config);

            // Register transformer first (critical) - must support retransformation
            inst.addTransformer(mutationManager.getTransformer(), true);
            logger.info("Bytecode transformer registered successfully");

            // Retransform already-loaded classes that match mutation rules
            retransformTargetClasses(inst, config);

            // Start control server (optional - don't fail if it can't start)
            try {
                ControlServer controlServer = new ControlServer(mutationManager);
                controlServer.start();
                logger.info("Control server started on port " + controlServer.getPort());
            } catch (Exception e) {
                logger.warning("Failed to start control server: " + e.getMessage());
                logger.info("Continuing without control server - mutations will still work");
            }

            initialized = true;
            logger.info("MutatorAgent initialized successfully");

        } catch (Exception e) {
            logger.severe("Failed to initialize MutatorAgent: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retransform already-loaded classes that match mutation rules.
     * This is critical for dynamic attachment (agentmain) mode where
     * target classes may already be loaded.
     */
    private static void retransformTargetClasses(Instrumentation inst, MutationConfig config) {
        Set<String> targetClassNames = new java.util.HashSet<>();

        // Collect all target class names from mutation rules
        for (MutationConfig.MutationRule rule : config.getMutations()) {
            for (MutationConfig.MutationRule.TargetInfo target : rule.getAllTargets()) {
                if (target.className != null && !target.className.contains("*") && !target.className.contains("?")) {
                    targetClassNames.add(target.className);
                }
            }
        }

        if (targetClassNames.isEmpty()) {
            logger.info("No specific target classes to retransform");
            return;
        }

        logger.info("Looking for " + targetClassNames.size() + " target class(es) to retransform");

        // Find and retransform matching loaded classes
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            String className = clazz.getName();
            if (targetClassNames.contains(className)) {
                if (inst.isModifiableClass(clazz)) {
                    try {
                        logger.info("Retransforming already-loaded class: " + className);
                        inst.retransformClasses(clazz);
                        logger.info("Successfully retransformed: " + className);
                    } catch (UnmodifiableClassException e) {
                        logger.warning("Failed to retransform class " + className + ": " + e.getMessage());
                    }
                } else {
                    logger.warning("Class " + className + " is not modifiable");
                }
            }
        }
    }

    public static MutationManager getMutationManager() {
        return mutationManager;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
