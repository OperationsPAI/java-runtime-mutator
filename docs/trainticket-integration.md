# TrainTicket Integration Guide

## Overview

This guide explains how to integrate the Java Runtime Mutator agent with TrainTicket microservices.

## Prerequisites

- TrainTicket deployed in Kubernetes
- Chaos Mesh installed in the cluster
- Java Runtime Mutator agent built

## Step 1: Build the Mutator Agent

```bash
cd java-runtime-mutator/agent
mvn clean package
```

This produces `agent/target/mutator-agent.jar`.

## Step 2: Update TrainTicket Service Dockerfiles

For each TrainTicket service you want to enable mutations on, modify the Dockerfile:

```dockerfile
# Add mutator agent to the image
COPY mutator-agent.jar /app/mutator-agent.jar

# Configure Java agent
ENV JAVA_TOOL_OPTIONS="-javaagent:/app/mutator-agent.jar=port=8080,enabled=true"
```

See `docs/trainticket-dockerfile-example.md` for a complete example.

## Step 3: Create Mutation Configuration

Create a ConfigMap with mutation rules:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: mutation-config
  namespace: ts
data:
  mutation-config.yaml: |
    mutations:
      - type: constant
        target:
          className: org.services.user.UserServiceImpl
          methodName: getMaxRetries
        mutation:
          from: "3"
          to: "0"
    controlServerPort: 8080
    enabled: false  # Start disabled, enable via Chaos Mesh
```

## Step 4: Mount Configuration in Deployment

Update the service deployment to mount the ConfigMap:

```yaml
spec:
  template:
    spec:
      containers:
      - name: ts-user-service
        volumeMounts:
        - name: mutation-config
          mountPath: /app/mutation-config.yaml
          subPath: mutation-config.yaml
      volumes:
      - name: mutation-config
        configMap:
          name: mutation-config
```

## Step 5: Deploy Updated Services

```bash
# Rebuild images with mutator agent
cd train-ticket
skaffold build --default-repo=<your-registry>

# Deploy with Helm
helm upgrade ts manifests/helm/generic_service -n ts \
  --set global.image.tag=<new-tag>
```

## Step 6: Apply JVMMutatorChaos

Create a chaos experiment:

```yaml
apiVersion: chaos-mesh.org/v1alpha1
kind: JVMMutatorChaos
metadata:
  name: mutate-user-service
  namespace: ts
spec:
  selector:
    namespaces:
      - ts
    labelSelectors:
      app: ts-user-service
  mode: one
  duration: "5m"
  mutation:
    type: constant
    target:
      class: org.services.user.UserServiceImpl
      method: getMaxRetries
    config:
      from: "3"
      to: "0"
```

Apply it:

```bash
kubectl apply -f jvm-mutator-chaos.yaml
```

## Step 7: Verify Mutations

Check the control API:

```bash
# Port-forward to the service
kubectl port-forward -n ts svc/ts-user-service 8080:8080

# Check mutation status
curl http://localhost:8080/mutations/status

# Enable mutations manually (if needed)
curl -X POST http://localhost:8080/mutations/enable

# Disable mutations
curl -X POST http://localhost:8080/mutations/disable
```

## Troubleshooting

### Agent Not Loading

Check the pod logs:

```bash
kubectl logs -n ts <pod-name> | grep MutatorAgent
```

Expected output:
```
MutatorAgent starting in premain mode
Loaded mutation config: MutationConfig{mutations=1, port=8080, enabled=true}
Control server started on port 8080
MutatorAgent initialized successfully
```

### Mutations Not Applied

1. Verify agent is loaded: Check for "MutatorAgent initialized" in logs
2. Check mutation status: `curl http://localhost:8080/mutations/status`
3. Verify target class/method exists in the service
4. Check Chaos Mesh controller logs: `kubectl logs -n chaos-mesh chaos-controller-manager-xxx`

### Performance Impact

Monitor the overhead:

```bash
# Check CPU/memory usage
kubectl top pod -n ts <pod-name>

# Compare with baseline (no agent)
```

Expected overhead: < 5% CPU, < 50MB memory

## Integration with AegisLab

The mutator integrates with AegisLab for automated fault injection:

```python
from rcabench.openapi import AegisLabClient

client = AegisLabClient(base_url="http://aegislab-api:8080")

# Submit JVM mutator task
task = client.submit_fault_injection(
    type="jvm_mutator",
    target={
        "namespace": "ts",
        "service": "ts-user-service",
        "class": "org.services.user.UserServiceImpl",
        "method": "getMaxRetries"
    },
    mutation={
        "type": "constant",
        "from": "3",
        "to": "0"
    },
    duration="5m"
)

print(f"Task submitted: {task.id}")
```

## Next Steps

- Explore different mutation types (operator, string)
- Integrate with Pandora for intelligent fault scheduling
- Use RCABench Platform to evaluate RCA algorithms under mutations
