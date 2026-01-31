package io.chaosmesh.mutator.transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.mockito.Mockito.*;

class ReturnValueMutatorTest {

    @Mock
    private MethodVisitor mockMethodVisitor;

    private ReturnValueMutator mutator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Null mutation tests
    @Test
    void testMutateToNullForObjectReturn() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "null", null, "()Ljava/lang/String;", null);
        mutator.visitInsn(Opcodes.ARETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitInsn(Opcodes.ACONST_NULL);
        verify(mockMethodVisitor).visitInsn(Opcodes.ARETURN);
    }

    @Test
    void testMutateToNullForIntReturnDoesNothing() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "null", null, "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    // Zero mutation tests
    @Test
    void testMutateToZeroForIntReturn() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "zero", null, "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitInsn(Opcodes.ICONST_0);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    @Test
    void testMutateToZeroForLongReturn() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "zero", null, "()J", null);
        mutator.visitInsn(Opcodes.LRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP2);
        verify(mockMethodVisitor).visitInsn(Opcodes.LCONST_0);
        verify(mockMethodVisitor).visitInsn(Opcodes.LRETURN);
    }

    @Test
    void testMutateToZeroForFloatReturn() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "zero", null, "()F", null);
        mutator.visitInsn(Opcodes.FRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitInsn(Opcodes.FCONST_0);
        verify(mockMethodVisitor).visitInsn(Opcodes.FRETURN);
    }

    @Test
    void testMutateToZeroForDoubleReturn() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "zero", null, "()D", null);
        mutator.visitInsn(Opcodes.DRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP2);
        verify(mockMethodVisitor).visitInsn(Opcodes.DCONST_0);
        verify(mockMethodVisitor).visitInsn(Opcodes.DRETURN);
    }

    // False mutation tests
    @Test
    void testMutateToFalseForBooleanReturn() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "false", null, "()Z", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitInsn(Opcodes.ICONST_0);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    @Test
    void testMutateToFalseForNonBooleanDoesNothing() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "false", null, "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    // Empty mutation tests
    @Test
    void testMutateToEmptyForStringReturn() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "empty", null, "()Ljava/lang/String;", null);
        mutator.visitInsn(Opcodes.ARETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitLdcInsn("");
        verify(mockMethodVisitor).visitInsn(Opcodes.ARETURN);
    }

    @Test
    void testMutateToEmptyForNonStringDoesNothing() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "empty", null, "()Ljava/lang/Object;", null);
        mutator.visitInsn(Opcodes.ARETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.ARETURN);
    }

    // Exception mutation tests
    @Test
    void testMutateToExceptionWithDefaultClass() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "exception", null, "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
        verify(mockMethodVisitor).visitInsn(Opcodes.DUP);
        verify(mockMethodVisitor).visitLdcInsn("Mutated return value");
        verify(mockMethodVisitor).visitMethodInsn(Opcodes.INVOKESPECIAL,
            "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
        verify(mockMethodVisitor).visitInsn(Opcodes.ATHROW);
    }

    @Test
    void testMutateToExceptionWithCustomClass() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "exception",
            "java.lang.IllegalArgumentException", "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitTypeInsn(Opcodes.NEW, "java/lang/IllegalArgumentException");
        verify(mockMethodVisitor).visitInsn(Opcodes.DUP);
        verify(mockMethodVisitor).visitLdcInsn("Mutated return value");
        verify(mockMethodVisitor).visitMethodInsn(Opcodes.INVOKESPECIAL,
            "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
        verify(mockMethodVisitor).visitInsn(Opcodes.ATHROW);
    }

    // Custom value mutation tests
    @Test
    void testMutateToCustomIntValue() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "custom", 999, "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitIntInsn(Opcodes.SIPUSH, 999);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    @Test
    void testMutateToCustomStringValue() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "custom", "custom_value", "()Ljava/lang/String;", null);
        mutator.visitInsn(Opcodes.ARETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitLdcInsn("custom_value");
        verify(mockMethodVisitor).visitInsn(Opcodes.ARETURN);
    }

    @Test
    void testMutateToCustomWithoutValue() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "custom", null, "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    // Non-return instructions should pass through
    @Test
    void testNonReturnInstructionPassesThrough() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "zero", null, "()I", null);
        mutator.visitInsn(Opcodes.NOP);
        verify(mockMethodVisitor).visitInsn(Opcodes.NOP);
    }

    @Test
    void testVoidReturnPassesThrough() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "zero", null, "()V", null);
        mutator.visitInsn(Opcodes.RETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.RETURN);
    }

    // Custom value with small integers
    @Test
    void testCustomValueSmallInteger() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "custom", 3, "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitInsn(Opcodes.ICONST_3);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    @Test
    void testCustomValueNegativeOne() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "custom", -1, "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitInsn(Opcodes.ICONST_M1);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    @Test
    void testCustomValueByteRange() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "custom", 100, "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitIntInsn(Opcodes.BIPUSH, 100);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    @Test
    void testCustomValueShortRange() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "custom", 1000, "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitIntInsn(Opcodes.SIPUSH, 1000);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    @Test
    void testCustomValueLargeInteger() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "custom", 100000, "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitLdcInsn(100000);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    // Custom value with string conversion
    @Test
    void testCustomValueStringToInt() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "custom", "42", "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitIntInsn(Opcodes.BIPUSH, 42);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    @Test
    void testCustomValueInvalidStringToInt() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "custom", "invalid", "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitInsn(Opcodes.ICONST_0);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    // Custom value with non-string object
    @Test
    void testCustomValueNonStringObject() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "custom", 123, "()Ljava/lang/String;", null);
        mutator.visitInsn(Opcodes.ARETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitInsn(Opcodes.ACONST_NULL);
        verify(mockMethodVisitor).visitInsn(Opcodes.ARETURN);
    }

    // Unknown strategy should pass through
    @Test
    void testUnknownStrategyPassesThrough() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "unknown", null, "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    // Random mutation tests
    @Test
    void testMutateToRandomForBooleanReturn() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "random", null, "()Z", null);
        mutator.visitInsn(Opcodes.IRETURN);
        // Boolean: XOR with 1 to negate
        verify(mockMethodVisitor).visitInsn(Opcodes.ICONST_1);
        verify(mockMethodVisitor).visitInsn(Opcodes.IXOR);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    @Test
    void testMutateToRandomForIntReturnWithPreGeneratedValue() {
        // Test with pre-generated random value (as Map)
        java.util.Map<String, Object> randomValues = new java.util.HashMap<>();
        randomValues.put("int", 12345);
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "random", null, "()I", randomValues);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitIntInsn(Opcodes.SIPUSH, 12345);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    @Test
    void testMutateToRandomForIntReturnWithNullRandomValue() {
        // When randomValue is null, should use default value 0
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "random", null, "()I", null);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitInsn(Opcodes.ICONST_0);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    @Test
    void testMutateToRandomForLongReturnWithPreGeneratedValue() {
        java.util.Map<String, Object> randomValues = new java.util.HashMap<>();
        randomValues.put("long", 9876543210L);
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "random", null, "()J", randomValues);
        mutator.visitInsn(Opcodes.LRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP2);
        verify(mockMethodVisitor).visitLdcInsn(9876543210L);
        verify(mockMethodVisitor).visitInsn(Opcodes.LRETURN);
    }

    @Test
    void testMutateToRandomForFloatReturnWithPreGeneratedValue() {
        java.util.Map<String, Object> randomValues = new java.util.HashMap<>();
        randomValues.put("float", 0.5f);
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "random", null, "()F", randomValues);
        mutator.visitInsn(Opcodes.FRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitLdcInsn(0.5f);
        verify(mockMethodVisitor).visitInsn(Opcodes.FRETURN);
    }

    @Test
    void testMutateToRandomForDoubleReturnWithPreGeneratedValue() {
        java.util.Map<String, Object> randomValues = new java.util.HashMap<>();
        randomValues.put("double", 0.123456789);
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "random", null, "()D", randomValues);
        mutator.visitInsn(Opcodes.DRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP2);
        verify(mockMethodVisitor).visitLdcInsn(0.123456789);
        verify(mockMethodVisitor).visitInsn(Opcodes.DRETURN);
    }

    @Test
    void testMutateToRandomForStringReturnWithPreGeneratedValue() {
        java.util.Map<String, Object> randomValues = new java.util.HashMap<>();
        randomValues.put("string", "test-uuid-12345");
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "random", null, "()Ljava/lang/String;", randomValues);
        mutator.visitInsn(Opcodes.ARETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitLdcInsn("test-uuid-12345");
        verify(mockMethodVisitor).visitInsn(Opcodes.ARETURN);
    }

    @Test
    void testMutateToRandomForObjectReturnReturnsNull() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "random", null, "()Ljava/lang/Object;", null);
        mutator.visitInsn(Opcodes.ARETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitInsn(Opcodes.ACONST_NULL);
        verify(mockMethodVisitor).visitInsn(Opcodes.ARETURN);
    }

    @Test
    void testMutateToRandomForVoidReturnPassesThrough() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "random", null, "()V", null);
        mutator.visitInsn(Opcodes.RETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.RETURN);
    }

    @Test
    void testMutateToRandomWithDirectNumberValue() {
        // Test when randomValue is a direct Number (not a Map)
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "random", null, "()I", 42);
        mutator.visitInsn(Opcodes.IRETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitIntInsn(Opcodes.BIPUSH, 42);
        verify(mockMethodVisitor).visitInsn(Opcodes.IRETURN);
    }

    @Test
    void testMutateToRandomWithDirectStringValue() {
        // Test when randomValue is a direct String (not a Map)
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "random", null, "()Ljava/lang/String;", "direct-string");
        mutator.visitInsn(Opcodes.ARETURN);
        verify(mockMethodVisitor).visitInsn(Opcodes.POP);
        verify(mockMethodVisitor).visitLdcInsn("direct-string");
        verify(mockMethodVisitor).visitInsn(Opcodes.ARETURN);
    }

    // Multiple return types
    @Test
    void testAllReturnInstructions() {
        mutator = new ReturnValueMutator(Opcodes.ASM9, mockMethodVisitor, "zero", null, "()I", null);

        mutator.visitInsn(Opcodes.IRETURN);
        mutator.visitInsn(Opcodes.LRETURN);
        mutator.visitInsn(Opcodes.FRETURN);
        mutator.visitInsn(Opcodes.DRETURN);
        mutator.visitInsn(Opcodes.ARETURN);
        mutator.visitInsn(Opcodes.RETURN);

        verify(mockMethodVisitor, atLeastOnce()).visitInsn(anyInt());
    }
}
