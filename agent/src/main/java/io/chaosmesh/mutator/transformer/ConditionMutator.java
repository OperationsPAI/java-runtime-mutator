package io.chaosmesh.mutator.transformer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Mutates conditional logic in bytecode (negate conditions, swap branches)
 */
public class ConditionMutator extends MethodVisitor {
    private final String strategy;

    public ConditionMutator(int api, MethodVisitor mv, String strategy) {
        super(api, mv);
        this.strategy = strategy;
    }

    @Override
    public void visitJumpInsn(int opcode, org.objectweb.asm.Label label) {
        if (strategy.equals("always_false")) {
            return;
        }
        super.visitJumpInsn(mutateJump(opcode), label);
    }

    private int mutateJump(int opcode) {
        switch (strategy) {
            case "negate":
                return negateCondition(opcode);
            case "always_true":
                return Opcodes.GOTO;
            default:
                return opcode;
        }
    }

    private int negateCondition(int opcode) {
        switch (opcode) {
            case Opcodes.IFEQ: return Opcodes.IFNE;
            case Opcodes.IFNE: return Opcodes.IFEQ;
            case Opcodes.IFLT: return Opcodes.IFGE;
            case Opcodes.IFGE: return Opcodes.IFLT;
            case Opcodes.IFGT: return Opcodes.IFLE;
            case Opcodes.IFLE: return Opcodes.IFGT;
            case Opcodes.IF_ICMPEQ: return Opcodes.IF_ICMPNE;
            case Opcodes.IF_ICMPNE: return Opcodes.IF_ICMPEQ;
            case Opcodes.IF_ICMPLT: return Opcodes.IF_ICMPGE;
            case Opcodes.IF_ICMPGE: return Opcodes.IF_ICMPLT;
            case Opcodes.IF_ICMPGT: return Opcodes.IF_ICMPLE;
            case Opcodes.IF_ICMPLE: return Opcodes.IF_ICMPGT;
            case Opcodes.IF_ACMPEQ: return Opcodes.IF_ACMPNE;
            case Opcodes.IF_ACMPNE: return Opcodes.IF_ACMPEQ;
            case Opcodes.IFNULL: return Opcodes.IFNONNULL;
            case Opcodes.IFNONNULL: return Opcodes.IFNULL;
            default: return opcode;
        }
    }
}
