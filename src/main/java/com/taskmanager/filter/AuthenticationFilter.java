package com.taskmanager.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.taskmanager.util.AuthUtil;

/**
 * Filter to check if user is authenticated
 * Protects resources that require authentication
 */
@WebFilter(urlPatterns = {
    "/dashboard/*", 
    "/projects/*", 
    "/tasks/*", 
    "/profile/*", 
    "/files/*",
    "/chat/*",
    "/analytics/*"
})
public class AuthenticationFilter implements Filter {
    
    private AuthUtil authUtil;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        authUtil = new AuthUtil();
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Check if user is already logged in via session
        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);
        
        // If not logged in, check for "remember me" cookie
        if (!isLoggedIn) {
            isLoggedIn = authUtil.checkRememberMeCookie(httpRequest, httpResponse, session);
        }
        
        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        // Special handling for POST requests to /projects and /projects/
        if ((requestURI.equals(httpRequest.getContextPath() + "/projects") || 
             requestURI.equals(httpRequest.getContextPath() + "/projects/")) && 
            "POST".equalsIgnoreCase(method)) {
            
            // For project creation/editing, ensure we have a valid session
            if (session == null) {
                session = httpRequest.getSession(true);
            }
            
            if (isLoggedIn) {
                // User is authenticated, proceed with the request
                chain.doFilter(request, response);
                return;
            }
        }
        
        if (isLoggedIn) {
            // User is authenticated, proceed with the request
            chain.doFilter(request, response);
        } else {
            // User is not authenticated, redirect to login page
            httpRequest.setAttribute("errorMessage", "You must be logged in to access this resource");
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
        }
    }
    
    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}