# Java Runtime Mutator - CLAUDE.md

## Documentation

This project maintains two core documents:
- **CLAUDE.md** (this file) - Technical context for AI assistants, including architecture, implementation details, and development workflow
- **README.md** - User-facing quick start guide and usage instructions

## Project Overview

Java Runtime Mutator is a bytecode mutation engine for fault injection in Java applications. It enables runtime modification of Java code (constants, operators, strings) without requiring application restart or code changes.

**Status**: Core implementation complete and tested locally. Ready for feature expansion.

## Architecture

### Three-Layer Design

```
Java Agent (MutatorAgent.java)
    ↓
Mutation Manager (MutationManager.java)
    ↓
Bytecode Transformer (MutatorTransformer.java)
    ├→ ConstantMutator (ASM MethodVisitor)
    ├→ OperatorMutator (ASM MethodVisitor)
    └→ StringMutator (via ConstantMutator)
```

### Key Components

1. **MutatorAgent**: Java agent entry point (premain/agentmain modes)
2. **MutationManager**: Coordinates mutation state and bytecode transformation
3. **MutatorTransformer**: ClassFileTransformer using ASM for bytecode manipulation
4. **ConstantMutator**: ASM MethodVisitor for constant mutations (integers, floats, strings)
5. **OperatorMutator**: ASM MethodVisitor for operator mutations (arithmetic, logical)
6. **ControlServer**: Embedded Jetty server for HTTP API (optional, graceful failure)
7. **MutationConfig**: YAML configuration loader

## Implemented Features

### Mutation Types (Completed)

1. **Constant Mutation** ✅
   - Integer: zero, one, minus_one, max, min, negate, increment, decrement, random
   - Float/Double: zero, one, nan, infinity, neg_infinity, negate, random
   - String: empty, null, reverse, uppercase, lowercase, random
   - Type conversion: String config values auto-convert to numeric types

2. **Operator Mutation** ✅
   - Arithmetic: add_to_sub, sub_to_add, mul_to_div, div_to_mul
   - Logical: and_to_or, or_to_and, not_negate

3. **String Mutation** ✅
   - Integrated with ConstantMutator
   - Supports all string strategies

### Configuration

- YAML-based configuration (`mutation-config.yaml`)
- Per-mutation target specification (className, methodName)
- Global control server port setting
- Enable/disable flag

### Control API

- `GET /mutations/status` - Get mutation status
- `POST /mutations/enable` - Enable all mutations
- `POST /mutations/disable` - Disable all mutations
- `GET /mutations/config` - Get current configuration
- `POST /mutations/config` - Update configuration

### Testing

- ✅ Local test application (SimpleCalculator)
- ✅ All three mutation types verified working
- ✅ Test results documented in TEST_RESULTS.md

## Known Issues & Fixes

### Issue 1: ControlServer Port Binding Failure
**Problem**: ControlServer startup failure caused entire agent initialization to fail
**Solution**: Moved transformer registration before ControlServer startup, made ControlServer optional
**Status**: ✅ Fixed

### Issue 2: String-to-Number Type Conversion
**Problem**: Configuration values are strings ("100", "0") but constants are integers (100, 0)
**Solution**: Added type conversion logic in ConstantMutator.shouldMutate() and getMutatedValue()
**Status**: ✅ Fixed

### Issue 3: String Mutation Not Applied
**Problem**: String mutation type was not handled in MutatorTransformer
**Solution**: Added "string" type handling alongside "constant" type
**Status**: ✅ Fixed

## Implemented Features (Continued)

### 4. Multi-Location Mutation Support ✅
   - **Targets Array**: Apply same mutation to multiple class/method combinations
   - **Wildcard Matching**: Use `*` and `?` patterns in className and methodName
   - **Backward Compatible**: Single `target` field still supported
   - **Pattern Matching**: Regex-based matching with caching for performance

### 5. Return Value Mutation ✅
   - **Strategies**: null, zero, false, empty, exception, custom
   - **Supported Return Types**: Primitives (int, long, float, double, boolean), Objects (String, etc.)
   - **Configuration Examples**:
     ```yaml
     # Return zero for numeric methods
     - type: return
       target:
         className: com.example.Calculator
         methodName: multiply
       mutation:
         strategy: zero

     # Return false for boolean methods
     - type: return
       target:
         className: com.example.Validator
         methodName: isValid
       mutation:
         strategy: false

     # Return null for object methods
     - type: return
       target:
         className: com.example.Service
         methodName: getData
       mutation:
         strategy: null

     # Return empty string
     - type: return
       target:
         className: com.example.Service
         methodName: getMessage
       mutation:
         strategy: empty

     # Throw exception
     - type: return
       target:
         className: com.example.Service
         methodName: process
       mutation:
         strategy: exception
         value: "java.lang.RuntimeException"

     # Return custom value
     - type: return
       target:
         className: com.example.Calculator
         methodName: add
       mutation:
         strategy: custom
         value: "999"
     ```
   - **Test Results**: ✅ Verified working (multiply(10,5) returns 0)

## Planned Features

### Phase 2: Additional Mutation Types (Next Priority)
- [ ] Method Call Interception - Intercept and modify method calls
- [ ] Condition Mutation - Negate/modify conditional logic
- [ ] Field Value Mutation - Modify object field values
- [ ] Timing Mutation - Add delays/timeouts
- [ ] Exception Mutation - Modify exception handling
- [ ] Loop Mutation - Modify loop behavior
- [ ] Concurrency Mutation - Remove synchronization
- [ ] Resource Leak Mutation - Skip resource cleanup
- [ ] Array/Collection Mutation - Modify collection contents
- [ ] Null Pointer Mutation - Inject null values
- [ ] Logging Mutation - Suppress/modify logging

