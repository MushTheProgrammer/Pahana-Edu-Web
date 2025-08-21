package org.pahanaedu.controller;

import org.pahanaedu.service.AuthService;
import org.pahanaedu.service.impl.AuthServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "AuthServlet", urlPatterns = {"/api/auth/*"})
public class AuthServlet extends HttpServlet {
    private final AuthService authService;

    public AuthServlet() {
        this.authService = new AuthServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        
        if ("/status".equals(pathInfo)) {
            handleAuthStatus(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if ("/login".equals(pathInfo)) {
            handleLogin(req, resp);
        } else if ("/logout".equals(pathInfo)) {
            handleLogout(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    private void handleAuthStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        boolean isAuthenticated = session != null && session.getAttribute("user") != null;
        
        resp.setContentType("application/json");
        resp.getWriter().write(String.format("{\"authenticated\": %b}", isAuthenticated));
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        
        boolean isAuthenticated = authService.authenticate(username, password);
        
        resp.setContentType("application/json");
        if (isAuthenticated) {
            HttpSession session = req.getSession();
            session.setAttribute("user", username);
            session.setMaxInactiveInterval(30 * 60); // 30 minutes
            
            resp.getWriter().write("{\"success\": true, \"redirect\": \"dashboard\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"success\": false, \"message\": \"Invalid username or password\"}");
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.setContentType("application/json");
        resp.getWriter().write("{\"success\": true, \"redirect\": \"login\"}");
    }
}
