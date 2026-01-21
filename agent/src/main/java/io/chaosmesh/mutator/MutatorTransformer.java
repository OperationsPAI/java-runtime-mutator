package io.chaosmesh.mutator;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.logging.Logger;

/**
 * Bytecode transformer that applies mutations to target classes.
 */
public class MutatorTransformer implements ClassFileTransformer {
    private static final Logger logger = Logger.getLogger(MutatorTransformer.class.getName());

    private final MutationManager mutationManager;

    public MutatorTransformer(MutationManager mutationManager) {
        this.mutationManager = mutationManager;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                           ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!mutationManager.isEnabled()) {
            return null;
        }

        // Convert internal class name to standard format
        String standardClassName = className.replace('/', '.');

        // Check if this class has any mutations by checking if any target's className pattern matches
        boolean hasMutations = mutationManager.getAllMutationStates().stream()
                .anyMatch(state -> {
                    for (MutationConfig.MutationRule.TargetInfo target : state.getRule().getAllTargets()) {
                        if (target.className != null) {
                            String regex = target.className
                                .replace(".", "\\.")
                                .replace("*", ".*")
                                .replace("?", ".");
                            if (standardClassName.matches(regex)) {
                                return true;
                            }
                        }
                    }
                    return false;
                });

        if (!hasMutations) {
            return null;
        }

        try {
            logger.info("Transforming class: " + standardClassName + " (has mutations for this class)");

            // Use ASM to transform the bytecode
            org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(classfileBuffer);
            org.objectweb.asm.ClassWriter writer = new org.objectweb.asm.ClassWriter(reader, org.objectweb.asm.ClassWriter.COMPUTE_FRAMES);

            MutationClassVisitor visitor = new MutationClassVisitor(Opcodes.ASM9, writer, mutationManager, standardClassName);
            reader.accept(visitor, 0);

            logger.info("Transformation complete for class: " + standardClassName);
            return writer.toByteArray();

        } catch (Exception e) {
            logger.warning("Failed to transform class " + standardClassName + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * ASM ClassVisitor for applying mutations.
     */
    private static class MutationClassVisitor extends ClassVisitor {
        private final MutationManager mutationManager;
        private final String className;

        public MutationClassVisitor(int api, ClassVisitor cv, MutationManager mutationManager, String className) {
            super(api, cv);
            this.mutationManager = mutationManager;
            this.className = className;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                        String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            // Check if this method has mutations
            List<MutationManager.MutationState> states = mutationManager.getMutationStates(className, name);

            logger.info("Checking method: " + className + "." + name + " - found " + states.size() + " mutation(s)");

            // Apply all matching mutations (chain them)
            for (MutationManager.MutationState state : states) {
                if (state.isActive()) {
                    logger.info("Applying mutation to method: " + className + "." + name);

                    MutationConfig.MutationRule rule = state.getRule();
                    logger.info("Mutation type: " + rule.type + ", mutation config: " + rule.mutation);

                    // Apply appropriate mutator based on type
                    if ("constant".equals(rule.type) || "string".equals(rule.type)) {
                        String mutationType = (String) rule.mutation.getOrDefault("strategy", "zero");
                        Object from = rule.mutation.get("from");
                        Object to = rule.mutation.get("to");
                        mv = new io.chaosmesh.mutator.transformer.ConstantMutator(Opcodes.ASM9, mv, mutationType, from, to);
                    } else if ("operator".equals(rule.type)) {
                        String mutationType = (String) rule.mutation.getOrDefault("strategy", "add_to_sub");
                        logger.info("Operator mutation type: " + mutationType);
                        mv = new io.chaosmesh.mutator.transformer.OperatorMutator(Opcodes.ASM9, mv, mutationType);
                    } else if ("return".equals(rule.type)) {
                        String mutationType = (String) rule.mutation.getOrDefault("strategy", "null");
                        Object customValue = rule.mutation.get("value");
                        mv = new io.chaosmesh.mutator.transformer.ReturnValueMutator(Opcodes.ASM9, mv, mutationType, customValue, descriptor);
                    } else if ("method_call".equals(rule.type)) {
                        String strategy = (String) rule.mutation.getOrDefault("strategy", "skip");
                        String targetMethod = (String) rule.mutation.get("target_method");
                        mv = new io.chaosmesh.mutator.transformer.MethodCallMutator(Opcodes.ASM9, mv, strategy, targetMethod);
                    } else if ("condition".equals(rule.type)) {
                        String strategy = (String) rule.mutation.getOrDefault("strategy", "negate");
                        mv = new io.chaosmesh.mutator.transformer.ConditionMutator(Opcodes.ASM9, mv, strategy);
                    } else if ("field_value".equals(rule.type)) {
                        String strategy = (String) rule.mutation.getOrDefault("strategy", "null");
                        String targetField = (String) rule.mutation.get("target_field");
                        mv = new io.chaosmesh.mutator.transformer.FieldValueMutator(Opcodes.ASM9, mv, strategy, targetField);
                    } else if ("timing".equals(rule.type)) {
                        Long delayMs = ((Number) rule.mutation.getOrDefault("delay_ms", 1000L)).longValue();
                        mv = new io.chaosmesh.mutator.transformer.TimingMutator(Opcodes.ASM9, mv, delayMs);
                    } else if ("exception".equals(rule.type)) {
                        String exceptionClass = (String) rule.mutation.getOrDefault("exception_class", "java.lang.RuntimeException");
                        String message = (String) rule.mutation.get("message");
                        mv = new io.chaosmesh.mutator.transformer.ExceptionMutator(Opcodes.ASM9, mv, exceptionClass, message);
                    } else if ("loop".equals(rule.type)) {
                        String strategy = (String) rule.mutation.getOrDefault("strategy", "skip_first");
                        mv = new io.chaosmesh.mutator.transformer.LoopMutator(Opcodes.ASM9, mv, strategy);
                    } else if ("concurrency".equals(rule.type)) {
                        String strategy = (String) rule.mutation.getOrDefault("strategy", "remove_sync");
                        mv = new io.chaosmesh.mutator.transformer.ConcurrencyMutator(Opcodes.ASM9, mv, strategy);
                    } else if ("resource_leak".equals(rule.type)) {
                        String strategy = (String) rule.mutation.getOrDefault("strategy", "skip_close");
                        mv = new io.chaosmesh.mutator.transformer.ResourceLeakMutator(Opcodes.ASM9, mv, strategy);
                    } else if ("array_collection".equals(rule.type)) {
                        String strategy = (String) rule.mutation.getOrDefault("strategy", "empty");
                        mv = new io.chaosmesh.mutator.transformer.ArrayCollectionMutator(Opcodes.ASM9, mv, strategy);
                    } else if ("null_pointer".equals(rule.type)) {
                        String strategy = (String) rule.mutation.getOrDefault("strategy", "null_locals");
                        mv = new io.chaosmesh.mutator.transformer.NullPointerMutator(Opcodes.ASM9, mv, strategy);
                    } else if ("logging".equals(rule.type)) {
                        String strategy = (String) rule.mutation.getOrDefault("strategy", "suppress");
                        mv = new io.chaosmesh.mutator.transformer.LoggingMutator(Opcodes.ASM9, mv, strategy);
                    }
                }
            }

            return mv;
        }
    }
}
