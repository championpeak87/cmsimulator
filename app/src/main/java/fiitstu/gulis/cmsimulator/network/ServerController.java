package fiitstu.gulis.cmsimulator.network;

import android.app.DownloadManager;
import android.view.View;
import com.squareup.okhttp.*;

import java.io.*;
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

    public String doPostRequest(File file) {

        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("task", file.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), file))
                .build();

        UrlManager urlManager = new UrlManager();

        Request request = new Request.Builder()
                .url(urlManager.getPublishAutomataTaskURL(file.getName()))
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        Call call = okHttpClient.newCall(request);
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.toString();
    }




}
