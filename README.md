# Java Runtime Mutator

A Java agent for runtime bytecode mutation enabling fault injection in microservices. Supports constant, operator, and return value mutations without requiring code changes.

## Features

- **Constant Mutation**: Modify integer, float, string, and boolean constants at runtime
- **Operator Mutation**: Replace arithmetic and logical operators (+ → -, * → /, etc.)
- **Return Value Mutation**: Mutate method return values (null, zero, false, empty, exception, custom)
- **Runtime Control**: HTTP API for enabling/disabling mutations without restart
- **Multi-Location Targeting**: Apply mutations to multiple classes/methods with wildcard support
- **Zero Code Changes**: Works via Java agent attachment, no application modifications needed

## Quick Start

### Build

```bash
mvn clean package
```

### Run Tests

```bash
# Run all unit tests (138 tests)
mvn test

# Run with coverage report
mvn test jacoco:report
```

### Run Example

```bash
# Terminal 1: Run test application with agent
java -javaagent:agent/target/mutator-agent-1.0.0-SNAPSHOT.jar=config=examples/mutation-config.yaml \
     -jar examples/simple-app/target/simple-app-1.0.0-SNAPSHOT.jar

# Terminal 2: Control mutations via API
curl http://localhost:8080/mutations/status
curl -X POST http://localhost:8080/mutations/disable
curl -X POST http://localhost:8080/mutations/enable
```

## Configuration

Create a `mutation-config.yaml`:

```yaml
mutations:
  - type: constant
    target:
      className: com.example.UserService
      methodName: getMaxRetries
    mutation:
      from: "3"
      to: "0"

  - type: operator
    target:
      className: com.example.Calculator
      methodName: add
    mutation:
      strategy: add_to_sub

  - type: string
    target:
      className: com.example.MessageService
      methodName: getWelcomeMessage
    mutation:
      strategy: empty

controlServerPort: 8080
enabled: true
```

## Mutation Types

### Constant Mutations

Strategies: `zero`, `one`, `minus_one`, `max`, `min`, `negate`, `increment`, `decrement`, `random`

For strings: `empty`, `null`, `reverse`, `uppercase`, `lowercase`, `random`

### Operator Mutations

- `add_to_sub`: + → -
- `sub_to_add`: - → +
- `mul_to_div`: * → /
- `div_to_mul`: / → *

### Return Value Mutations

- `null`: Return null for object types
- `zero`: Return 0 for numeric types
- `false`: Return false for boolean types
- `empty`: Return empty string for String types
- `exception`: Throw exception (configurable exception class)
- `custom`: Return custom value

## Testing

### Run Tests

```bash
# Run all unit tests
mvn test

# Run with coverage report
mvn test jacoco:report

# View coverage report
open agent/target/site/jacoco/index.html
```

### Test Coverage

- 138 unit tests covering all mutation types
- 85%+ code coverage across all components
- Tests for MutationConfig, MutationManager, ConstantMutator, OperatorMutator, ReturnValueMutator

## Control API

- `GET /mutations/status` - Get mutation status
- `POST /mutations/enable` - Enable all mutations
- `POST /mutations/disable` - Disable all mutations
- `GET /mutations/config` - Get current configuration
- `POST /mutations/config` - Update configuration

## Architecture

- **MutatorAgent**: Java agent entry point (premain/agentmain)
- **MutationManager**: Coordinates mutation state and bytecode transformation
- **MutatorTransformer**: ClassFileTransformer using ASM for bytecode manipulation
- **ConstantMutator**: ASM MethodVisitor for constant mutations
- **OperatorMutator**: ASM MethodVisitor for operator mutations
- **ReturnValueMutator**: ASM MethodVisitor for return value mutations
- **ControlServer**: Embedded Jetty server for HTTP API

## Future Integration

Chaos Mesh integration is planned for a future release and will require:
- Go-based Kubernetes controller implementation
- Integration with chaos-experiment framework
- Custom CRD for Kubernetes orchestration

## License

Apache 2.0

