package io.chaosmesh.mutator.transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConstantMutatorTest {

    @Mock
    private MethodVisitor mockMethodVisitor;

    private ConstantMutator mutator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIntegerMutationZero() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", null, null);
        mutator.visitLdcInsn(100);
        verify(mockMethodVisitor).visitLdcInsn(0);
    }

    @Test
    void testIntegerMutationOne() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "one", null, null);
        mutator.visitLdcInsn(100);
        verify(mockMethodVisitor).visitLdcInsn(1);
    }

    @Test
    void testIntegerMutationMinusOne() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "minus_one", null, null);
        mutator.visitLdcInsn(100);
        verify(mockMethodVisitor).visitLdcInsn(-1);
    }

    @Test
    void testIntegerMutationMax() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "max", null, null);
        mutator.visitLdcInsn(100);
        verify(mockMethodVisitor).visitLdcInsn(Integer.MAX_VALUE);
    }

    @Test
    void testIntegerMutationMin() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "min", null, null);
        mutator.visitLdcInsn(100);
        verify(mockMethodVisitor).visitLdcInsn(Integer.MIN_VALUE);
    }

    @Test
    void testIntegerMutationNegate() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "negate", null, null);
        mutator.visitLdcInsn(100);
        verify(mockMethodVisitor).visitLdcInsn(-100);
    }

    @Test
    void testIntegerMutationIncrement() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "increment", null, null);
        mutator.visitLdcInsn(100);
        verify(mockMethodVisitor).visitLdcInsn(101);
    }

    @Test
    void testIntegerMutationDecrement() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "decrement", null, null);
        mutator.visitLdcInsn(100);
        verify(mockMethodVisitor).visitLdcInsn(99);
    }

    @Test
    void testFloatMutationZero() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", null, null);
        mutator.visitLdcInsn(3.14f);
        verify(mockMethodVisitor).visitLdcInsn(0.0f);
    }

    @Test
    void testFloatMutationOne() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "one", null, null);
        mutator.visitLdcInsn(3.14f);
        verify(mockMethodVisitor).visitLdcInsn(1.0f);
    }

    @Test
    void testFloatMutationNaN() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "nan", null, null);
        mutator.visitLdcInsn(3.14f);
        verify(mockMethodVisitor).visitLdcInsn(Float.NaN);
    }

    @Test
    void testFloatMutationInfinity() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "infinity", null, null);
        mutator.visitLdcInsn(3.14f);
        verify(mockMethodVisitor).visitLdcInsn(Float.POSITIVE_INFINITY);
    }

    @Test
    void testFloatMutationNegInfinity() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "neg_infinity", null, null);
        mutator.visitLdcInsn(3.14f);
        verify(mockMethodVisitor).visitLdcInsn(Float.NEGATIVE_INFINITY);
    }

    @Test
    void testFloatMutationNegate() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "negate", null, null);
        mutator.visitLdcInsn(3.14f);
        verify(mockMethodVisitor).visitLdcInsn(-3.14f);
    }

    @Test
    void testDoubleMutationZero() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", null, null);
        mutator.visitLdcInsn(3.14);
        verify(mockMethodVisitor).visitLdcInsn(0.0);
    }

    @Test
    void testDoubleMutationOne() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "one", null, null);
        mutator.visitLdcInsn(3.14);
        verify(mockMethodVisitor).visitLdcInsn(1.0);
    }

    @Test
    void testDoubleMutationNaN() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "nan", null, null);
        mutator.visitLdcInsn(3.14);
        verify(mockMethodVisitor).visitLdcInsn(Double.NaN);
    }

    @Test
    void testDoubleMutationInfinity() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "infinity", null, null);
        mutator.visitLdcInsn(3.14);
        verify(mockMethodVisitor).visitLdcInsn(Double.POSITIVE_INFINITY);
    }

    @Test
    void testDoubleMutationNegInfinity() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "neg_infinity", null, null);
        mutator.visitLdcInsn(3.14);
        verify(mockMethodVisitor).visitLdcInsn(Double.NEGATIVE_INFINITY);
    }

    @Test
    void testDoubleMutationNegate() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "negate", null, null);
        mutator.visitLdcInsn(3.14);
        verify(mockMethodVisitor).visitLdcInsn(-3.14);
    }

    @Test
    void testStringMutationEmpty() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "empty", null, null);
        mutator.visitLdcInsn("hello");
        verify(mockMethodVisitor).visitLdcInsn("");
    }

    @Test
    void testStringMutationNull() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "null", null, null);
        mutator.visitLdcInsn("hello");
        verify(mockMethodVisitor).visitLdcInsn(null);
    }

    @Test
    void testStringMutationReverse() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "reverse", null, null);
        mutator.visitLdcInsn("hello");
        verify(mockMethodVisitor).visitLdcInsn("olleh");
    }

    @Test
    void testStringMutationUppercase() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "uppercase", null, null);
        mutator.visitLdcInsn("hello");
        verify(mockMethodVisitor).visitLdcInsn("HELLO");
    }

    @Test
    void testStringMutationLowercase() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "lowercase", null, null);
        mutator.visitLdcInsn("HELLO");
        verify(mockMethodVisitor).visitLdcInsn("hello");
    }

    @Test
    void testCustomToValue() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", null, 999);
        mutator.visitLdcInsn(100);
        verify(mockMethodVisitor).visitLdcInsn(999);
    }

    @Test
    void testStringToNumberConversion() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", "100", "200");
        mutator.visitLdcInsn(100);
        verify(mockMethodVisitor).visitLdcInsn(200);
    }

    @Test
    void testFromValueMatching() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", 100, null);
        mutator.visitLdcInsn(100);
        verify(mockMethodVisitor).visitLdcInsn(0);

        reset(mockMethodVisitor);
        mutator.visitLdcInsn(200);
        verify(mockMethodVisitor).visitLdcInsn(200);
    }

    @Test
    void testFromValueNotMatching() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", 100, null);
        mutator.visitLdcInsn(200);
        verify(mockMethodVisitor).visitLdcInsn(200);
    }

    @Test
    void testVisitIntInsn() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", null, null);
        mutator.visitIntInsn(Opcodes.BIPUSH, 50);
        verify(mockMethodVisitor).visitIntInsn(Opcodes.BIPUSH, 0);
    }

    @Test
    void testVisitIntInsnNoMatch() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", 100, null);
        mutator.visitIntInsn(Opcodes.BIPUSH, 50);
        verify(mockMethodVisitor).visitIntInsn(Opcodes.BIPUSH, 50);
    }

    @Test
    void testVisitInsnIconst() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "one", null, null);
        mutator.visitInsn(Opcodes.ICONST_0);
        verify(mockMethodVisitor).visitInsn(Opcodes.ICONST_1);
    }

    @Test
    void testVisitInsnIconstOutOfRange() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "max", null, null);
        mutator.visitInsn(Opcodes.ICONST_0);
        verify(mockMethodVisitor).visitLdcInsn(Integer.MAX_VALUE);
    }

    @Test
    void testVisitInsnNonIconst() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", null, null);
        mutator.visitInsn(Opcodes.NOP);
        verify(mockMethodVisitor).visitInsn(Opcodes.NOP);
    }

    @Test
    void testLongMutationZero() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", null, null);
        mutator.visitLdcInsn(1000L);
        verify(mockMethodVisitor).visitLdcInsn(0L);
    }

    @Test
    void testLongMutationOne() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "one", null, null);
        mutator.visitLdcInsn(1000L);
        verify(mockMethodVisitor).visitLdcInsn(1L);
    }

    @Test
    void testLongMutationMinusOne() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "minus_one", null, null);
        mutator.visitLdcInsn(1000L);
        verify(mockMethodVisitor).visitLdcInsn(-1L);
    }

    @Test
    void testLongMutationMax() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "max", null, null);
        mutator.visitLdcInsn(1000L);
        verify(mockMethodVisitor).visitLdcInsn(Long.MAX_VALUE);
    }

    @Test
    void testLongMutationMin() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "min", null, null);
        mutator.visitLdcInsn(1000L);
        verify(mockMethodVisitor).visitLdcInsn(Long.MIN_VALUE);
    }

    @Test
    void testLongMutationNegate() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "negate", null, null);
        mutator.visitLdcInsn(1000L);
        verify(mockMethodVisitor).visitLdcInsn(-1000L);
    }

    @Test
    void testStringToNumberConversionFloat() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", "3.14", "2.71");
        mutator.visitLdcInsn(3.14f);
        verify(mockMethodVisitor).visitLdcInsn(2.71f);
    }

    @Test
    void testStringToNumberConversionDouble() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", "3.14", "2.71");
        mutator.visitLdcInsn(3.14);
        verify(mockMethodVisitor).visitLdcInsn(2.71);
    }

    @Test
    void testStringToNumberConversionLong() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", "1000", "2000");
        mutator.visitLdcInsn(1000L);
        verify(mockMethodVisitor).visitLdcInsn(2000L);
    }

    @Test
    void testInvalidStringToNumberConversion() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", "invalid", "also_invalid");
        mutator.visitLdcInsn(100);
        // When fromValue doesn't match, the original value should pass through
        verify(mockMethodVisitor).visitLdcInsn(100);
    }

    @Test
    void testUnknownMutationType() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "unknown", null, null);
        mutator.visitLdcInsn(100);
        verify(mockMethodVisitor).visitLdcInsn(0);
    }

    @Test
    void testNullFromValue() {
        mutator = new ConstantMutator(Opcodes.ASM9, mockMethodVisitor, "zero", null, null);
        mutator.visitLdcInsn(100);
        mutator.visitLdcInsn(200);
        mutator.visitLdcInsn(300);
        verify(mockMethodVisitor, times(3)).visitLdcInsn(0);
    }
}
