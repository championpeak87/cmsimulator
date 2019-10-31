package fiitstu.gulis.cmsimulator.network;

import android.app.DownloadManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ServerController {

    public String getResponseFromServer(URL url) throws IOException
    {
        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
        try
        {
            InputStream in = httpURLConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput)
            {
                return scanner.next();
            }
            else
            {
                return null;
            }
        }
        finally {
            httpURLConnection.disconnect();
        }
    }

}
