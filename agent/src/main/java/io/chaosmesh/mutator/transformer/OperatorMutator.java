package io.chaosmesh.mutator.transformer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Mutates arithmetic and logical operators in bytecode.
 */
public class OperatorMutator extends MethodVisitor {
    private final String mutationType;

    public OperatorMutator(int api, MethodVisitor mv, String mutationType) {
        super(api, mv);
        this.mutationType = mutationType;
    }

    @Override
    public void visitInsn(int opcode) {
        int mutated = mutateOpcode(opcode);
        super.visitInsn(mutated);
    }

    private int mutateOpcode(int opcode) {
        // Arithmetic operator mutations
        switch (opcode) {
            // Integer arithmetic
            case Opcodes.IADD: return mutateAdd(Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV);
            case Opcodes.ISUB: return mutateSub(Opcodes.IADD, Opcodes.IMUL, Opcodes.IDIV);
            case Opcodes.IMUL: return mutateMul(Opcodes.IADD, Opcodes.ISUB, Opcodes.IDIV);
            case Opcodes.IDIV: return mutateDiv(Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL);
            case Opcodes.IREM: return Opcodes.IDIV;

            // Long arithmetic
            case Opcodes.LADD: return mutateAdd(Opcodes.LSUB, Opcodes.LMUL, Opcodes.LDIV);
            case Opcodes.LSUB: return mutateSub(Opcodes.LADD, Opcodes.LMUL, Opcodes.LDIV);
            case Opcodes.LMUL: return mutateMul(Opcodes.LADD, Opcodes.LSUB, Opcodes.LDIV);
            case Opcodes.LDIV: return mutateDiv(Opcodes.LADD, Opcodes.LSUB, Opcodes.LMUL);
            case Opcodes.LREM: return Opcodes.LDIV;

            // Float arithmetic
            case Opcodes.FADD: return mutateAdd(Opcodes.FSUB, Opcodes.FMUL, Opcodes.FDIV);
            case Opcodes.FSUB: return mutateSub(Opcodes.FADD, Opcodes.FMUL, Opcodes.FDIV);
            case Opcodes.FMUL: return mutateMul(Opcodes.FADD, Opcodes.FSUB, Opcodes.FDIV);
            case Opcodes.FDIV: return mutateDiv(Opcodes.FADD, Opcodes.FSUB, Opcodes.FMUL);
            case Opcodes.FREM: return Opcodes.FDIV;

            // Double arithmetic
            case Opcodes.DADD: return mutateAdd(Opcodes.DSUB, Opcodes.DMUL, Opcodes.DDIV);
            case Opcodes.DSUB: return mutateSub(Opcodes.DADD, Opcodes.DMUL, Opcodes.DDIV);
            case Opcodes.DMUL: return mutateMul(Opcodes.DADD, Opcodes.DSUB, Opcodes.DDIV);
            case Opcodes.DDIV: return mutateDiv(Opcodes.DADD, Opcodes.DSUB, Opcodes.DMUL);
            case Opcodes.DREM: return Opcodes.DDIV;

            default: return opcode;
        }
    }

    private int mutateAdd(int sub, int mul, int div) {
        switch (mutationType) {
            case "add_to_sub": return sub;
            case "add_to_mul": return mul;
            case "add_to_div": return div;
            default: return sub; // Default: + → -
        }
    }

    private int mutateSub(int add, int mul, int div) {
        switch (mutationType) {
            case "sub_to_add": return add;
            case "sub_to_mul": return mul;
            case "sub_to_div": return div;
            default: return add; // Default: - → +
        }
    }

    private int mutateMul(int add, int sub, int div) {
        switch (mutationType) {
            case "mul_to_add": return add;
            case "mul_to_sub": return sub;
            case "mul_to_div": return div;
            default: return div; // Default: * → /
        }
    }

    private int mutateDiv(int add, int sub, int mul) {
        switch (mutationType) {
            case "div_to_add": return add;
            case "div_to_sub": return sub;
            case "div_to_mul": return mul;
            default: return mul; // Default: / → *
        }
    }
}
