package io.chaosmesh.mutator.transformer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * ASM MethodVisitor that mutates method return values.
 * Supports strategies: null, zero, false, empty, exception, custom value
 */
public class ReturnValueMutator extends MethodVisitor {
    private final String strategy;
    private final Object customValue;
    private final String methodDescriptor;

    public ReturnValueMutator(int api, MethodVisitor mv, String strategy, Object customValue, String methodDescriptor) {
        super(api, mv);
        this.strategy = strategy;
        this.customValue = customValue;
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public void visitInsn(int opcode) {
        // Intercept return instructions
        if (isReturnInstruction(opcode)) {
            Type returnType = Type.getReturnType(methodDescriptor);

            switch (strategy) {
                case "null":
                    mutateToNull(opcode, returnType);
                    return;
                case "zero":
                    mutateToZero(opcode, returnType);
                    return;
                case "false":
                    mutateToFalse(opcode, returnType);
                    return;
                case "empty":
                    mutateToEmpty(opcode, returnType);
                    return;
                case "exception":
                    mutateToException();
                    return;
                case "custom":
                    mutateToCustom(opcode, returnType);
                    return;
            }
        }

        super.visitInsn(opcode);
    }

    private boolean isReturnInstruction(int opcode) {
        return opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN ||
               opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN ||
               opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN;
    }

    private void mutateToNull(int opcode, Type returnType) {
        if (opcode == Opcodes.ARETURN) {
            mv.visitInsn(Opcodes.POP);  // Remove original return value
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
        } else {
            super.visitInsn(opcode);  // Can't return null for primitives
        }
    }

    private void mutateToZero(int opcode, Type returnType) {
        switch (opcode) {
            case Opcodes.IRETURN:
                mv.visitInsn(Opcodes.POP);
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitInsn(Opcodes.IRETURN);
                break;
            case Opcodes.LRETURN:
                mv.visitInsn(Opcodes.POP2);
                mv.visitInsn(Opcodes.LCONST_0);
                mv.visitInsn(Opcodes.LRETURN);
                break;
            case Opcodes.FRETURN:
                mv.visitInsn(Opcodes.POP);
                mv.visitInsn(Opcodes.FCONST_0);
                mv.visitInsn(Opcodes.FRETURN);
                break;
            case Opcodes.DRETURN:
                mv.visitInsn(Opcodes.POP2);
                mv.visitInsn(Opcodes.DCONST_0);
                mv.visitInsn(Opcodes.DRETURN);
                break;
            default:
                super.visitInsn(opcode);
        }
    }

    private void mutateToFalse(int opcode, Type returnType) {
        // Boolean is represented as int (I) in bytecode, sort is Type.INT (5)
        if (opcode == Opcodes.IRETURN) {
            mv.visitInsn(Opcodes.POP);
            mv.visitInsn(Opcodes.ICONST_0);  // false
            mv.visitInsn(Opcodes.IRETURN);
        } else {
            super.visitInsn(opcode);
        }
    }

    private void mutateToEmpty(int opcode, Type returnType) {
        if (opcode == Opcodes.ARETURN && returnType.getSort() == Type.OBJECT &&
            returnType.getInternalName().equals("java/lang/String")) {
            mv.visitInsn(Opcodes.POP);
            mv.visitLdcInsn("");
            mv.visitInsn(Opcodes.ARETURN);
        } else {
            super.visitInsn(opcode);
        }
    }

    private void mutateToException() {
        String exceptionClass = customValue != null ? customValue.toString() : "java.lang.RuntimeException";
        String exceptionMessage = "Mutated return value";

        mv.visitTypeInsn(Opcodes.NEW, exceptionClass.replace('.', '/'));
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(exceptionMessage);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
            exceptionClass.replace('.', '/'),
            "<init>",
            "(Ljava/lang/String;)V",
            false);
        mv.visitInsn(Opcodes.ATHROW);
    }

    private void mutateToCustom(int opcode, Type returnType) {
        if (customValue == null) {
            super.visitInsn(opcode);
            return;
        }

        switch (opcode) {
            case Opcodes.IRETURN:
                mv.visitInsn(Opcodes.POP);
                pushIntValue(customValue);
                mv.visitInsn(Opcodes.IRETURN);
                break;
            case Opcodes.ARETURN:
                mv.visitInsn(Opcodes.POP);
                if (customValue instanceof String) {
                    mv.visitLdcInsn(customValue);
                } else {
                    mv.visitInsn(Opcodes.ACONST_NULL);
                }
                mv.visitInsn(Opcodes.ARETURN);
                break;
            default:
                super.visitInsn(opcode);
        }
    }

    private void pushIntValue(Object value) {
        int intValue;
        if (value instanceof Number) {
            intValue = ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                intValue = Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                intValue = 0;
            }
        } else {
            intValue = 0;
        }

        if (intValue >= -1 && intValue <= 5) {
            mv.visitInsn(Opcodes.ICONST_0 + intValue);
        } else if (intValue >= Byte.MIN_VALUE && intValue <= Byte.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.BIPUSH, intValue);
        } else if (intValue >= Short.MIN_VALUE && intValue <= Short.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.SIPUSH, intValue);
        } else {
            mv.visitLdcInsn(intValue);
        }
    }
}
