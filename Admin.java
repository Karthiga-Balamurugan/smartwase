
public class Admin extends User {

    public Admin(String name, String username) {
        super(name, username);
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    public String manageWaste() {
        return "Admin can manage dustbins, vehicles and collection routes.";
    }
}