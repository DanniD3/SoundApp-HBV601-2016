package thepack.soundapp.models;

public class Navigation {

    private String username;
    private boolean isUserValid;

    // App State Constants
    private int navState = 0;
    public static final int NAV_HOME = 0;
    public static final int NAV_SEARCH = 1;
    public static final int NAV_UPLOAD = 2;
    public static final int NAV_LOGIN = 3;

    public Navigation () {
        this.isUserValid = false;
    }

    public boolean isAtHome() {
        return navState == NAV_HOME;
    }

    public int getNavState() {
        return navState;
    }

    public void setNavState(int navState) {
        this.navState = navState;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void logout() {
        this.username = null;
    }
}
