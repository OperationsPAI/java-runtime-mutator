package io.chaosmesh.mutator.transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.mockito.Mockito.*;
import static org.objectweb.asm.Opcodes.ASM9;

class NullPointerMutatorTest {

    @Mock
    private MethodVisitor mv;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testNullLocalsStrategyForALOAD() {
        NullPointerMutator mutator = new NullPointerMutator(ASM9, mv, "null_locals");

        mutator.visitVarInsn(Opcodes.ALOAD, 0);

        verify(mv).visitInsn(Opcodes.ACONST_NULL);
        verify(mv, never()).visitVarInsn(anyInt(), anyInt());
    }

    @Test
    void testNullLocalsStrategyForALOAD1() {
        NullPointerMutator mutator = new NullPointerMutator(ASM9, mv, "null_locals");

        mutator.visitVarInsn(Opcodes.ALOAD, 1);

        verify(mv).visitInsn(Opcodes.ACONST_NULL);
        verify(mv, never()).visitVarInsn(anyInt(), anyInt());
    }

    @Test
    void testNonObjectLoadNotAffected() {
        NullPointerMutator mutator = new NullPointerMutator(ASM9, mv, "null_locals");

        mutator.visitVarInsn(Opcodes.ILOAD, 0);

        verify(mv).visitVarInsn(Opcodes.ILOAD, 0);
        verify(mv, never()).visitInsn(Opcodes.ACONST_NULL);
    }

    @Test
    void testUnknownStrategy() {
        NullPointerMutator mutator = new NullPointerMutator(ASM9, mv, "unknown");

        mutator.visitVarInsn(Opcodes.ALOAD, 0);

        verify(mv).visitVarInsn(Opcodes.ALOAD, 0);
        verify(mv, never()).visitInsn(Opcodes.ACONST_NULL);
    }

    @Test
    void testASTORE() {
        NullPointerMutator mutator = new NullPointerMutator(ASM9, mv, "null_locals");

        mutator.visitVarInsn(Opcodes.ASTORE, 0);

        verify(mv).visitVarInsn(Opcodes.ASTORE, 0);
        verify(mv, never()).visitInsn(Opcodes.ACONST_NULL);
    }
}
