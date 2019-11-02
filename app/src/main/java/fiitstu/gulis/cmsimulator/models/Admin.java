package fiitstu.gulis.cmsimulator.models;

public class Admin extends User{

    public Admin() {
    }

    public Admin(String username, String first_name, String last_name, Integer user_id, String auth_key) {
        super(username, first_name, last_name, user_id, auth_key);
    }

    public Admin(Integer user_id, String username, String first_name, String last_name, String password) {
        super(user_id, username, first_name, last_name, password);
    }
}
