package fiitstu.gulis.cmsimulator.network;

import android.net.Uri;
import fiitstu.gulis.cmsimulator.models.users.User;
import fiitstu.gulis.cmsimulator.network.users.PasswordManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlManager {

    private final String URI = "http://192.168.0.102:3000";

    // PATHS
    private final String LOGIN_PATH = "/api/login";
    private final String CHANGE_PASSWORD_PATH = "/api/user/changePassword";
    private final String ADD_NEW_USER_PATH = "/api/user/signup";
    private final String GET_ALL_USERS_PATH = "/api/user/getUsers";
    private final String SEARCH_USER_PATH = "/api/user/getUsersFiltered";

    // LOGIN QUERY KEYS
    private final String USERNAME_QUERY_KEY = "username";
    private final String AUTHKEY_QUERY_KEY = "auth_key";

    // SIGN UP QUERY KEYS
    private final String FIRST_NAME_KEY = "first_name";
    private final String LAST_NAME_KEY = "last_name";
    private final String USER_TYPE_KEY = "user_type";

    // CHANGE PASSWORD QUERY KEYS
    private final String USER_ID_KEY = "user_id";
    private final String NEW_AUTH_KEY = "new_auth_key";
    private final String OLD_AUTH_KEY = "auth_key";


    public URL getLoginUrl(String username, String password)
    {
        PasswordManager passwordManager = new PasswordManager();
        String authkey = passwordManager.getAuthkey(password);

        Uri builtUri = Uri.parse(URI + LOGIN_PATH).buildUpon()
                .appendQueryParameter(USERNAME_QUERY_KEY, username)
                .appendQueryParameter(AUTHKEY_QUERY_KEY, authkey)
                .build();

        URL url = null;

        try
        {
            url = new URL(builtUri.toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return url;
    }

    public URL getLoginUrlWithAuthkey(String username, String authkey)
    {
        Uri builtUri = Uri.parse(URI + LOGIN_PATH).buildUpon()
                .appendQueryParameter(USERNAME_QUERY_KEY, username)
                .appendQueryParameter(AUTHKEY_QUERY_KEY, authkey)
                .build();

        URL url = null;

        try
        {
            url = new URL(builtUri.toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return url;
    }

    public URL getAllUsersUrl(String authkey)
    {
        Uri builtUri = Uri.parse(URI + GET_ALL_USERS_PATH).buildUpon()
                .appendQueryParameter(AUTHKEY_QUERY_KEY, authkey)
                .build();

        URL url = null;

        try
        {
            url = new URL(builtUri.toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return url;
    }

    public URL getSearchedUser(String authkey, String searchedUserLastName)
    {
        Uri builtUri = Uri.parse(URI + SEARCH_USER_PATH).buildUpon()
                .appendQueryParameter(AUTHKEY_QUERY_KEY, authkey)
                .appendQueryParameter(LAST_NAME_KEY, searchedUserLastName)
                .build();

        URL url = null;

        try
        {
            url = new URL(builtUri.toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return url;
    }

    public URL getChangePasswordUrl(int user_id, String old_password, String new_password)
    {
        PasswordManager passwordManager = new PasswordManager();
        String old_auth = passwordManager.getAuthkey(old_password);
        String new_auth = passwordManager.getAuthkey(new_password);

        Uri builtUri = Uri.parse(URI + CHANGE_PASSWORD_PATH).buildUpon()
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .appendQueryParameter(NEW_AUTH_KEY, new_auth)
                .appendQueryParameter(OLD_AUTH_KEY, old_auth)
                .build();

        URL url = null;

        try
        {
            url = new URL(builtUri.toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return url;
    }

    public URL getAddUserUrl(String username, String first_name, String last_name, String authkey, User.user_type user_type)
    {
        PasswordManager passwordManager = new PasswordManager();

        String user_type_string = "";

        switch (user_type)
        {
            case admin:
                user_type_string = "admin";
                break;
            case lector:
                user_type_string = "lector";
                break;
            case student:
                user_type_string = "student";
                break;
        }

        Uri builtUri = Uri.parse(URI + ADD_NEW_USER_PATH).buildUpon()
                .appendQueryParameter(FIRST_NAME_KEY, first_name)
                .appendQueryParameter(LAST_NAME_KEY, last_name)
                .appendQueryParameter(USERNAME_QUERY_KEY, username)
                .appendQueryParameter(AUTHKEY_QUERY_KEY, authkey)
                .appendQueryParameter(USER_TYPE_KEY, user_type_string)
                .build();

        URL builtURL = null;

        try
        {
            builtURL = new URL(builtUri.toString());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return builtURL;
    }

}
