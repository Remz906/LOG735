package arreat.api;

import java.sql.*;
import java.util.ArrayList;

public class DataBase {
    private Connection connection = DriverManager.getConnection("jdbc:h2:~/test","test", "test");
    private Statement statement = connection.createStatement();

    public DataBase() throws SQLException {
    }

    // Initialisation of tables
    public void initDB(){
        System.out.println("initDB");
        // Creation of historicDiscussion table
        try {
            try {
                Class.forName("org.h2.Driver");
                try{
                    //statement.execute("DROP TABLE lapluie");
                    statement.execute("" +
                            "CREATE TABLE historicDiscussion (" +
                                "DiscussionName VARCHAR(20), " +
                                "timestamp INTEGER," +
                                "senderPseudo VARCHAR(20)," +
                                "message varchar )");
                    System.out.println("historicDiscussion create");
                } catch (SQLException e) {
                    // the table already exist
                    e.printStackTrace();
                    statement.execute("DROP TABLE historicDiscussion");
                    initDB();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            System.out.println("2");
            e.printStackTrace();
        }
    }

    // Add new message to the database
    public void newMessage(String DiscussionName,int timestamp, String pseudo, String message){
        System.out.println("new message");
        try {
            statement.execute("INSERT INTO HISTORICDISCUSSION  VALUES('"+DiscussionName+"',"+timestamp+", '"+pseudo+"', '"+message+"')");
            //connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Display all the data of the table HISTORICDISCUSSION
    public ArrayList readMessages(){
        System.out.println("readMessage");
        ArrayList dbElement = new ArrayList();
        try {
            ResultSet rs = statement.executeQuery("SELECT * FROM HISTORICDISCUSSION ");
            while(rs.next()) {
                dbElement.add(rs.getString("DISCUSSIONNAME"));
                dbElement.add(rs.getInt("TIMESTAMP"));
                dbElement.add(rs.getString("SENDERPSEUDO"));
                dbElement.add(rs.getString("MESSAGE"));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            dbElement.add("error");
        }
        return dbElement;
    }

}
