package org.pahanaedu.dao.impl;

import org.pahanaedu.dao.UserDAO;
import org.pahanaedu.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOImpl implements UserDAO {
    private static final String VALIDATE_USER = "SELECT username FROM users WHERE BINARY username = ? AND password = ?";

    @Override
    public boolean validateUser(String username, String password) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(VALIDATE_USER)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // If there's a matching user, return true
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
