package fiitstu.gulis.cmsimulator.models;

public class Student extends User {
    private int group_id;

    public Student() {

    }

    public Student(String username, String first_name, String last_name, Integer user_id, String auth_key, int group_id) {
        super(username, first_name, last_name, user_id, auth_key);
        this.group_id = group_id;
    }

    public Student(Integer user_id, String username, String first_name, String last_name, String password, int group_id) {
        super(user_id, username, first_name, last_name, password);
        this.group_id = group_id;
    }

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }
}
