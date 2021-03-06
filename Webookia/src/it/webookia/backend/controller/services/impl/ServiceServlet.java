package it.webookia.backend.controller.services.impl;

import it.webookia.backend.controller.resources.exception.ResourceErrorType;
import it.webookia.backend.controller.resources.exception.ResourceException;
import it.webookia.backend.utils.servlets.Context;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datanucleus.util.StringUtils;

/**
 * Manages to invoke services using Http requests and responses
 * 
 */
public class ServiceServlet extends HttpServlet {
    /**
     * Defines the context where a request is made
     */
    public static final String CONTEXT = "CONTEXT";
    /**
     * Defines the error that occurs
     */
    public static final String ERROR = "ERROR";

    private static final long serialVersionUID = -6364544534456443497L;
    private static final Pattern requestPattern = Pattern
        .compile("/([a-z|A-Z]+)/([a-z|A-Z]*).*");

    private Context context;

    private Map<String, Service> getServices;
    private Map<String, Service> postServices;
    private Service defaultGetService;
    private Service defaultPostService;

    /**
     * Class constructor
     * 
     * @param context
     *            - is the context in which a request is made
     */
    protected ServiceServlet(Context context) {
        this.getServices = new HashMap<String, Service>();
        this.postServices = new HashMap<String, Service>();
        this.context = context;
    }

    /**
     * Sets the default behavior when the passed {@service} is invoked,
     * associating the {@verb} to the {@service}.
     * 
     * @param verb
     *            - indicates which kind of action is required (GET or POST)
     * @param service
     *            - it's a service availables
     */
    protected final void registerDefaultService(Verb verb, Service service) {
        if (verb.equals(Verb.GET)) {
            this.defaultGetService = service;
        } else {
            this.defaultPostService = service;
        }
    }

    /**
     * Registers a service, associating a {@service} to an {@action} and to a
     * {@verb}.
     * 
     * @param verb
     *            - indicates which kind of action is allowed to the user, hence
     *            if retrieving data or showing data (GET or POST)
     * @param action
     *            - indicates the action a user wants to perform with that
     *            service
     * @param service
     *            - indicates a service
     */
    protected final void registerService(Verb verb, String action,
            Service service) {
        Map<String, Service> target =
            verb.equals(Verb.GET) ? getServices : postServices;

        target.put(action, service);
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        runService(Verb.GET, req, resp);
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        runService(Verb.POST, req, resp);
    }

    private void runService(Verb verb, HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {
        String actionName;
        ServiceContext serviceContext = new ServiceContext(req, resp);

        try {
            actionName = getActionName(req);
        } catch (IllegalArgumentException e) {
            ResourceException ex =
                new ResourceException(ResourceErrorType.NOT_FOUND, e);
            serviceContext.sendError(ex);
            return;
        }

        req.setAttribute(CONTEXT, context);

        if (StringUtils.isEmpty(actionName)) {
            Service def =
                verb.equals(Verb.GET) ? defaultGetService : defaultPostService;

            if (def == null) {
                String message =
                    "No default service for " + context.getContextName();
                ResourceException ex =
                    new ResourceException(ResourceErrorType.NOT_FOUND, message);
                serviceContext.sendError(ex);
                return;
            }

            def.service(serviceContext);
            return;
        }

        Map<String, Service> servicePool =
            verb.equals(Verb.GET) ? getServices : postServices;

        if (!servicePool.keySet().contains(actionName)) {
            String message =
                "No service found for action "
                    + actionName
                    + " in context "
                    + context.getContextName();
            ResourceException ex =
                new ResourceException(ResourceErrorType.NOT_FOUND, message);
            serviceContext.sendError(ex);
            return;
        }

        servicePool.get(actionName).service(serviceContext);
    }

    private String getActionName(HttpServletRequest request)
            throws IllegalArgumentException {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relativePath = requestURI.replace(contextPath, "");

        Matcher m = requestPattern.matcher(relativePath);

        if (!m.matches()) {
            throw new IllegalArgumentException("Bad request: " + relativePath);
        }

        String context = (m.group(1) == null ? "" : m.group(1));
        String action = (m.group(2) == null ? "" : m.group(2));

        if (!context.equals(this.context.getContextName())) {
            throw new IllegalArgumentException("Service context error: "
                + context);
        }

        return action;
    }
}
