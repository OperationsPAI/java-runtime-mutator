package io.chaosmesh.mutator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.ProtectionDomain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MutatorTransformerTest {

    @Mock
    private MutationManager mutationManager;

    private MutatorTransformer transformer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transformer = new MutatorTransformer(mutationManager);
    }

    @Test
    void testTransformReturnsNullWhenDisabled() {
        when(mutationManager.isEnabled()).thenReturn(false);

        byte[] result = transformer.transform(null, "com/example/Test", null, null, new byte[0]);

        assertNull(result);
        verify(mutationManager).isEnabled();
    }

    @Test
    void testTransformReturnsNullWhenNoMutations() {
        when(mutationManager.isEnabled()).thenReturn(true);
        when(mutationManager.getAllMutationStates()).thenReturn(java.util.Collections.emptyList());

        byte[] result = transformer.transform(null, "com/example/Test", null, null, new byte[0]);

        assertNull(result);
    }
}
