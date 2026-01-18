package io.chaosmesh.mutator.transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.mockito.Mockito.*;
import static org.objectweb.asm.Opcodes.ASM9;

class ConditionMutatorTest {

    @Mock
    private MethodVisitor mv;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testNegateConditions() {
        ConditionMutator mutator = new ConditionMutator(ASM9, mv, "negate");
        org.objectweb.asm.Label label = new org.objectweb.asm.Label();

        mutator.visitJumpInsn(Opcodes.IFEQ, label);
        verify(mv).visitJumpInsn(Opcodes.IFNE, label);

        mutator.visitJumpInsn(Opcodes.IFLT, label);
        verify(mv).visitJumpInsn(Opcodes.IFGE, label);

        mutator.visitJumpInsn(Opcodes.IFNULL, label);
        verify(mv).visitJumpInsn(Opcodes.IFNONNULL, label);
    }

    @Test
    void testAlwaysTrue() {
        ConditionMutator mutator = new ConditionMutator(ASM9, mv, "always_true");
        org.objectweb.asm.Label label = new org.objectweb.asm.Label();

        mutator.visitJumpInsn(Opcodes.IFEQ, label);

        verify(mv).visitJumpInsn(Opcodes.GOTO, label);
    }

    @Test
    void testAlwaysFalse() {
        ConditionMutator mutator = new ConditionMutator(ASM9, mv, "always_false");
        org.objectweb.asm.Label label = new org.objectweb.asm.Label();

        mutator.visitJumpInsn(Opcodes.IFEQ, label);

        verify(mv, never()).visitJumpInsn(anyInt(), any());
    }

    @Test
    void testUnknownStrategy() {
        ConditionMutator mutator = new ConditionMutator(ASM9, mv, "unknown");
        org.objectweb.asm.Label label = new org.objectweb.asm.Label();

        mutator.visitJumpInsn(Opcodes.IFEQ, label);

        verify(mv).visitJumpInsn(Opcodes.IFEQ, label);
    }
}