### Phase 3: Chaos Mesh Integration (Deferred)
- Chaos Mesh integration will be implemented in a separate phase
- Requires Go-based Kubernetes controller implementation
- Will integrate with chaos-experiment framework
- Planned for future release

### Phase 4: AegisLab Integration (Deferred)
- [ ] Add JVMMutator task type
- [ ] Implement consumer handler
- [ ] Add API endpoints
- [ ] Test end-to-end workflow

## Comparison with Chaos Mesh JVMChaos

### Chaos Mesh JVMChaos (Byteman-based)
- **Strengths**: Mature, stable, supports 7 fault types (latency, return, exception, stress, gc, mysql, ruleData)
- **Limitations**: No fine-grained bytecode mutation, single rule per JVMChaos, no multi-location support, complex Byteman syntax

### Java Runtime Mutator (ASM-based)
- **Strengths**: Fine-grained mutation, simple YAML config, flexible bytecode manipulation, multi-location support with wildcards
- **Limitations**: Currently fewer mutation types than JVMChaos, but designed for extensibility

**Complementary**: Both can coexist - JVMChaos for high-level faults, Mutator for code-level mutations

## Build & Test

### Build
```bash
mvn clean package -DskipTests
```

### Run Tests
```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=MutationConfigTest

# Run tests with verbose output
mvn test -X
```

### Test Coverage
- **MutationConfig**: 90%+ coverage - YAML parsing, validation, pattern matching
- **MutationManager**: 85%+ coverage - State management, caching, enable/disable
- **ConstantMutator**: 90%+ coverage - All mutation strategies (integer, float, double, string)
- **OperatorMutator**: 90%+ coverage - All operator mutations (arithmetic, logical)
- **ReturnValueMutator**: 90%+ coverage - All return value strategies (null, zero, false, empty, exception, custom)
- **Overall**: 138 unit tests, all passing

### Test Results
```
Tests run: 138, Failures: 0, Errors: 0, Skipped: 0
```

### Coverage Report
After running `mvn test jacoco:report`, view the coverage report at:
```
agent/target/site/jacoco/index.html
```

## Key Files

### Core Implementation
- `agent/src/main/java/io/chaosmesh/mutator/MutatorAgent.java` - Agent entry point
- `agent/src/main/java/io/chaosmesh/mutator/MutationManager.java` - Mutation state management
- `agent/src/main/java/io/chaosmesh/mutator/MutatorTransformer.java` - Bytecode transformation
- `agent/src/main/java/io/chaosmesh/mutator/transformer/ConstantMutator.java` - Constant mutations
- `agent/src/main/java/io/chaosmesh/mutator/transformer/OperatorMutator.java` - Operator mutations
- `agent/src/main/java/io/chaosmesh/mutator/ControlServer.java` - HTTP control API

### Configuration & Examples
- `examples/mutation-config.yaml` - Configuration example
- `examples/simple-app/src/main/java/io/chaosmesh/examples/SimpleCalculator.java` - Test application

### Documentation
- `README.md` - Quick start guide
- `TEST_RESULTS.md` - Test results and verification
- `MUTATION_IDEAS.md` - Brainstorm of additional mutation types
- `CHAOS_MESH_JVM_ANALYSIS.md` - Analysis of Chaos Mesh JVM implementation

## Development Workflow

### Adding a New Mutation Type

1. Create new MethodVisitor class in `agent/src/main/java/io/chaosmesh/mutator/transformer/`
2. Implement mutation logic by overriding appropriate visit* methods
3. Register in MutatorTransformer.visitMethod()
4. Add configuration support in MutationConfig
5. Create test cases in examples/
6. Update documentation

### Testing New Features

1. Update `examples/mutation-config.yaml` with new mutation
2. Update `examples/simple-app/SimpleCalculator.java` with test methods
3. Build: `mvn clean package -DskipTests`
4. Run: `java -javaagent:agent/target/mutator-agent-1.0.0-SNAPSHOT.jar=config=examples/mutation-config.yaml -jar examples/simple-app/target/simple-app-1.0.0-SNAPSHOT.jar`
5. Verify output shows expected mutations

## Performance Considerations

- **Agent Overhead**: < 1 second startup time
- **Bytecode Transformation**: Minimal overhead (only target classes transformed)
- **Runtime Impact**: Depends on mutation type
  - Constant mutations: No overhead (compile-time replacement)
  - Operator mutations: Minimal (instruction replacement)
  - Timing mutations: Significant (Thread.sleep)
- **Memory**: Acceptable for embedded agent

## Security Notes

- Agent validates mutation targets (prevents arbitrary code execution)
- Control API should be restricted in production (no authentication currently)
- Mutations are applied at bytecode level (safe from tampering)
- Consider adding authentication/authorization for control API

## Next Steps

1. **Immediate**: Implement multi-location mutation support
2. **Short-term**: Add Return Value and Method Call Interception mutations
3. **Medium-term**: Implement Chaos Mesh integration
4. **Long-term**: Add remaining mutation types and production hardening

## References

- Chaos Mesh JVMChaos: `/home/nn/workspace/proj/chaos-mesh/api/v1alpha1/jvmchaos_types.go`
- Chaos Mesh Controller: `/home/nn/workspace/proj/chaos-mesh/controllers/chaosimpl/jvmchaos/impl.go`
- Chaos Mesh ChaosDaemon: `/home/nn/workspace/proj/chaos-mesh/pkg/chaosdaemon/jvm_server.go`
- ASM Documentation: https://asm.ow2.io/
- Byteman Documentation: https://downloads.jboss.org/byteman/4.0.14/byteman-programmers-guide.html
