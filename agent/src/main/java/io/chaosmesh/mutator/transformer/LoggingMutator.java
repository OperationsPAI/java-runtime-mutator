package io.chaosmesh.mutator.transformer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Suppresses logging calls in bytecode
 */
public class LoggingMutator extends MethodVisitor {
    private final String strategy;

    public LoggingMutator(int api, MethodVisitor mv, String strategy) {
        super(api, mv);
        this.strategy = strategy;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (strategy.equals("suppress") && isLoggingCall(owner, name)) {
            return;
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    private boolean isLoggingCall(String owner, String methodName) {
        return (owner.contains("Logger") || owner.contains("Log")) &&
               (methodName.equals("debug") || methodName.equals("info") ||
                methodName.equals("warn") || methodName.equals("error") ||
                methodName.equals("trace") || methodName.equals("log"));
    }
}
