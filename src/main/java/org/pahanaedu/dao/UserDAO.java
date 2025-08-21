package org.pahanaedu.dao;

public interface UserDAO {
    boolean validateUser(String username, String password);
}
