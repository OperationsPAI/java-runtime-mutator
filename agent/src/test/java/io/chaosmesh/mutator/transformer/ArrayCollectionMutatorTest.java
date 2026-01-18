package io.chaosmesh.mutator.transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.mockito.Mockito.*;
import static org.objectweb.asm.Opcodes.ASM9;

class ArrayCollectionMutatorTest {

    @Mock
    private MethodVisitor mv;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEmptyStrategyForArrays() {
        ArrayCollectionMutator mutator = new ArrayCollectionMutator(ASM9, mv, "empty");

        mutator.visitInsn(Opcodes.IALOAD);
        verify(mv, times(2)).visitInsn(Opcodes.POP);
        verify(mv).visitInsn(Opcodes.ICONST_0);

        mutator.visitInsn(Opcodes.AALOAD);
        verify(mv).visitInsn(Opcodes.ACONST_NULL);
    }

    @Test
    void testNonArrayLoadNotAffected() {
        ArrayCollectionMutator mutator = new ArrayCollectionMutator(ASM9, mv, "empty");

        mutator.visitInsn(Opcodes.IADD);

        verify(mv).visitInsn(Opcodes.IADD);
        verify(mv, never()).visitInsn(Opcodes.POP);
    }
}
