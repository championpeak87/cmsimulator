package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.models.ChessGame;
import fiitstu.gulis.cmsimulator.models.ChessGameModel;
import fiitstu.gulis.cmsimulator.network.ChessGameParser;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BrowseGamesActivity extends FragmentActivity {
    private static final String TAG = "BrowseGamesActivity";

    private List<ChessGameModel> listOfGames = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_games);

        setActionBar();
        fetchGames();
    }

    private void setActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.available_games);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void fetchGames() {
        new FetchGamesAsync().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class FetchGamesAsync extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            UrlManager urlManager = new UrlManager();
            URL url = urlManager.getFetchGamesURL();
            ServerController serverController = new ServerController();
            String output = null;

            try {
                output = serverController.getResponseFromServer(url);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                return output;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null || s.isEmpty()) {
                Toast.makeText(BrowseGamesActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    listOfGames = ChessGameParser.getListOfChessGamesFromJSONArray(new JSONArray(s));
                } catch (JSONException e) {
                    Toast.makeText(BrowseGamesActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
