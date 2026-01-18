package io.chaosmesh.mutator.transformer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Mutates array and collection access operations in bytecode
 */
public class ArrayCollectionMutator extends MethodVisitor {
    private final String strategy;

    public ArrayCollectionMutator(int api, MethodVisitor mv, String strategy) {
        super(api, mv);
        this.strategy = strategy;
    }

    @Override
    public void visitInsn(int opcode) {
        if (strategy.equals("empty") && isArrayLoad(opcode)) {
            super.visitInsn(Opcodes.POP);
            super.visitInsn(Opcodes.POP);
            pushDefaultValue(opcode);
            return;
        }
        super.visitInsn(opcode);
    }

    private boolean isArrayLoad(int opcode) {
        return opcode == Opcodes.IALOAD || opcode == Opcodes.LALOAD ||
               opcode == Opcodes.FALOAD || opcode == Opcodes.DALOAD ||
               opcode == Opcodes.AALOAD || opcode == Opcodes.BALOAD ||
               opcode == Opcodes.CALOAD || opcode == Opcodes.SALOAD;
    }

    private void pushDefaultValue(int opcode) {
        switch (opcode) {
            case Opcodes.IALOAD:
            case Opcodes.BALOAD:
            case Opcodes.CALOAD:
            case Opcodes.SALOAD:
                super.visitInsn(Opcodes.ICONST_0);
                break;
            case Opcodes.LALOAD:
                super.visitInsn(Opcodes.LCONST_0);
                break;
            case Opcodes.FALOAD:
                super.visitInsn(Opcodes.FCONST_0);
                break;
            case Opcodes.DALOAD:
                super.visitInsn(Opcodes.DCONST_0);
                break;
            case Opcodes.AALOAD:
                super.visitInsn(Opcodes.ACONST_NULL);
                break;
        }
    }
}
