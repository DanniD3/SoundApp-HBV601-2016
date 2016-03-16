package thepack.soundapp.entities;

public class User {

    private long id;
    private String name;
    private String pw;

    public User() {
    }

    public User(long id, String name, String pw) {
        this.id = id;
        this.name = name;
        this.pw = pw;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }
}
