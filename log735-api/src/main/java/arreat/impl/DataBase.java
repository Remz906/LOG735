package arreat.impl;

import java.sql.*;
import java.util.ArrayList;

public class DataBase {
    private Connection connection = DriverManager.getConnection("jdbc:h2:~/test","test", "test");
    private Statement statement = connection.createStatement();

    public DataBase() throws SQLException {
    }

    // Initialisation of tables
    public void initDB(){

        /**
         * CLIENT
         */
        // Creation of historicDiscussion table
        try {
            try {
                Class.forName("org.h2.Driver");
                try{
                    statement.execute("" +
                            "CREATE TABLE historicDiscussion (" +
                                "keyDiscussion INTEGER PRIMARY KEY, " +
                                "DiscussionName VARCHAR(20) NOT NULL, " +
                                "timestamp INTEGER NOT NULL," +
                                "senderPseudo VARCHAR(20) NOT NULL," +
                                "message varchar NOT NULL )");
                    System.out.println("historicDiscussion create");

                    statement.execute("" +
                            "CREATE TABLE clientIP (" +
                            "keyClient INTEGER PRIMARY KEY, " +
                            "Pseudo VARCHAR(20) NOT NULL, " +
                            "IP VARCHAR(20) NOT NULL," +
                            "port INTEGER NOT NULL)");
                    System.out.println("clientIP table create");
                } catch (SQLException e) {
                    // the table already exist
                    System.out.println("the table historicDiscussion or clientIP already exist, we will drop them and create a new one");
                    statement.execute("DROP TABLE historicDiscussion");
                    statement.execute("DROP TABLE clientIP");
                    initDB();
                }


            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Add new message to the database
    public void newMessage(String DiscussionName,int timestamp, String pseudo, String message){
        try {
            int key = keyNumber("keyDiscussion", "historicDiscussion");
            statement.execute("INSERT INTO HISTORICDISCUSSION  VALUES("+key+ ", '"+DiscussionName+"',"+timestamp+", '"+pseudo+"', '"+message+"')");
            //connection.close();
        } catch (SQLException e) {
            System.out.println("ERROR newMessage");
            e.printStackTrace();
        }
    }


    // Display all the data of the table HISTORICDISCUSSION
    public ArrayList readMessages(){
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
