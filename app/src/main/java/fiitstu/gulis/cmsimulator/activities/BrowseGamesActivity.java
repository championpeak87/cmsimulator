package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.tasks.ChessGameAdapter;
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

    // UI ELEMENTS
    private LinearLayout linearlayout_empty_games;
    private ProgressBar progressbar_games;
    private RecyclerView recyclerview_games;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_games);

        setActionBar();
        setUIElements();
        fetchGames();
        //setRecyclerView();
    }

    private void setUIElements() {
        linearlayout_empty_games = findViewById(R.id.linearLayout_empty_games);
        progressbar_games = findViewById(R.id.progressbar_games);
        recyclerview_games = findViewById(R.id.recyclerview_games);
    }

    private void setRecyclerView() {
        final ChessGameAdapter chessGameAdapter = new ChessGameAdapter(listOfGames, this);
        chessGameAdapter.setOnDataSetChangedListener(new ChessGameAdapter.OnDataSetChangedListener() {
            @Override
            public void onChange() {
                linearlayout_empty_games.setVisibility(chessGameAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });
        recyclerview_games.setAdapter(chessGameAdapter);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180);

        GridLayoutManager layoutManager = new GridLayoutManager(this, noOfColumns);
        recyclerview_games.setLayoutManager(layoutManager);
        recyclerview_games.setAdapter(chessGameAdapter);

        Animation showUpAnimation = AnimationUtils.loadAnimation(this, R.anim.item_show_animation);

        recyclerview_games.setAnimation(showUpAnimation);

        Log.i("SIZE", Integer.toString(listOfGames.size()));
        if (listOfGames.size() != 0) {
            linearlayout_empty_games.setVisibility(View.GONE);
        }
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
                    setRecyclerView();
                } catch (JSONException e) {
                    Toast.makeText(BrowseGamesActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
