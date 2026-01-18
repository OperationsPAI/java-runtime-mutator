package io.chaosmesh.mutator.transformer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Injects exception throwing into bytecode
 */
public class ExceptionMutator extends MethodVisitor {
    private final String exceptionClass;
    private final String message;
    private boolean injected = false;

    public ExceptionMutator(int api, MethodVisitor mv, String exceptionClass, String message) {
        super(api, mv);
        this.exceptionClass = exceptionClass;
        this.message = message;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        if (!injected) {
            injectException();
            injected = true;
        }
    }

    private void injectException() {
        String internalName = exceptionClass.replace('.', '/');
        super.visitTypeInsn(Opcodes.NEW, internalName);
        super.visitInsn(Opcodes.DUP);
        if (message != null) {
            super.visitLdcInsn(message);
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, internalName, "<init>", "(Ljava/lang/String;)V", false);
        } else {
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, internalName, "<init>", "()V", false);
        }
        super.visitInsn(Opcodes.ATHROW);
    }
}
