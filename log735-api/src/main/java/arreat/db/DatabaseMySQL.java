package arreat.db;


import java.sql.*;

public class DatabaseMySQL extends DatabaseSQL{

    private static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DATABASE_URL= "jdbc:mysql://localhost:3306/javabase";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    public DatabaseMySQL() {
        super(DATABASE_URL, USERNAME, PASSWORD, DATABASE_DRIVER);
    }


}
