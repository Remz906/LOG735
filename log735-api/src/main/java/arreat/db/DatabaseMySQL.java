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

    @Override
    protected void initDB(){

    }


    private void initMasterNodeTable(){
        try {
            sendQuery("CREATE TABLE masterNode (" +
                    "name VARCHAR(20) NOT NULL, " +
                    "masterPseudo VARCHAR(20) NOT NULL)");
            System.out.println("masterNode table created");
        } catch (SQLException e) {
            System.out.println("code: "+e.getErrorCode());
            // the table already exist
            if(e.getErrorCode()==42101){
                System.out.println("the table already exist");
            }else
                e.printStackTrace();
        }
    }


    // Add new client to the database
    public void addNode(Node node){
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO node  VALUES("+ node.getName() +","+node.getMasterUser()+")");
            statement.executeUpdate();
            statement.closeOnCompletion();
        } catch (SQLException e) {
            System.out.println("ERROR addNode");
            e.printStackTrace();
        }
    }


    public void removeNode(Node node){
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM masterNode WHERE name ="+ node.getName() +")");
            statement.executeUpdate();
            statement.closeOnCompletion();
        } catch (SQLException e) {
            System.out.println("ERROR rm Node");
            e.printStackTrace();
        }
    }



}
