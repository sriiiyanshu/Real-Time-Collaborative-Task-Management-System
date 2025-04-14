package com.taskmanager.filter;

import java.io.IOException;
import java.util.Date;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Filter to log all requests to the application
 * Useful for debugging and auditing purposes
 */
@WebFilter(urlPatterns = {"/*"})
public class RequestLoggingFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // Get request details
        String ipAddress = httpRequest.getRemoteAddr();
        String url = httpRequest.getRequestURL().toString();
        String method = httpRequest.getMethod();
        Date requestTime = new Date();
        
        // Get session information (if exists)
        HttpSession session = httpRequest.getSession(false);
        String username = "anonymous";
        if (session != null && session.getAttribute("username") != null) {
            username = (String) session.getAttribute("username");
        }
        
        // Log the request
        logRequest(ipAddress, url, method, username, requestTime);
        
        // Calculate request processing time
        long startTime = System.currentTimeMillis();
        
        try {
            // Pass the request along the filter chain
            chain.doFilter(request, response);
        } finally {
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            
            // Log request completion
            logRequestCompletion(url, method, processingTime);
        }
    }
    
    @Override
    public void destroy() {
        // Cleanup code if needed
    }
    
    /**
     * Log request details
     */
    private void logRequest(String ipAddress, String url, String method, String username, Date requestTime) {
        System.out.println(String.format("[%s] %s request to %s by %s from IP %s", 
                requestTime.toString(), method, url, username, ipAddress));
    }
    
    /**
     * Log request completion details
     */
    private void logRequestCompletion(String url, String method, long processingTime) {
        System.out.println(String.format("[%s] %s request completed in %d ms", 
                method, url, processingTime));
    }
}