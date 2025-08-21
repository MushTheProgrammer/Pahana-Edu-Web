package org.pahanaedu.service.impl;

import org.pahanaedu.dao.UserDAO;
import org.pahanaedu.dao.impl.UserDAOImpl;
import org.pahanaedu.service.AuthService;

public class AuthServiceImpl implements AuthService {
    private final UserDAO userDAO;

    public AuthServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }

    @Override
    public boolean authenticate(String username, String password) {
        return userDAO.validateUser(username, password);
    }
}
