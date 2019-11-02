package fiitstu.gulis.cmsimulator.network;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

public class UrlManager {

    private final String URI = "http://192.168.1.235:3000";

    private final String LOGIN_PATH = "/api/login";
    private final String CHANGE_PASSWORD_PATH = "/api/user/changePassword";

    // LOGIN QUERY KEYS
    private final String USERNAME_QUERY_KEY = "username";
    private final String AUTHKEY_QUERY_KEY = "auth_key";

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
}
