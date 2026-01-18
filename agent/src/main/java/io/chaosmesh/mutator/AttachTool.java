package io.chaosmesh.mutator;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.List;

/**
 * Tool for attaching the mutator agent to a running JVM process.
 *
 * Usage:
 *   java -cp agent/target/mutator-agent-1.0.0-SNAPSHOT.jar \
 *        io.chaosmesh.mutator.AttachTool <pid> <agent-jar-path> [config=path/to/config.yaml]
 */
public class AttachTool {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: AttachTool <pid|process-name> <agent-jar-path> [agent-args]");
            System.err.println("Example: AttachTool 12345 agent.jar config=mutation-config.yaml");
            System.exit(1);
        }

        String target = args[0];
        String agentPath = args[1];
        String agentArgs = args.length > 2 ? args[2] : "";

        try {
            String pid = findPid(target);
            if (pid == null) {
                System.err.println("Process not found: " + target);
                System.exit(1);
            }

            System.out.println("Attaching to process " + pid + "...");
            VirtualMachine vm = VirtualMachine.attach(pid);

            System.out.println("Loading agent: " + agentPath);
            System.out.println("Agent args: " + agentArgs);
            vm.loadAgent(agentPath, agentArgs);

            vm.detach();
            System.out.println("Agent attached successfully!");

        } catch (Exception e) {
            System.err.println("Failed to attach agent: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String findPid(String target) {
        // If target is already a PID, return it
        if (target.matches("\\d+")) {
            return target;
        }

        // Otherwise, search for process by name
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        for (VirtualMachineDescriptor vmd : vms) {
            if (vmd.displayName().contains(target)) {
                System.out.println("Found process: " + vmd.displayName() + " (PID: " + vmd.id() + ")");
                return vmd.id();
            }
        }

        return null;
    }
}
