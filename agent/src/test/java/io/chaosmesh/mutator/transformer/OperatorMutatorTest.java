package io.chaosmesh.mutator.transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.mockito.Mockito.*;

class OperatorMutatorTest {

    @Mock
    private MethodVisitor mockMethodVisitor;

    private OperatorMutator mutator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Integer arithmetic mutations
    @Test
    void testIntegerAddToSub() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "add_to_sub");
        mutator.visitInsn(Opcodes.IADD);
        verify(mockMethodVisitor).visitInsn(Opcodes.ISUB);
    }

    @Test
    void testIntegerSubToAdd() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "sub_to_add");
        mutator.visitInsn(Opcodes.ISUB);
        verify(mockMethodVisitor).visitInsn(Opcodes.IADD);
    }

    @Test
    void testIntegerMulToDiv() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "mul_to_div");
        mutator.visitInsn(Opcodes.IMUL);
        verify(mockMethodVisitor).visitInsn(Opcodes.IDIV);
    }

    @Test
    void testIntegerDivToMul() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "div_to_mul");
        mutator.visitInsn(Opcodes.IDIV);
        verify(mockMethodVisitor).visitInsn(Opcodes.IMUL);
    }

    @Test
    void testIntegerRemToDiv() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "default");
        mutator.visitInsn(Opcodes.IREM);
        verify(mockMethodVisitor).visitInsn(Opcodes.IDIV);
    }

    // Long arithmetic mutations
    @Test
    void testLongAddToSub() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "add_to_sub");
        mutator.visitInsn(Opcodes.LADD);
        verify(mockMethodVisitor).visitInsn(Opcodes.LSUB);
    }

    @Test
    void testLongSubToAdd() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "sub_to_add");
        mutator.visitInsn(Opcodes.LSUB);
        verify(mockMethodVisitor).visitInsn(Opcodes.LADD);
    }

    @Test
    void testLongMulToDiv() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "mul_to_div");
        mutator.visitInsn(Opcodes.LMUL);
        verify(mockMethodVisitor).visitInsn(Opcodes.LDIV);
    }

    @Test
    void testLongDivToMul() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "div_to_mul");
        mutator.visitInsn(Opcodes.LDIV);
        verify(mockMethodVisitor).visitInsn(Opcodes.LMUL);
    }

    @Test
    void testLongRemToDiv() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "default");
        mutator.visitInsn(Opcodes.LREM);
        verify(mockMethodVisitor).visitInsn(Opcodes.LDIV);
    }

    // Float arithmetic mutations
    @Test
    void testFloatAddToSub() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "add_to_sub");
        mutator.visitInsn(Opcodes.FADD);
        verify(mockMethodVisitor).visitInsn(Opcodes.FSUB);
    }

    @Test
    void testFloatSubToAdd() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "sub_to_add");
        mutator.visitInsn(Opcodes.FSUB);
        verify(mockMethodVisitor).visitInsn(Opcodes.FADD);
    }

    @Test
    void testFloatMulToDiv() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "mul_to_div");
        mutator.visitInsn(Opcodes.FMUL);
        verify(mockMethodVisitor).visitInsn(Opcodes.FDIV);
    }

    @Test
    void testFloatDivToMul() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "div_to_mul");
        mutator.visitInsn(Opcodes.FDIV);
        verify(mockMethodVisitor).visitInsn(Opcodes.FMUL);
    }

    @Test
    void testFloatRemToDiv() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "default");
        mutator.visitInsn(Opcodes.FREM);
        verify(mockMethodVisitor).visitInsn(Opcodes.FDIV);
    }

    // Double arithmetic mutations
    @Test
    void testDoubleAddToSub() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "add_to_sub");
        mutator.visitInsn(Opcodes.DADD);
        verify(mockMethodVisitor).visitInsn(Opcodes.DSUB);
    }

    @Test
    void testDoubleSubToAdd() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "sub_to_add");
        mutator.visitInsn(Opcodes.DSUB);
        verify(mockMethodVisitor).visitInsn(Opcodes.DADD);
    }

    @Test
    void testDoubleMulToDiv() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "mul_to_div");
        mutator.visitInsn(Opcodes.DMUL);
        verify(mockMethodVisitor).visitInsn(Opcodes.DDIV);
    }

    @Test
    void testDoubleDivToMul() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "div_to_mul");
        mutator.visitInsn(Opcodes.DDIV);
        verify(mockMethodVisitor).visitInsn(Opcodes.DMUL);
    }

    @Test
    void testDoubleRemToDiv() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "default");
        mutator.visitInsn(Opcodes.DREM);
        verify(mockMethodVisitor).visitInsn(Opcodes.DDIV);
    }

    // Default mutation strategies
    @Test
    void testDefaultAddMutation() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "unknown");
        mutator.visitInsn(Opcodes.IADD);
        verify(mockMethodVisitor).visitInsn(Opcodes.ISUB);
    }

    @Test
    void testDefaultSubMutation() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "unknown");
        mutator.visitInsn(Opcodes.ISUB);
        verify(mockMethodVisitor).visitInsn(Opcodes.IADD);
    }

    @Test
    void testDefaultMulMutation() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "unknown");
        mutator.visitInsn(Opcodes.IMUL);
        verify(mockMethodVisitor).visitInsn(Opcodes.IDIV);
    }

    @Test
    void testDefaultDivMutation() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "unknown");
        mutator.visitInsn(Opcodes.IDIV);
        verify(mockMethodVisitor).visitInsn(Opcodes.IMUL);
    }

    // Alternative mutations
    @Test
    void testAddToMul() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "add_to_mul");
        mutator.visitInsn(Opcodes.IADD);
        verify(mockMethodVisitor).visitInsn(Opcodes.IMUL);
    }

    @Test
    void testAddToDiv() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "add_to_div");
        mutator.visitInsn(Opcodes.IADD);
        verify(mockMethodVisitor).visitInsn(Opcodes.IDIV);
    }

    @Test
    void testSubToMul() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "sub_to_mul");
        mutator.visitInsn(Opcodes.ISUB);
        verify(mockMethodVisitor).visitInsn(Opcodes.IMUL);
    }

    @Test
    void testSubToDiv() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "sub_to_div");
        mutator.visitInsn(Opcodes.ISUB);
        verify(mockMethodVisitor).visitInsn(Opcodes.IDIV);
    }

    @Test
    void testMulToAdd() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "mul_to_add");
        mutator.visitInsn(Opcodes.IMUL);
        verify(mockMethodVisitor).visitInsn(Opcodes.IADD);
    }

    @Test
    void testMulToSub() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "mul_to_sub");
        mutator.visitInsn(Opcodes.IMUL);
        verify(mockMethodVisitor).visitInsn(Opcodes.ISUB);
    }

    @Test
    void testDivToAdd() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "div_to_add");
        mutator.visitInsn(Opcodes.IDIV);
        verify(mockMethodVisitor).visitInsn(Opcodes.IADD);
    }

    @Test
    void testDivToSub() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "div_to_sub");
        mutator.visitInsn(Opcodes.IDIV);
        verify(mockMethodVisitor).visitInsn(Opcodes.ISUB);
    }

    // Non-arithmetic opcodes should pass through unchanged
    @Test
    void testNonArithmeticOpcode() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "add_to_sub");
        mutator.visitInsn(Opcodes.NOP);
        verify(mockMethodVisitor).visitInsn(Opcodes.NOP);
    }

    @Test
    void testReturnOpcode() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "add_to_sub");
        mutator.visitInsn(Opcodes.RETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.RETURN);
    }

    @Test
    void testIconstOpcode() {
        mutator = new OperatorMutator(Opcodes.ASM9, mockMethodVisitor, "add_to_sub");
        mutator.visitInsn(Opcodes.ICONST_0);
        verify(mockMethodVisitor).visitInsn(Opcodes.ICONST_0);
    }
}
