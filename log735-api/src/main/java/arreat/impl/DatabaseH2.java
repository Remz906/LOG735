package arreat.impl;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseH2 {
    private Connection connection;
    private Statement statement;
    private String url = "jdbc:h2:~/test";
    private String userID = "test";
    private String password = "test";
    public DatabaseH2() {
        try {
            connection = DriverManager.getConnection(url,userID, password);
            statement = connection.createStatement();
        } catch (SQLException e) {
            System.out.println("The connection with de database can't be established");
            e.printStackTrace();
        }

        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    /**
     * ClientIP
     */
    // Initialisation of clientIp database
    public void initDBClientIP(){
        // Creation of historicDiscussion table
        try{
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
                    statement.execute("DROP TABLE clientIP");
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                initDBClientIP();
            }
            else
                e.printStackTrace();
        }
    }

    // Add new client to the database
    public void newClient(Client client){
        try {
            int key = keyNumber("keyClient", "clientIP");
            statement.execute("INSERT INTO clientIP  VALUES("+key+ ", '"+client.getPseudo()+"', '"+client.getIp()+"',"+client.getPort()+")");
        } catch (SQLException e) {
            System.out.println("ERROR newClient");
            e.printStackTrace();
        }
    }

    // Create Client object with the DB
    public ArrayList<Client> readClients(){
        ArrayList<Client> clients = new ArrayList();
        Client client;
        try {
            ResultSet rs = statement.executeQuery("SELECT * FROM clientIP ");
            while(rs.next()) {
                client = new Client(rs.getString("IP"), rs.getInt("port"), rs.getString("pseudo"));
                clients.add(client);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }


    /**
     * historicDiscussion
     */

    // Initialisation of the table historicDiscussion
    public void initDBhistoricDiscussion(){
        // Creation of historicDiscussion table
        try{
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
    }

    // Add new message to the database
    public void newMessage(Message message){
        try {
            int key = keyNumber("keyDiscussion", "historicDiscussion");
            statement.execute("INSERT INTO HISTORICDISCUSSION  VALUES("+key+ ", '"+message.getDiscussionName()+"',"+message.getTimestamp()+", '"+message.getPseudo()+"', '"+message.getMessage()+"')");
        } catch (SQLException e) {
            System.out.println("ERROR newMessage");
            e.printStackTrace();
        }
    }



    // Create Message object with the DB
    public ArrayList<Message> readMessages(){
        ArrayList<Message> messages = new ArrayList();
        Message message;
        try {
            ResultSet rs = statement.executeQuery("SELECT * FROM HISTORICDISCUSSION ");
            while(rs.next()) {
                message = new Message(rs.getString("DISCUSSIONNAME"), rs.getInt("TIMESTAMP"), rs.getString("SENDERPSEUDO"), rs.getString("MESSAGE"));
                messages.add(message);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }


    /**
     * General tools
     */

    //return the new keynumber to add for the new row
    public int keyNumber(String keyName, String tableName){
        int newKeyNumber = -1;
        ResultSet rs;
        try {
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
        return newKeyNumber;
    }

}
