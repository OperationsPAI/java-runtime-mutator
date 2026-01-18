package io.chaosmesh.mutator.transformer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Mutates loop control flow in bytecode
 */
public class LoopMutator extends MethodVisitor {
    private final String strategy;

    public LoopMutator(int api, MethodVisitor mv, String strategy) {
        super(api, mv);
        this.strategy = strategy;
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        if (strategy.equals("skip_first") && isBackwardJump(opcode)) {
            super.visitInsn(Opcodes.NOP);
            return;
        }
        if (strategy.equals("infinite") && isBackwardJump(opcode)) {
            super.visitJumpInsn(Opcodes.GOTO, label);
            return;
        }
        super.visitJumpInsn(opcode, label);
    }

    private boolean isBackwardJump(int opcode) {
        return opcode == Opcodes.GOTO ||
               opcode == Opcodes.IFEQ || opcode == Opcodes.IFNE ||
               opcode == Opcodes.IFLT || opcode == Opcodes.IFGE ||
               opcode == Opcodes.IFGT || opcode == Opcodes.IFLE;
    }
}
