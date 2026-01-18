package io.chaosmesh.mutator.transformer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Injects null values into bytecode
 */
public class NullPointerMutator extends MethodVisitor {
    private final String strategy;

    public NullPointerMutator(int api, MethodVisitor mv, String strategy) {
        super(api, mv);
        this.strategy = strategy;
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        if (strategy.equals("null_locals") && isObjectLoad(opcode)) {
            super.visitInsn(Opcodes.ACONST_NULL);
            return;
        }
        super.visitVarInsn(opcode, var);
    }

    private boolean isObjectLoad(int opcode) {
        return opcode == Opcodes.ALOAD;
    }
}
