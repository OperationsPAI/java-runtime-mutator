package io.chaosmesh.mutator;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages mutation state and coordinates bytecode transformation.
 */
public class MutationManager {
    private static final Logger logger = Logger.getLogger(MutationManager.class.getName());

    private final Instrumentation instrumentation;
    private final MutationConfig config;
    private final MutatorTransformer transformer;
    private final List<MutationState> mutationStates = new ArrayList<>();
    private final Map<String, MutationState> exactMatchCache = new ConcurrentHashMap<>();
    private volatile boolean enabled;

    public MutationManager(Instrumentation instrumentation, MutationConfig config) {
        this.instrumentation = instrumentation;
        this.config = config;
        this.enabled = config.isEnabled();
        this.transformer = new MutatorTransformer(this);

        // Initialize mutation states
        for (MutationConfig.MutationRule rule : config.getMutations()) {
            mutationStates.add(new MutationState(rule));
        }
    }

    public ClassFileTransformer getTransformer() {
        return transformer;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        logger.info("Mutations " + (enabled ? "enabled" : "disabled"));
    }

    public MutationConfig getConfig() {
        return config;
    }

    public List<MutationState> getMutationStates(String className, String methodName) {
        // Check cache first for exact matches
        String key = className + "." + methodName;
        MutationState cached = exactMatchCache.get(key);
        if (cached != null) {
            return Collections.singletonList(cached);
        }

        // Find all matching mutation states
        List<MutationState> matches = new ArrayList<>();
        for (MutationState state : mutationStates) {
            if (state.matches(className, methodName)) {
                matches.add(state);
            }
        }

        // Cache exact matches for performance
        if (matches.size() == 1 && !hasWildcard(matches.get(0).getRule())) {
            exactMatchCache.put(key, matches.get(0));
        }

        return matches;
    }

    private boolean hasWildcard(MutationConfig.MutationRule rule) {
        for (MutationConfig.MutationRule.TargetInfo target : rule.getAllTargets()) {
            if (target.className != null && (target.className.contains("*") || target.className.contains("?"))) {
                return true;
            }
            if (target.methodName != null && (target.methodName.contains("*") || target.methodName.contains("?"))) {
                return true;
            }
        }
        return false;
    }

    public List<MutationState> getAllMutationStates() {
        return new ArrayList<>(mutationStates);
    }

    public void addMutation(MutationConfig.MutationRule rule) {
        mutationStates.add(new MutationState(rule));
        exactMatchCache.clear();
        logger.info("Added mutation rule with " + rule.getAllTargets().size() + " target(s)");

        // Retransform affected classes
        for (MutationConfig.MutationRule.TargetInfo target : rule.getAllTargets()) {
            if (!hasWildcard(rule)) {
                retransformClass(target.className);
            }
        }
    }

    public void removeMutation(String className, String methodName) {
        mutationStates.removeIf(state -> state.matches(className, methodName));
        exactMatchCache.clear();
        logger.info("Removed mutation: " + className + "." + methodName);

        // Retransform affected classes
        retransformClass(className);
    }

    /**
     * Clear all mutations and retransform affected classes.
     * This is used for recovery - disabling mutations completely.
     */
    public void clearAllMutations() {
        // Collect affected class names before clearing
        Set<String> affectedClasses = new java.util.HashSet<>();
        for (MutationState state : mutationStates) {
            for (MutationConfig.MutationRule.TargetInfo target : state.getRule().getAllTargets()) {
                if (target.className != null && !target.className.contains("*") && !target.className.contains("?")) {
                    affectedClasses.add(target.className);
                }
            }
        }

        // Clear all mutations
        mutationStates.clear();
        exactMatchCache.clear();
        logger.info("Cleared all mutations");

        // Retransform affected classes to restore original bytecode
        for (String className : affectedClasses) {
            retransformClass(className);
        }
    }

    private void retransformClass(String className) {
        try {
            String internalName = className.replace('.', '/');
            for (Class<?> clazz : instrumentation.getAllLoadedClasses()) {
                if (clazz.getName().equals(className)) {
                    if (instrumentation.isModifiableClass(clazz)) {
                        instrumentation.retransformClasses(clazz);
                        logger.info("Retransformed class: " + className);
                    }
                    break;
                }
            }
        } catch (UnmodifiableClassException e) {
            logger.warning("Cannot retransform class " + className + ": " + e.getMessage());
        }
    }

    /**
     * Represents the state of a single mutation rule.
     */
    public static class MutationState {
        private final MutationConfig.MutationRule rule;
        private volatile boolean active = true;

        public MutationState(MutationConfig.MutationRule rule) {
            this.rule = rule;
        }

        public MutationConfig.MutationRule getRule() {
            return rule;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean matches(String className, String methodName) {
            for (MutationConfig.MutationRule.TargetInfo target : rule.getAllTargets()) {
                if (target.matches(className, methodName)) {
                    return true;
                }
            }
            return false;
        }
    }
}
