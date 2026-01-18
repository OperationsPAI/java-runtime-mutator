package io.chaosmesh.mutator.transformer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Skips resource cleanup calls in bytecode
 */
public class ResourceLeakMutator extends MethodVisitor {
    private final String strategy;

    public ResourceLeakMutator(int api, MethodVisitor mv, String strategy) {
        super(api, mv);
        this.strategy = strategy;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (strategy.equals("skip_close") && isCloseMethod(name)) {
            return;
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    private boolean isCloseMethod(String methodName) {
        return "close".equals(methodName) ||
               "dispose".equals(methodName) ||
               "release".equals(methodName) ||
               "shutdown".equals(methodName);
    }
}
