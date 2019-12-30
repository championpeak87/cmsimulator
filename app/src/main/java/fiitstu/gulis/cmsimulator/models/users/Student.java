package fiitstu.gulis.cmsimulator.models.users;

public class Student extends User {
    public Student() {

    }

    public Student(String username, String first_name, String last_name, Integer user_id, String auth_key) {
        super(username, first_name, last_name, user_id, auth_key);
    }

    public Student(String username, String first_name, String last_name, Integer user_id, String password, String salt) {
        super(username, first_name, last_name, user_id, password, salt);
    }
}
