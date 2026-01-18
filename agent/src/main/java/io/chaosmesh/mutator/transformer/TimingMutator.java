package io.chaosmesh.mutator.transformer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Injects timing delays into bytecode
 */
public class TimingMutator extends MethodVisitor {
    private final long delayMs;
    private boolean injected = false;

    public TimingMutator(int api, MethodVisitor mv, long delayMs) {
        super(api, mv);
        this.delayMs = delayMs;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        if (!injected) {
            injectDelay();
            injected = true;
        }
    }

    private void injectDelay() {
        super.visitLdcInsn(delayMs);
        super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
    }
}
