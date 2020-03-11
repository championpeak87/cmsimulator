package fiitstu.gulis.cmsimulator.network.grammar_tasks;

import android.os.AsyncTask;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;

import java.io.IOException;
import java.net.URL;
import java.sql.Time;

public class UpdateTimerAsync extends AsyncTask<String, Void, String> {

    /**
     *
     * @param strings[0] task_id, strings[1] user_id, strings[2] time
     * @return
     */
    @Override
    protected String doInBackground(String... strings) {
        int user_id = Integer.parseInt(strings[1]);
        int task_id = Integer.parseInt(strings[0]);
        Time time = Time.valueOf(strings[2]);

        UrlManager urlManager = new UrlManager();
        ServerController serverController = new ServerController();
        URL url = urlManager.getUpdateGrammarTimerURL(user_id, task_id, time);
        String output = null;

        try {
            output = serverController.getResponseFromServer(url);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return output;
        }
    }
}
