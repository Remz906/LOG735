package arreat.DB;

import arreat.impl.Message;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DatabaseH2 extends DatabaseSQL{
    static final private String url = "jdbc:h2:~/test";
    static final private String userID = "test";
    static final private String password = "test";
    static final private String driver = "org.h2.Driver";

    private DatabaseH2(){
        super(url,userID,password,driver);
    }

    @Override
    protected void initDB(){

    }

    /**
     * ClientIP
     */
    // Initialisation of clientIp database
    private void initDBClientIP() throws SQLException {
        Statement statement= null;
        // Creation of historicDiscussion table
        try{
            statement = connection.createStatement();
            statement.execute("" +
                    "CREATE TABLE clientIP (" +
                    "keyClient INTEGER PRIMARY KEY, " +
                    "Pseudo VARCHAR(20) NOT NULL, " +
                    "IP VARCHAR(20) NOT NULL," +
                    "port INTEGER NOT NULL)");
            System.out.println("clientIP table create");
        } catch (SQLException e) {
            System.out.println("code: "+e.getErrorCode());
            // the table already exist
            if(e.getErrorCode()==42101){
                System.out.println("the table clientIP already exist, we will drop it and create a new one");
                try {
                    statement = connection.createStatement();
                    statement.execute("DROP TABLE clientIP");
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                initDBClientIP();
            }
            else
                e.printStackTrace();
        }

        if (statement != null)
            statement.closeOnCompletion();
    }

    // Add new client to the database
    public void newClient(Client client){

        Statement statement = null;
        try {
            statement = connection.createStatement();
            int key = keyNumber("keyClient", "clientIP");
            statement.execute("INSERT INTO clientIP  VALUES(" + key + ", '" + client.getPseudo() + "', '" + client.getIp() + "'," + client.getPort() + ")");
        } catch (SQLException e) {
            System.out.println("ERROR newClient");
            e.printStackTrace();
        }

        try {
            if (statement != null)
                statement.closeOnCompletion();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Create Client object with the DB
    public ArrayList<Client> readClients() {
        Statement statement = null;
        ArrayList<Client> clients = new ArrayList();
        Client client;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM clientIP ");
            while (rs.next()) {
                client = new Client(rs.getString("IP"), rs.getInt("port"), rs.getString("pseudo"));
                clients.add(client);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (statement != null)
                statement.closeOnCompletion();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return clients;
    }


    /**
     * historicDiscussion
     */

    // Initialisation of the table historicDiscussion
    public void initDBhistoricDiscussion() throws SQLException {
        Statement statement= null;
        // Creation of historicDiscussion table
        try{
            statement = connection.createStatement();
            statement.execute("" +
                    "CREATE TABLE historicDiscussion (" +
                    "keyDiscussion INTEGER PRIMARY KEY, " +
                    "DiscussionName VARCHAR(20) NOT NULL, " +
                    "timestamp INTEGER NOT NULL," +
                    "senderPseudo VARCHAR(20) NOT NULL," +
                    "message varchar NOT NULL )");
            System.out.println("historicDiscussion create");

        } catch (SQLException e) {
            // the table already exist
            if(e.getErrorCode()==42101){
                System.out.println("the table historicDiscussion already exist, we will drop it and create a new one");
                try {
                    statement.execute("DROP TABLE historicDiscussion");
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                initDBhistoricDiscussion();
            }
            else
                e.printStackTrace();
        }

        if (statement != null)
            statement.closeOnCompletion();
    }

    // Add new message to the database
    public void newMessage(Message message) throws SQLException {
        Statement statement= null;
        try {
            statement = connection.createStatement();
            int key = keyNumber("keyDiscussion", "historicDiscussion");
            statement.execute("INSERT INTO HISTORICDISCUSSION  VALUES("+key+ ", '"+message.getDiscussionName()+"',"+message.getTimestamp()+", '"+message.getPseudo()+"', '"+message.getMessage()+"')");
        } catch (SQLException e) {
            System.out.println("ERROR newMessage");
            e.printStackTrace();
        }

        if (statement != null)
            statement.closeOnCompletion();
    }

    // Create Message object with the DB
    public ArrayList<Message> readMessages() throws SQLException {
        Statement statement= null;
        ArrayList<Message> messages = new ArrayList();
        Message message;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM HISTORICDISCUSSION ");
            while(rs.next()) {
                message = new Message(rs.getString("DISCUSSIONNAME"), rs.getInt("TIMESTAMP"), rs.getString("SENDERPSEUDO"), rs.getString("MESSAGE"));
                messages.add(message);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (statement != null)
            statement.closeOnCompletion();
        return messages;
    }

    /**
     * General tools
     */

}
