package fiitstu.gulis.cmsimulator.network.users;

import fiitstu.gulis.cmsimulator.models.users.Admin;
import fiitstu.gulis.cmsimulator.models.users.Lector;
import fiitstu.gulis.cmsimulator.models.users.Student;
import fiitstu.gulis.cmsimulator.models.users.User;
import org.json.JSONException;
import org.json.JSONObject;

public class UserParser {
    private final static String USERNAME_KEY = "username";
    private final static String FIRST_NAME_KEY = "first_name";
    private final static String LAST_NAME_KEY = "last_name";
    private final static String AUTHKEY_KEY = "password_hash";
    private final static String USER_ID_KEY = "user_id";
    private final static String USER_TYPE_KEY = "type";

    private final static String USER_TYPE_LECTOR = "lector";
    private final static String USER_TYPE_STUDENT = "student";
    private final static String USER_TYPE_ADMIN = "admin";

    private final static int NULL_USER_GROUP = -1;

    public User getUserFromJson(String in) throws JSONException {
        JSONObject reader = new JSONObject(in);

        User parsedUser;
        String username, firstName, lastName, authkey, user_type;
        int userid;

        username = reader.getString(USERNAME_KEY);
        firstName = reader.getString(FIRST_NAME_KEY);
        lastName = reader.getString(LAST_NAME_KEY);
        authkey = reader.getString(AUTHKEY_KEY);
        userid = reader.getInt(USER_ID_KEY);
        user_type = reader.getString(USER_TYPE_KEY);

        switch (user_type) {
            case USER_TYPE_LECTOR:
                parsedUser = new Lector(
                        username,
                        firstName,
                        lastName,
                        userid,
                        authkey);
                break;
            case USER_TYPE_ADMIN:
                parsedUser = new Admin(
                        username,
                        firstName,
                        lastName,
                        userid,
                        authkey);
                break;
            case USER_TYPE_STUDENT:
                parsedUser = new Student(
                        username,
                        firstName,
                        lastName,
                        userid,
                        authkey,
                        NULL_USER_GROUP);
                break;
            default:
                parsedUser = null;
                break;
        }

        return parsedUser;
    }

}
