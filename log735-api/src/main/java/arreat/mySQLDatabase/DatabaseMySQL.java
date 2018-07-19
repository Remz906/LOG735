package arreat.mySQLDatabase;


import java.sql.*;
import java.util.Properties;

public class DatabaseMySQL {

    private static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DATABASE_URL_MAIN = "jdbc:mysql://localhost:3306";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String MAX_POOL = "250";

    private final String DATABASE_URL;
    private Connection connection;
    private Properties properties;

    public DatabaseMySQL(String databaseName) {
        DATABASE_URL = DATABASE_URL_MAIN + "/" + databaseName;
        try {
            connect();
        }catch(Exception e)
        {
            try {
                initDB();
                connect();
            }catch (Exception e2)
            {
                e.printStackTrace();
            }

        }

    }

    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            properties.setProperty("user", USERNAME);
            properties.setProperty("password", PASSWORD);
            properties.setProperty("MaxPooledStatements", MAX_POOL);
        }
        return properties;
    }

    public Connection connect() {
        if (connection == null) {
            try {
                Class.forName(DATABASE_DRIVER);
                connection = DriverManager.getConnection(DATABASE_URL, getProperties());
                System.out.println("MySQL DB connected");
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("MySQL DB disconnected");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public ResultSet sendQuery(String query){
        ResultSet rs = null;
        if (connection != null){
            try{
                Statement st = connection.createStatement();
                rs = st.executeQuery(query);
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        return rs;
    }

    private void initDB(){
        

    }

}
