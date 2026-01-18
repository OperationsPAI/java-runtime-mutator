package io.chaosmesh.mutator.transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.mockito.Mockito.*;
import static org.objectweb.asm.Opcodes.ASM9;

class TimingMutatorTest {

    @Mock
    private MethodVisitor mv;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDelayInjection() {
        TimingMutator mutator = new TimingMutator(ASM9, mv, 1000L);

        mutator.visitCode();

        verify(mv).visitCode();
        verify(mv).visitLdcInsn(1000L);
        verify(mv).visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
    }

    @Test
    void testDelayInjectedOnlyOnce() {
        TimingMutator mutator = new TimingMutator(ASM9, mv, 500L);

        mutator.visitCode();
        mutator.visitCode();

        verify(mv, times(1)).visitLdcInsn(500L);
    }
}
