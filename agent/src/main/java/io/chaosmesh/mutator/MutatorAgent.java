package io.chaosmesh.mutator;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.logging.Logger;

/**
 * Java agent entry point for runtime mutation.
 * Supports both premain (startup) and agentmain (dynamic attachment) modes.
 */
public class MutatorAgent {
    private static final Logger logger = Logger.getLogger(MutatorAgent.class.getName());
    private static MutationManager mutationManager;

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
        initialize(agentArgs, inst);
    }

    private static void initialize(String agentArgs, Instrumentation inst) {
        try {
            // Parse agent arguments
            MutationConfig config = MutationConfig.parse(agentArgs);
            logger.info("Loaded mutation config: " + config);

            // Initialize mutation manager
            mutationManager = new MutationManager(inst, config);

            // Register transformer first (critical)
            inst.addTransformer(mutationManager.getTransformer(), true);
            logger.info("Bytecode transformer registered successfully");

            // Start control server (optional - don't fail if it can't start)
            try {
                ControlServer controlServer = new ControlServer(mutationManager);
                controlServer.start();
                logger.info("Control server started on port " + controlServer.getPort());
            } catch (Exception e) {
                logger.warning("Failed to start control server: " + e.getMessage());
                logger.info("Continuing without control server - mutations will still work");
            }

            logger.info("MutatorAgent initialized successfully");

        } catch (Exception e) {
            logger.severe("Failed to initialize MutatorAgent: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static MutationManager getMutationManager() {
        return mutationManager;
    }
}
