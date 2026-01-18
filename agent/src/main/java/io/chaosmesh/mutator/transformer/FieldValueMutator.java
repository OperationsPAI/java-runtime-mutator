package io.chaosmesh.mutator.transformer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Mutates field access operations in bytecode
 */
public class FieldValueMutator extends MethodVisitor {
    private final String strategy;
    private final String targetField;

    public FieldValueMutator(int api, MethodVisitor mv, String strategy, String targetField) {
        super(api, mv);
        this.strategy = strategy;
        this.targetField = targetField;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        if (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC) {
            if (shouldMutate(name)) {
                mutateFieldRead(opcode, owner, name, descriptor);
                return;
            }
        }
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    private boolean shouldMutate(String fieldName) {
        return targetField == null || targetField.equals(fieldName);
    }

    private void mutateFieldRead(int opcode, String owner, String name, String descriptor) {
        super.visitFieldInsn(opcode, owner, name, descriptor);

        switch (strategy) {
            case "null":
                super.visitInsn(Opcodes.POP);
                super.visitInsn(Opcodes.ACONST_NULL);
                break;
            case "zero":
                super.visitInsn(Opcodes.POP);
                pushZeroValue(descriptor);
                break;
        }
    }

    private void pushZeroValue(String descriptor) {
        char type = descriptor.charAt(0);
        switch (type) {
            case 'Z':
            case 'B':
            case 'C':
            case 'S':
            case 'I':
                super.visitInsn(Opcodes.ICONST_0);
                break;
            case 'J':
                super.visitInsn(Opcodes.LCONST_0);
                break;
            case 'F':
                super.visitInsn(Opcodes.FCONST_0);
                break;
            case 'D':
                super.visitInsn(Opcodes.DCONST_0);
                break;
            case 'L':
            case '[':
                super.visitInsn(Opcodes.ACONST_NULL);
                break;
        }
    }
}
