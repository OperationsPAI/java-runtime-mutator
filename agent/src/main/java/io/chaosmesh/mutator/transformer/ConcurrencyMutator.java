package io.chaosmesh.mutator.transformer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Removes synchronization from bytecode
 */
public class ConcurrencyMutator extends MethodVisitor {
    private final String strategy;

    public ConcurrencyMutator(int api, MethodVisitor mv, String strategy) {
        super(api, mv);
        this.strategy = strategy;
    }

    @Override
    public void visitInsn(int opcode) {
        if (strategy.equals("remove_sync") && isMonitorInsn(opcode)) {
            return;
        }
        super.visitInsn(opcode);
    }

    private boolean isMonitorInsn(int opcode) {
        return opcode == Opcodes.MONITORENTER || opcode == Opcodes.MONITOREXIT;
    }
}
