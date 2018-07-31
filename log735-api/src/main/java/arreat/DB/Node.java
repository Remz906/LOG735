package arreat.DB;

import java.io.Serializable;

public class Node implements Serializable {



    private int id;
    private String name;
    private String masterPseudo;

    public Node(){};
    public Node(int id, String name, String masterPseudo){
        this.id = id;
        this.name = name;
        this.masterPseudo = masterPseudo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMasterPseudo() {
        return masterPseudo;
    }

    public void setMasterPseudo(String masterPseudo) {
        this.masterPseudo = masterPseudo;
    }

    public int getId() {
        return id;
    }

    public Node setId(int id) {
        this.id = id;
        return this;
    }


}
