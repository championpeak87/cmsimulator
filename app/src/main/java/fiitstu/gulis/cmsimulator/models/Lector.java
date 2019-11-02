package fiitstu.gulis.cmsimulator.models;

public class Lector extends User {

    public Lector() {
    }

    public Lector(String username, String first_name, String last_name, Integer user_id, String auth_key) {
        super(username, first_name, last_name, user_id, auth_key);
    }

    public Lector(Integer user_id, String username, String first_name, String last_name, String password) {
        super(user_id, username, first_name, last_name, password);
    }
}
