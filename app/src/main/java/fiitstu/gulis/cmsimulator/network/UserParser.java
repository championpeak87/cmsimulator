package fiitstu.gulis.cmsimulator.network;

import fiitstu.gulis.cmsimulator.models.User;
import org.json.JSONException;
import org.json.JSONObject;

public class UserParser {
    private final static String USERNAME_KEY = "username";
    private final static String FIRST_NAME_KEY = "first_name";
    private final static String LAST_NAME_KEY = "last_name";
    private final static String AUTHKEY_KEY = "auth_key";
    private final static String USER_ID_KEY = "user_id";

    public User getUserFromJson(String in) throws JSONException
    {
        JSONObject reader = new JSONObject(in);

        User parsedUser;
        String username, firstName, lastName, authkey;
        int userid;

        username = reader.getString(USERNAME_KEY);
        firstName = reader.getString(FIRST_NAME_KEY);
        lastName = reader.getString(LAST_NAME_KEY);
        authkey = reader.getString(AUTHKEY_KEY);
        userid = reader.getInt(USER_ID_KEY);

        parsedUser = new User(username,
                              firstName,
                              lastName,
                              userid,
                              authkey);

        return parsedUser;
    }

}
