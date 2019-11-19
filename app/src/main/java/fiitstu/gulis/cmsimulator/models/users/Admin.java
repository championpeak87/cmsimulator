package fiitstu.gulis.cmsimulator.models.users;

public class Admin extends User{


    public Admin(String username, String first_name, String last_name, Integer user_id, String auth_key) {
        super(username, first_name, last_name, user_id, auth_key);
    }

    public Admin() {

    }
}
