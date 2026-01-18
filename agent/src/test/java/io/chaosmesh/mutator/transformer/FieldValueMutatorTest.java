package io.chaosmesh.mutator.transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.mockito.Mockito.*;
import static org.objectweb.asm.Opcodes.ASM9;

class FieldValueMutatorTest {

    @Mock
    private MethodVisitor mv;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testNullStrategy() {
        FieldValueMutator mutator = new FieldValueMutator(ASM9, mv, "null", "targetField");

        mutator.visitFieldInsn(Opcodes.GETFIELD, "com/example/Test", "targetField", "Ljava/lang/String;");

        verify(mv).visitFieldInsn(Opcodes.GETFIELD, "com/example/Test", "targetField", "Ljava/lang/String;");
        verify(mv).visitInsn(Opcodes.POP);
        verify(mv).visitInsn(Opcodes.ACONST_NULL);
    }

    @Test
    void testZeroStrategyForPrimitives() {
        FieldValueMutator mutator = new FieldValueMutator(ASM9, mv, "zero", "targetField");

        mutator.visitFieldInsn(Opcodes.GETFIELD, "com/example/Test", "targetField", "I");
        verify(mv).visitInsn(Opcodes.ICONST_0);

        mutator.visitFieldInsn(Opcodes.GETFIELD, "com/example/Test", "targetField", "J");
        verify(mv).visitInsn(Opcodes.LCONST_0);
    }

    @Test
    void testNoMutationForDifferentField() {
        FieldValueMutator mutator = new FieldValueMutator(ASM9, mv, "null", "targetField");

        mutator.visitFieldInsn(Opcodes.GETFIELD, "com/example/Test", "otherField", "Ljava/lang/String;");

        verify(mv).visitFieldInsn(Opcodes.GETFIELD, "com/example/Test", "otherField", "Ljava/lang/String;");
        verify(mv, never()).visitInsn(Opcodes.POP);
    }

    @Test
    void testMutateAllFieldsWhenTargetIsNull() {
        FieldValueMutator mutator = new FieldValueMutator(ASM9, mv, "null", null);

        mutator.visitFieldInsn(Opcodes.GETFIELD, "com/example/Test", "anyField", "Ljava/lang/String;");

        verify(mv).visitInsn(Opcodes.ACONST_NULL);
    }
}
