package fiitstu.gulis.cmsimulator.network;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

public class UrlManager {

    private final String URI = "http://192.168.43.160:3000";

    private final String LOGIN_PATH = "/api/login";

    private final String USERNAME_QUERY_KEY = "username";
    private final String AUTHKEY_QUERY_KEY = "auth_key";


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
}
