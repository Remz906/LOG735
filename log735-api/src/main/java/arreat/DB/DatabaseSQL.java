package arreat.DB;

import arreat.impl.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

public class DatabaseSQL {

    private String databaseURL;
    private String username;
    private String password;
    private String dbDriver;
    private String MAX_POOL = "250";

    protected Connection connection;
    private Properties properties;

    public DatabaseSQL(String databaseURL, String username, String password, String dbDriver) {
    this.databaseURL = databaseURL;
    this.username = username;
    this.password = password;
    this.dbDriver = dbDriver;
        try {
            connect();
        } catch (Exception e) {
            try {
                initDB();
                connect();
            } catch (Exception e2) {
                System.out.println("The connection with de database can't be established");
                e.printStackTrace();
            }
        }
    }

    protected void initDB() {


    }

    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            properties.setProperty("user", username);
            properties.setProperty("password", password);
            properties.setProperty("MaxPooledStatements", MAX_POOL);
        }
        return properties;
    }

    public Connection connect() {
        if (connection == null) {
            try {
                Class.forName(this.dbDriver);
                connection = DriverManager.getConnection(databaseURL, getProperties());
                System.out.println("DB connected");
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
                System.out.println("DB disconnected");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public ResultSet sendQuery(String query) throws SQLException {
        ResultSet rs = null;
        if (connection != null) {
            Statement st = connection.createStatement();
            rs = st.executeQuery(query);
            st.closeOnCompletion();
        }
        return rs;
    }


    private void initCltTable() {
        try {
            sendQuery("CREATE TABLE client (" +
                    "keyClient INTEGER PRIMARY KEY, " +
                    "Pseudo VARCHAR(20) NOT NULL, " +
                    "IP VARCHAR(20) NOT NULL," +
                    "port INTEGER NOT NULL," +
                    "pwd VARCHAR(20))");
            System.out.println("client table create");
        } catch (SQLException e) {
            System.out.println("code: "+e.getErrorCode());
            // the table already exist
            if(e.getErrorCode()==42101){
                System.out.println("the table clientIP already exist");
            }else
                e.printStackTrace();
        }

    }

    // Add new client to the database
    public void newClient(Client client){
        try {
            Statement statement = connection.createStatement();
            int key = keyNumber("keyClient", "client");
            statement.execute("INSERT INTO clientIP  VALUES("+key+ ", '"+client.getPseudo()+"', '"+client.getIp()+"',"+client.getPort()+"','"+client.getPwd()+")");
            statement.closeOnCompletion();
        } catch (SQLException e) {
            System.out.println("ERROR newClient");
            e.printStackTrace();
        }
    }

    //return the new keynumber to add for the new row
    int keyNumber(String keyName, String tableName) throws SQLException {
        int newKeyNumber = -1;
        ResultSet rs;
        Statement statement= null;
        try {
            statement = connection.createStatement();
            // if we have no row we return the first keyNumber 1
            rs = statement.executeQuery("SELECT COUNT(*) FROM " + tableName );
            rs.next();
            if(rs.getInt("COUNT(*)")==0)
                newKeyNumber = 1;

                // if one or more row exist, we return de biggest key + 1
            else{
                rs = statement.executeQuery("SELECT * FROM " + tableName +" ORDER BY " + keyName+ " DESC");
                rs.next();
                newKeyNumber = rs.getInt(keyName) + 1;
            }
        } catch (SQLException e) {
            System.out.println("ERROR keyNumber");
            e.printStackTrace();
        }
        if (statement != null)
            statement.closeOnCompletion();
        return newKeyNumber;
    }

    public ArrayList<Client> readClients(){
        ArrayList<Client> clients = new ArrayList();
        Client client;
        try {
            ResultSet rs = sendQuery("SELECT * FROM clientIP ");
            while (rs.next()) {
                client = new Client(rs.getString("IP"), rs.getInt("port"), rs.getString("pseudo"), rs.getString("pwd"));
                clients.add(client);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    public void removeClt(){

    }



}
