
public abstract class User {

    private String name;
    private String username;

    public User(String name, String username) {
        this.name = name;
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public abstract String getRole();

    public String getWelcomeMessage() {
        return "Welcome, " + name + "!";
    }
}