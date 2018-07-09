package arreat.api;


import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class DataBaseTest {

    @Test
    public void initDB() {
        DataBase db = null;
        try {
            db = new DataBase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        db.initDB();
        ArrayList inputElement = new ArrayList();
        inputElement.add("discussion");
        inputElement.add(42);
        inputElement.add("lalallal");
        inputElement.add("hello world");
        db.newMessage((String) inputElement.get(0), (int) inputElement.get(1), (String) inputElement.get(2), (String) inputElement.get(3));
        ArrayList outputElement = db.readMessages();
        outputElement.equals(inputElement);
        assertEquals(outputElement, inputElement);

    }
}