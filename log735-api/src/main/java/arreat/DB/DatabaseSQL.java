package arreat.DB;

import arreat.impl.Message;

import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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



    /********************* Client ************************/

    private void initCltTable() {
        String sql = "CREATE TABLE Client (" +
                "id INTEGER NOT NULL AUTO_INCREMENT, " +
                "pseudo VARCHAR(255) NOT NULL, " +
                "ip VARCHAR(255) NOT NULL," +
                "port INTEGER NOT NULL," +
                "pwd VARCHAR(20)m " +
                "PRIMARY KEY(id))";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.executeUpdate();
            ps.closeOnCompletion();
            System.out.println("Client table create");
        } catch (SQLException e) {
            System.out.println("code: "+e.getErrorCode());
            // the table already exist
            if(e.getErrorCode()==42101){
                System.out.println("the table Client already exist");
            }else
                e.printStackTrace();
        }

    }

    // Add new client to the database
    public void newClient(Client client){
        String sql = "INSERT INTO Client VALUES(" + client.getPseudo()+", "+client.getIp()+", "+client.getPort()+", "+client.getPwd()+")";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.closeOnCompletion();
        } catch (SQLException e) {
            System.out.println("ERROR newClient");
            e.printStackTrace();
        }
    }

    public List<Client> getAllClients(){
        String sql = "SELECT * FROM Client";
        List<Client> list = new LinkedList<>();
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                list.add(getClientFromRs(rs));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Client getClientByPseudo(String pseudo){
        String sql = "SELECT FROM Client WHERE pseudo =" + pseudo;
        Client clt = null;
        try{
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            clt = getClientFromRs(rs);

            statement.closeOnCompletion();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clt;
    }

    public void deleteClt(Client client){
        String sql = "DELETE FROM Client WHERE id = ?";
        try (Connection conn = this.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // set the corresponding param
            ps.setInt(1, client.getId());
            // execute the delete statement
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public void updateClt(Client client){
        String sql = "";
        Client clt = getClientByPseudo(client.getPseudo());


    }

    /********************* Node ************************/




    /********************* RS mappers ************************/

    private Client getClientFromRs(ResultSet rs) throws SQLException {
        return  new Client(rs.getInt("id"), rs.getString("ip"),
                rs.getInt("port"), rs.getString("pseudo"), rs.getString("pwd"));
    }

    private Node getNodeFromRs(ResultSet rs) throws  SQLException{
        return new Node(rs.getInt("id"), rs.getString("name"), rs.getString("masterPseudo"));
    }


}
