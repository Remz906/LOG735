import arreat.impl.Client;
import arreat.impl.DataBase;
import arreat.impl.Message;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataBaseTest {
    private DataBase db = new DataBase();


    @Test
    public void message() {
        db.initDBhistoricDiscussion();
        Message message = new Message("discussion1", 1, "G4", "hello world");
        db.newMessage(message);
        assertTrue(message.equals(db.readMessages().get(0)));
    }

    @Test
    void client() {
        db.initDBClientIP();
        Client client = new Client("197.565.454.454",49444, "G4" );
        db.newClient(client);
        assertTrue(client.equals(db.readClients().get(0)));
    }
}