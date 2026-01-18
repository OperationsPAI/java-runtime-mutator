package io.chaosmesh.mutator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class MutationConfigTest {

    @Test
    void testParseEmptyArgs() throws IOException {
        MutationConfig config = MutationConfig.parse("");
        assertThat(config.getMutations()).isEmpty();
        assertThat(config.getControlServerPort()).isEqualTo(8080);
        assertThat(config.isEnabled()).isTrue();
    }

    @Test
    void testParseNullArgs() throws IOException {
        MutationConfig config = MutationConfig.parse(null);
        assertThat(config.getMutations()).isEmpty();
        assertThat(config.getControlServerPort()).isEqualTo(8080);
        assertThat(config.isEnabled()).isTrue();
    }

    @Test
    void testParsePortOverride() throws IOException {
        MutationConfig config = MutationConfig.parse("port=9090");
        assertThat(config.getControlServerPort()).isEqualTo(9090);
    }

    @Test
    void testParseEnabledOverride() throws IOException {
        MutationConfig config = MutationConfig.parse("enabled=false");
        assertThat(config.isEnabled()).isFalse();
    }

    @Test
    void testLoadFromYamlFile(@TempDir Path tempDir) throws IOException {
        String yaml = "controlServerPort: 9090\n" +
            "enabled: true\n" +
            "mutations:\n" +
            "  - type: constant\n" +
            "    target:\n" +
            "      className: com.example.Calculator\n" +
            "      methodName: add\n" +
            "    mutation:\n" +
            "      strategy: zero\n";

        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        MutationConfig config = MutationConfig.parse("config=" + configFile.toString());

        assertThat(config.getControlServerPort()).isEqualTo(9090);
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getMutations()).hasSize(1);
        assertThat(config.getMutations().get(0).type).isEqualTo("constant");
    }

    @Test
    void testLoadFromYamlWithMultipleTargets(@TempDir Path tempDir) throws IOException {
        String yaml = "mutations:\n" +
            "  - type: constant\n" +
            "    targets:\n" +
            "      - className: com.example.Calculator\n" +
            "        methodName: add\n" +
            "      - className: com.example.Calculator\n" +
            "        methodName: subtract\n" +
            "    mutation:\n" +
            "      strategy: zero\n";

        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        MutationConfig config = MutationConfig.parse("config=" + configFile.toString());

        assertThat(config.getMutations()).hasSize(1);
        assertThat(config.getMutations().get(0).getAllTargets()).hasSize(2);
    }

    @Test
    void testTargetMatching() {
        MutationConfig.MutationRule.TargetInfo target = new MutationConfig.MutationRule.TargetInfo();
        target.className = "com.example.Calculator";
        target.methodName = "add";

        assertThat(target.matches("com.example.Calculator", "add")).isTrue();
        assertThat(target.matches("com.example.Calculator", "subtract")).isFalse();
        assertThat(target.matches("com.other.Calculator", "add")).isFalse();
    }

    @Test
    void testWildcardMatching() {
        MutationConfig.MutationRule.TargetInfo target = new MutationConfig.MutationRule.TargetInfo();
        target.className = "com.example.*";
        target.methodName = "get*";

        assertThat(target.matches("com.example.Calculator", "getValue")).isTrue();
        assertThat(target.matches("com.example.Service", "getData")).isTrue();
        assertThat(target.matches("com.other.Calculator", "getValue")).isFalse();
        assertThat(target.matches("com.example.Calculator", "setValue")).isFalse();
    }

    @Test
    void testQuestionMarkWildcard() {
        MutationConfig.MutationRule.TargetInfo target = new MutationConfig.MutationRule.TargetInfo();
        target.className = "com.example.Calculator";
        target.methodName = "get?alue";

        assertThat(target.matches("com.example.Calculator", "getValue")).isTrue();
        assertThat(target.matches("com.example.Calculator", "getXalue")).isTrue();
        assertThat(target.matches("com.example.Calculator", "getABvalue")).isFalse();
    }

    @Test
    void testNullPatternMatching() {
        MutationConfig.MutationRule.TargetInfo target = new MutationConfig.MutationRule.TargetInfo();
        target.className = null;
        target.methodName = "add";

        assertThat(target.matches("com.example.Calculator", "add")).isFalse();
    }

    @Test
    void testSetEnabled() {
        MutationConfig config = new MutationConfig();
        assertThat(config.isEnabled()).isTrue();

        config.setEnabled(false);
        assertThat(config.isEnabled()).isFalse();

        config.setEnabled(true);
        assertThat(config.isEnabled()).isTrue();
    }

    @Test
    void testToString() {
        MutationConfig config = new MutationConfig();
        String str = config.toString();

        assertThat(str).contains("mutations=0");
        assertThat(str).contains("port=8080");
        assertThat(str).contains("enabled=true");
    }

    @Test
    void testGetAllTargetsWithBothSingleAndMultiple() {
        MutationConfig.MutationRule rule = new MutationConfig.MutationRule();

        MutationConfig.MutationRule.TargetInfo target1 = new MutationConfig.MutationRule.TargetInfo();
        target1.className = "com.example.A";
        target1.methodName = "methodA";
        rule.target = target1;

        MutationConfig.MutationRule.TargetInfo target2 = new MutationConfig.MutationRule.TargetInfo();
        target2.className = "com.example.B";
        target2.methodName = "methodB";

        MutationConfig.MutationRule.TargetInfo target3 = new MutationConfig.MutationRule.TargetInfo();
        target3.className = "com.example.C";
        target3.methodName = "methodC";

        rule.targets = java.util.Arrays.asList(target2, target3);

        assertThat(rule.getAllTargets()).hasSize(3);
    }
}
