package io.chaosmesh.mutator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MutationManagerTest {

    @Mock
    private Instrumentation instrumentation;

    private MutationConfig config;
    private MutationManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = new MutationConfig();
        when(instrumentation.getAllLoadedClasses()).thenReturn(new Class<?>[0]);
        manager = new MutationManager(instrumentation, config);
    }

    @Test
    void testInitialization() {
        assertThat(manager.isEnabled()).isTrue();
        assertThat(manager.getConfig()).isEqualTo(config);
        assertThat(manager.getTransformer()).isNotNull();
        assertThat(manager.getAllMutationStates()).isEmpty();
    }

    @Test
    void testInitializationWithMutations() {
        MutationConfig.MutationRule rule = new MutationConfig.MutationRule();
        rule.type = "constant";
        MutationConfig.MutationRule.TargetInfo target = new MutationConfig.MutationRule.TargetInfo();
        target.className = "com.example.Calculator";
        target.methodName = "add";
        rule.target = target;

        config.getMutations().add(rule);

        MutationManager manager2 = new MutationManager(instrumentation, config);
        assertThat(manager2.getAllMutationStates()).hasSize(1);
    }

    @Test
    void testSetEnabled() {
        assertThat(manager.isEnabled()).isTrue();

        manager.setEnabled(false);
        assertThat(manager.isEnabled()).isFalse();

        manager.setEnabled(true);
        assertThat(manager.isEnabled()).isTrue();
    }

    @Test
    void testGetMutationStatesNoMatches() {
        List<MutationManager.MutationState> states = manager.getMutationStates("com.example.Calculator", "add");
        assertThat(states).isEmpty();
    }

    @Test
    void testGetMutationStatesSingleMatch() {
        MutationConfig.MutationRule rule = new MutationConfig.MutationRule();
        rule.type = "constant";
        MutationConfig.MutationRule.TargetInfo target = new MutationConfig.MutationRule.TargetInfo();
        target.className = "com.example.Calculator";
        target.methodName = "add";
        rule.target = target;

        manager.addMutation(rule);

        List<MutationManager.MutationState> states = manager.getMutationStates("com.example.Calculator", "add");
        assertThat(states).hasSize(1);
        assertThat(states.get(0).getRule()).isEqualTo(rule);
    }

    @Test
    void testGetMutationStatesMultipleMatches() {
        MutationConfig.MutationRule rule1 = new MutationConfig.MutationRule();
        rule1.type = "constant";
        MutationConfig.MutationRule.TargetInfo target1 = new MutationConfig.MutationRule.TargetInfo();
        target1.className = "com.example.Calculator";
        target1.methodName = "add";
        rule1.target = target1;

        MutationConfig.MutationRule rule2 = new MutationConfig.MutationRule();
        rule2.type = "operator";
        MutationConfig.MutationRule.TargetInfo target2 = new MutationConfig.MutationRule.TargetInfo();
        target2.className = "com.example.Calculator";
        target2.methodName = "add";
        rule2.target = target2;

        manager.addMutation(rule1);
        manager.addMutation(rule2);

        List<MutationManager.MutationState> states = manager.getMutationStates("com.example.Calculator", "add");
        assertThat(states).hasSize(2);
    }

    @Test
    void testGetMutationStatesWithWildcard() {
        MutationConfig.MutationRule rule = new MutationConfig.MutationRule();
        rule.type = "constant";
        MutationConfig.MutationRule.TargetInfo target = new MutationConfig.MutationRule.TargetInfo();
        target.className = "com.example.*";
        target.methodName = "get*";
        rule.target = target;

        manager.addMutation(rule);

        List<MutationManager.MutationState> states1 = manager.getMutationStates("com.example.Calculator", "getValue");
        assertThat(states1).hasSize(1);

        List<MutationManager.MutationState> states2 = manager.getMutationStates("com.example.Service", "getData");
        assertThat(states2).hasSize(1);

        List<MutationManager.MutationState> states3 = manager.getMutationStates("com.other.Calculator", "getValue");
        assertThat(states3).isEmpty();
    }

    @Test
    void testCaching() {
        MutationConfig.MutationRule rule = new MutationConfig.MutationRule();
        rule.type = "constant";
        MutationConfig.MutationRule.TargetInfo target = new MutationConfig.MutationRule.TargetInfo();
        target.className = "com.example.Calculator";
        target.methodName = "add";
        rule.target = target;

        manager.addMutation(rule);

        // First call - should cache
        List<MutationManager.MutationState> states1 = manager.getMutationStates("com.example.Calculator", "add");
        assertThat(states1).hasSize(1);

        // Second call - should use cache
        List<MutationManager.MutationState> states2 = manager.getMutationStates("com.example.Calculator", "add");
        assertThat(states2).hasSize(1);
        assertThat(states2.get(0)).isEqualTo(states1.get(0));
    }

    @Test
    void testCacheNotUsedForWildcards() {
        MutationConfig.MutationRule rule = new MutationConfig.MutationRule();
        rule.type = "constant";
        MutationConfig.MutationRule.TargetInfo target = new MutationConfig.MutationRule.TargetInfo();
        target.className = "com.example.*";
        target.methodName = "get*";
        rule.target = target;

        manager.addMutation(rule);

        // Wildcard rules should not be cached
        List<MutationManager.MutationState> states1 = manager.getMutationStates("com.example.Calculator", "getValue");
        List<MutationManager.MutationState> states2 = manager.getMutationStates("com.example.Service", "getData");

        assertThat(states1).hasSize(1);
        assertThat(states2).hasSize(1);
    }

    @Test
    void testAddMutation() {
        assertThat(manager.getAllMutationStates()).isEmpty();

        MutationConfig.MutationRule rule = new MutationConfig.MutationRule();
        rule.type = "constant";
        MutationConfig.MutationRule.TargetInfo target = new MutationConfig.MutationRule.TargetInfo();
        target.className = "com.example.Calculator";
        target.methodName = "add";
        rule.target = target;

        manager.addMutation(rule);

        assertThat(manager.getAllMutationStates()).hasSize(1);
    }

    @Test
    void testAddMutationClearsCache() {
        MutationConfig.MutationRule rule1 = new MutationConfig.MutationRule();
        rule1.type = "constant";
        MutationConfig.MutationRule.TargetInfo target1 = new MutationConfig.MutationRule.TargetInfo();
        target1.className = "com.example.Calculator";
        target1.methodName = "add";
        rule1.target = target1;

        manager.addMutation(rule1);
        manager.getMutationStates("com.example.Calculator", "add");

        MutationConfig.MutationRule rule2 = new MutationConfig.MutationRule();
        rule2.type = "operator";
        MutationConfig.MutationRule.TargetInfo target2 = new MutationConfig.MutationRule.TargetInfo();
        target2.className = "com.example.Calculator";
        target2.methodName = "add";
        rule2.target = target2;

        manager.addMutation(rule2);

        List<MutationManager.MutationState> states = manager.getMutationStates("com.example.Calculator", "add");
        assertThat(states).hasSize(2);
    }

    @Test
    void testRemoveMutation() {
        MutationConfig.MutationRule rule = new MutationConfig.MutationRule();
        rule.type = "constant";
        MutationConfig.MutationRule.TargetInfo target = new MutationConfig.MutationRule.TargetInfo();
        target.className = "com.example.Calculator";
        target.methodName = "add";
        rule.target = target;

        manager.addMutation(rule);
        assertThat(manager.getAllMutationStates()).hasSize(1);

        manager.removeMutation("com.example.Calculator", "add");
        assertThat(manager.getAllMutationStates()).isEmpty();
    }

    @Test
    void testRemoveMutationClearsCache() {
        MutationConfig.MutationRule rule = new MutationConfig.MutationRule();
        rule.type = "constant";
        MutationConfig.MutationRule.TargetInfo target = new MutationConfig.MutationRule.TargetInfo();
        target.className = "com.example.Calculator";
        target.methodName = "add";
        rule.target = target;

        manager.addMutation(rule);
        manager.getMutationStates("com.example.Calculator", "add");

        manager.removeMutation("com.example.Calculator", "add");

        List<MutationManager.MutationState> states = manager.getMutationStates("com.example.Calculator", "add");
        assertThat(states).isEmpty();
    }

    @Test
    void testMutationStateActive() {
        MutationConfig.MutationRule rule = new MutationConfig.MutationRule();
        rule.type = "constant";
        MutationConfig.MutationRule.TargetInfo target = new MutationConfig.MutationRule.TargetInfo();
        target.className = "com.example.Calculator";
        target.methodName = "add";
        rule.target = target;

        MutationManager.MutationState state = new MutationManager.MutationState(rule);

        assertThat(state.isActive()).isTrue();

        state.setActive(false);
        assertThat(state.isActive()).isFalse();

        state.setActive(true);
        assertThat(state.isActive()).isTrue();
    }

    @Test
    void testMutationStateMatches() {
        MutationConfig.MutationRule rule = new MutationConfig.MutationRule();
        rule.type = "constant";
        MutationConfig.MutationRule.TargetInfo target = new MutationConfig.MutationRule.TargetInfo();
        target.className = "com.example.Calculator";
        target.methodName = "add";
        rule.target = target;

        MutationManager.MutationState state = new MutationManager.MutationState(rule);

        assertThat(state.matches("com.example.Calculator", "add")).isTrue();
        assertThat(state.matches("com.example.Calculator", "subtract")).isFalse();
        assertThat(state.matches("com.other.Calculator", "add")).isFalse();
    }

    @Test
    void testMutationStateWithMultipleTargets() {
        MutationConfig.MutationRule rule = new MutationConfig.MutationRule();
        rule.type = "constant";

        MutationConfig.MutationRule.TargetInfo target1 = new MutationConfig.MutationRule.TargetInfo();
        target1.className = "com.example.Calculator";
        target1.methodName = "add";

        MutationConfig.MutationRule.TargetInfo target2 = new MutationConfig.MutationRule.TargetInfo();
        target2.className = "com.example.Calculator";
        target2.methodName = "subtract";

        rule.targets = new ArrayList<>();
        rule.targets.add(target1);
        rule.targets.add(target2);

        MutationManager.MutationState state = new MutationManager.MutationState(rule);

        assertThat(state.matches("com.example.Calculator", "add")).isTrue();
        assertThat(state.matches("com.example.Calculator", "subtract")).isTrue();
        assertThat(state.matches("com.example.Calculator", "multiply")).isFalse();
    }

    @Test
    void testGetAllMutationStates() {
        MutationConfig.MutationRule rule1 = new MutationConfig.MutationRule();
        rule1.type = "constant";
        MutationConfig.MutationRule.TargetInfo target1 = new MutationConfig.MutationRule.TargetInfo();
        target1.className = "com.example.Calculator";
        target1.methodName = "add";
        rule1.target = target1;

        MutationConfig.MutationRule rule2 = new MutationConfig.MutationRule();
        rule2.type = "operator";
        MutationConfig.MutationRule.TargetInfo target2 = new MutationConfig.MutationRule.TargetInfo();
        target2.className = "com.example.Calculator";
        target2.methodName = "multiply";
        rule2.target = target2;

        manager.addMutation(rule1);
        manager.addMutation(rule2);

        List<MutationManager.MutationState> states = manager.getAllMutationStates();
        assertThat(states).hasSize(2);
    }
}
