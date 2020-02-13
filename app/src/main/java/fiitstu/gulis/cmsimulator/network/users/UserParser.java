package fiitstu.gulis.cmsimulator.network.users;

import fiitstu.gulis.cmsimulator.models.users.Admin;
import fiitstu.gulis.cmsimulator.models.users.Lector;
import fiitstu.gulis.cmsimulator.models.users.Student;
import fiitstu.gulis.cmsimulator.models.users.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserParser {
    private final static String USERNAME_KEY = "username";
    private final static String FIRST_NAME_KEY = "first_name";
    private final static String LAST_NAME_KEY = "last_name";
    private final static String AUTHKEY_KEY = "password_hash";
    private final static String USER_ID_KEY = "user_id";
    private final static String USER_TYPE_KEY = "user_type";
    private final static String SALT_KEY = "salt";

    private final static String USER_TYPE_LECTOR = "lector";
    private final static String USER_TYPE_STUDENT = "student";
    private final static String USER_TYPE_ADMIN = "admin";

    private final static int NULL_USER_GROUP = -1;

    public String getSaltFromJson(String in) throws JSONException {
        JSONObject reader = new JSONObject(in);
        String salt = reader.getString(SALT_KEY);

        return salt;
    }

    public User getUserFromJson(String in) throws JSONException {
        JSONObject reader = new JSONObject(in);

        User parsedUser;
        String username, firstName, lastName, authkey, user_type;
        int userid;

        username = reader.getString(USERNAME_KEY);
        firstName = reader.getString(FIRST_NAME_KEY);
        lastName = reader.getString(LAST_NAME_KEY);
        authkey = reader.getString(AUTHKEY_KEY);
        if (authkey.length() % 2 != 0)
            authkey += '0';
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
                        authkey);
                break;
            default:
                parsedUser = null;
                break;
        }


        return parsedUser;
    }

    public User getUserFromJson(JSONObject reader) throws JSONException {
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
                        authkey);
                break;
            default:
                parsedUser = null;
                break;
        }


        return parsedUser;
    }

    public List<User> getListOfUsers(String in) throws JSONException {
        JSONArray jsonArray = new JSONArray(in);
        final int arrayLength = jsonArray.length();
        List<User> listOfUsers = new ArrayList<>();
        JSONObject currentUserJSON;
        User currentUser;
        for (int i = 0; i < arrayLength; i++) {
            currentUserJSON = jsonArray.getJSONObject(i);
            currentUser = this.getUserFromJson(currentUserJSON);
            listOfUsers.add(currentUser);
        }

        if (listOfUsers.size() > 0)
            return listOfUsers;
        else
            return null;
    }

}
