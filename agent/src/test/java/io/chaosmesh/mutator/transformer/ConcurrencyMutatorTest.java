package io.chaosmesh.mutator.transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.mockito.Mockito.*;
import static org.objectweb.asm.Opcodes.ASM9;

class ConcurrencyMutatorTest {

    @Mock
    private MethodVisitor mv;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRemoveSyncMonitorInsn() {
        ConcurrencyMutator mutator = new ConcurrencyMutator(ASM9, mv, "remove_sync");

        mutator.visitInsn(Opcodes.MONITORENTER);
        verify(mv, never()).visitInsn(Opcodes.MONITORENTER);

        mutator.visitInsn(Opcodes.MONITOREXIT);
        verify(mv, never()).visitInsn(Opcodes.MONITOREXIT);
    }

    @Test
    void testNonMonitorInsnNotAffected() {
        ConcurrencyMutator mutator = new ConcurrencyMutator(ASM9, mv, "remove_sync");

        mutator.visitInsn(Opcodes.IADD);

        verify(mv).visitInsn(Opcodes.IADD);
    }
}
