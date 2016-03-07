package thepack.soundapp.models;

public class Navigation {

    private String username;
    private boolean isUserValid;

    public Navigation () {
        this.isUserValid = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void logout() {
        this.username = "";
    }
}
