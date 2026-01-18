package io.chaosmesh.mutator.transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.mockito.Mockito.*;
import static org.objectweb.asm.Opcodes.ASM9;

class LoopMutatorTest {

    @Mock
    private MethodVisitor mv;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSkipFirstStrategy() {
        LoopMutator mutator = new LoopMutator(ASM9, mv, "skip_first");
        Label label = new Label();

        mutator.visitJumpInsn(Opcodes.GOTO, label);

        verify(mv).visitInsn(Opcodes.NOP);
        verify(mv, never()).visitJumpInsn(anyInt(), any());
    }

    @Test
    void testInfiniteStrategy() {
        LoopMutator mutator = new LoopMutator(ASM9, mv, "infinite");
        Label label = new Label();

        mutator.visitJumpInsn(Opcodes.IFEQ, label);

        verify(mv).visitJumpInsn(Opcodes.GOTO, label);
    }
}
