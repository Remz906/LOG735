package arreat.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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

    // Not working
    public void readMessages(){
        System.out.println("readMessage");
        try {
            statement.execute("SELECT * FROM HISTORICDISCUSSION ");
            //System.out.println(statement.getResultSet().getInt("timestamp"));
        } catch (SQLException e) {
            System.out.println("nop");
            e.printStackTrace();
        }
    }

}
