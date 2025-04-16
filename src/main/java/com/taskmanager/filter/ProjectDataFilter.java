package com.taskmanager.filter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

@WebFilter(urlPatterns = {"/project.jsp", "/projects/*"})
public class ProjectDataFilter implements Filter {
    
    // Define project statuses
    private static final List<String> PROJECT_STATUSES = Arrays.asList(
        "Not Started", "Planning", "Active", "On Hold", "Completed", "Cancelled"
    );
    
    // Define task statuses
    private static final List<String> TASK_STATUSES = Arrays.asList(
        "To Do", "In Progress", "Under Review", "Completed", "Blocked"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // Add project statuses to request
        request.setAttribute("projectStatuses", PROJECT_STATUSES);
        
        // Add task statuses to request
        request.setAttribute("taskStatuses", TASK_STATUSES);
        
        // Continue with the filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}