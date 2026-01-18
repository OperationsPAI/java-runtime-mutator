package io.chaosmesh.mutator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * HTTP control server for managing mutations at runtime.
 */
public class ControlServer {
    private static final Logger logger = Logger.getLogger(ControlServer.class.getName());

    private final MutationManager mutationManager;
    private final Server server;
    private final int port;

    public ControlServer(MutationManager mutationManager) {
        this.mutationManager = mutationManager;
        this.port = mutationManager.getConfig().getControlServerPort();
        this.server = new Server(port);

        setupServlets();
    }

    private void setupServlets() {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Status endpoint
        context.addServlet(new ServletHolder(new StatusServlet()), "/mutations/status");

        // Enable endpoint
        context.addServlet(new ServletHolder(new EnableServlet()), "/mutations/enable");

        // Disable endpoint
        context.addServlet(new ServletHolder(new DisableServlet()), "/mutations/disable");

        // Config endpoint
        context.addServlet(new ServletHolder(new ConfigServlet()), "/mutations/config");

        server.setHandler(context);
    }

    public void start() throws Exception {
        server.start();
        logger.info("Control server started on port " + port);
    }

    public void stop() throws Exception {
        server.stop();
        logger.info("Control server stopped");
    }

    public int getPort() {
        return port;
    }

    /**
     * Servlet for getting mutation status.
     */
    private class StatusServlet extends HttpServlet {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("application/json");

            Map<String, Object> status = new HashMap<>();
            status.put("enabled", mutationManager.isEnabled());
            status.put("mutations", mutationManager.getAllMutationStates().size());

            resp.getWriter().write(mapper.writeValueAsString(status));
        }
    }

    /**
     * Servlet for enabling mutations.
     */
    private class EnableServlet extends HttpServlet {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            mutationManager.setEnabled(true);
            resp.setContentType("application/json");

            Map<String, Object> result = new HashMap<>();
            result.put("status", "enabled");

            resp.getWriter().write(mapper.writeValueAsString(result));
        }
    }

    /**
     * Servlet for disabling mutations.
     */
    private class DisableServlet extends HttpServlet {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            mutationManager.setEnabled(false);
            resp.setContentType("application/json");

            Map<String, Object> result = new HashMap<>();
            result.put("status", "disabled");

            resp.getWriter().write(mapper.writeValueAsString(result));
        }
    }

    /**
     * Servlet for managing mutation configuration.
     */
    private class ConfigServlet extends HttpServlet {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("application/json");
            resp.getWriter().write(mapper.writeValueAsString(mutationManager.getAllMutationStates()));
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\": \"ok\"}");
        }
    }
}
