package fiitstu.gulis.cmsimulator.models;

import fiitstu.gulis.cmsimulator.network.users.PasswordManager;

public class User {
    public static final String USER_TYPE_KEY = "user_type";
    public static final String USERNAME_KEY = "username";
    public static final String FIRST_NAME_KEY = "first_name";
    public static final String LAST_NAME_KEY = "last_name";
    public static final String AUTHKEY_KEY = "auth_key";
    public static final String GROUP_ID_KEY = "group_id";
    public static final String USER_ID_KEY = "user_id";

    private String username;
    private String first_name;
    private String last_name;
    private Integer user_id;
    private String auth_key;

    public enum user_type {
        lector,
        student,
        admin
    }

    public User()
    {}

    public User(String username, String first_name, String last_name, Integer user_id, String auth_key) {
        this.username = username;
        this.first_name = first_name;
        this.last_name = last_name;
        this.user_id = user_id;
        this.auth_key = auth_key;
    }

    public User(Integer user_id, String username, String first_name, String last_name, String password) {
        this.username = username;
        this.first_name = first_name;
        this.last_name = last_name;
        this.user_id = user_id;

        PasswordManager passwordManager = new PasswordManager();
        String _authkey = passwordManager.getAuthkey(password);

        this.auth_key = _authkey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getAuth_key() {
        return auth_key;
    }

    public void setAuth_key(String auth_key) {
        this.auth_key = auth_key;
    }
}
