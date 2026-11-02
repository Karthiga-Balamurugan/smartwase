
public class Worker extends User {

    public Worker(String name, String username) {
        super(name, username);
    }

    @Override
    public String getRole() {
        return "Worker";
    }

    public String collectWaste() {
        return "Worker can collect waste from assigned dustbins.";
    }
}