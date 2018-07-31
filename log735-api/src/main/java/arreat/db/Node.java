package arreat.db;

import java.io.Serializable;

public class Node implements Serializable {



    private int id;
    private String name;
    private String masterUser;

    public Node(){};
    public Node(int id, String name, String masterUser){
        this.id = id;
        this.name = name;
        this.masterUser = masterUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMasterUser() {
        return masterUser;
    }

    public void setMasterUser(String masterUser) {
        this.masterUser = masterUser;
    }

    public int getId() {
        return id;
    }

    public Node setId(int id) {
        this.id = id;
        return this;
    }


}
