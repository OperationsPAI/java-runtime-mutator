package io.chaosmesh.mutator.transformer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Mutates method calls in bytecode
 */
public class MethodCallMutator extends MethodVisitor {
    private final String strategy;
    private final String targetMethod;

    public MethodCallMutator(int api, MethodVisitor mv, String strategy, String targetMethod) {
        super(api, mv);
        this.strategy = strategy;
        this.targetMethod = targetMethod;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (shouldMutate(name)) {
            switch (strategy) {
                case "skip":
                    skipMethodCall(opcode, descriptor);
                    return;
                case "null_return":
                    skipMethodCall(opcode, descriptor);
                    pushDefaultReturnValue(descriptor);
                    return;
                default:
                    break;
            }
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    private boolean shouldMutate(String methodName) {
        return targetMethod == null || targetMethod.equals(methodName);
    }

    private void skipMethodCall(int opcode, String descriptor) {
        Type[] args = Type.getArgumentTypes(descriptor);

        // Pop all arguments from stack
        for (int i = args.length - 1; i >= 0; i--) {
            Type arg = args[i];
            if (arg.getSize() == 2) {
                super.visitInsn(Opcodes.POP2);
            } else {
                super.visitInsn(Opcodes.POP);
            }
        }

        // Pop 'this' reference for non-static calls
        if (opcode != Opcodes.INVOKESTATIC) {
            super.visitInsn(Opcodes.POP);
        }
    }

    private void pushDefaultReturnValue(String descriptor) {
        Type returnType = Type.getReturnType(descriptor);

        switch (returnType.getSort()) {
            case Type.VOID:
                break;
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                super.visitInsn(Opcodes.ICONST_0);
                break;
            case Type.FLOAT:
                super.visitInsn(Opcodes.FCONST_0);
                break;
            case Type.LONG:
                super.visitInsn(Opcodes.LCONST_0);
                break;
            case Type.DOUBLE:
                super.visitInsn(Opcodes.DCONST_0);
                break;
            case Type.ARRAY:
            case Type.OBJECT:
                super.visitInsn(Opcodes.ACONST_NULL);
                break;
        }
    }
}
