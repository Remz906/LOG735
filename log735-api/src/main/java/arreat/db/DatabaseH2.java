package arreat.db;

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

    public DatabaseH2(){
        super(url,userID,password,driver);
    }

    @Override
    protected void initDB(){

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
            statement.execute("INSERT INTO HISTORICDISCUSSION  VALUES(" + message.getDiscussionName()+"',"+message.getTimestamp()+", '"+message.getPseudo()+"', '"+message.getMessage()+ ")");
        } catch (SQLException e) {
            System.out.println("ERROR newMessage");
            e.printStackTrace();
        }

        if (statement != null)
            statement.closeOnCompletion();
    }

    // Create UDPMessage object with the db
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
