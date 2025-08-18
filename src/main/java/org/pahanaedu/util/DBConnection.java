package org.pahanaedu.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

        private static final String URL = "jdbc:mysql://localhost:3306/pahanaedu";
        private static final String USER = "root"; // change to your MySQL username
        private static final String PASSWORD = "Mysql@123"; // change to your MySQL password

        public static Connection getConnection() throws SQLException {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                return DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new SQLException(e);
            }
        }


}
