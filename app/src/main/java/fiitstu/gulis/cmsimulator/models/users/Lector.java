package fiitstu.gulis.cmsimulator.models.users;

public class Lector extends User {

    public Lector() {
    }


    public Lector(String username, String first_name, String last_name, Integer user_id, String auth_key) {
        super(username, first_name, last_name, user_id, auth_key);
    }

    public Lector(String username, String first_name, String last_name, Integer user_id, String password, String salt) {
        super(username, first_name, last_name, user_id, password, salt);
    }
}
