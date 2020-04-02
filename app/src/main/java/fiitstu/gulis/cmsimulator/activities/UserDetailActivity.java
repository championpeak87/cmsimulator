package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.transition.Fade;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class UserDetailActivity extends FragmentActivity {

    private EditText edittext_automatas_new, edittext_automatas_in_progress, edittext_automatas_correct, edittext_automatas_wrong, edittext_automatas_too_late;
    private EditText edittext_grammars_new, edittext_grammars_in_progress, edittext_grammars_correct, edittext_grammars_wrong, edittext_grammars_too_late;

    private int automata_tasks_count = 0, grammar_tasks_count = 0, user_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_user_detail);

        //menu
        ActionBar actionBer = this.getActionBar();
        actionBer.setDisplayHomeAsUpEnabled(true);
        actionBer.setTitle(R.string.user_details);

        // handle data transfer
        TextView username = findViewById(R.id.textview_user_username);
        TextView fullname = findViewById(R.id.textview_full_name);
        TextView type = findViewById(R.id.textview_user_type);

        edittext_automatas_new = findViewById(R.id.edittext_automatas_new);
        edittext_automatas_in_progress = findViewById(R.id.edittext_automatas_in_progress);
        edittext_automatas_correct = findViewById(R.id.edittext_automatas_correct);
        edittext_automatas_wrong = findViewById(R.id.edittext_automatas_wrong);
        edittext_automatas_too_late = findViewById(R.id.edittext_automatas_too_late);

        edittext_grammars_new = findViewById(R.id.edittext_grammars_new);
        edittext_grammars_in_progress = findViewById(R.id.edittext_grammars_in_progress);
        edittext_grammars_correct = findViewById(R.id.edittext_grammars_correct);
        edittext_grammars_wrong = findViewById(R.id.edittext_grammars_wrong);
        edittext_grammars_too_late = findViewById(R.id.edittext_grammars_too_late);

        Intent thisIntent = this.getIntent();
        String user_name = thisIntent.getStringExtra("USERNAME");
        String full_name = thisIntent.getStringExtra("FULLNAME");
        String user_type = thisIntent.getStringExtra("USER_TYPE");
        user_id = thisIntent.getIntExtra("USER_ID", -1);

        username.setText(user_name);
        fullname.setText(full_name);
        type.setText(user_type);

        // handle connected transition
        setConnectedTransition();

        setData();
    }

    private void setData() {
        new FetchAutomataTasksCountAsync().execute();
        new FetchGrammarTasksCountAsync().execute();
    }

    class FetchUserResultsAsync extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... integers) {
            ServerController serverController = new ServerController();
            UrlManager urlManager = new UrlManager();
            URL url = urlManager.getUserResultsOverviewURL(integers[0]);
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
                Toast.makeText(UserDetailActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray array = new JSONArray(s);
                    final int arraySize = array.length();

                    int automatas_correct = 0, automatas_new = 0, automatas_in_progress = 0, automatas_wrong = 0, automatas_too_late = 0;
                    for (int i = 0; i < arraySize; i++) {
                        JSONObject object = array.getJSONObject(i);
                        if (object.has("found"))
                            break;
                        String currentStatus = object.getString("task_status");

                        switch (currentStatus) {
                            case "new":
                                automatas_new = object.getInt("count");
                                break;
                            case "in_progress":
                                automatas_in_progress = object.getInt("count");
                                break;
                            case "correct":
                                automatas_correct = object.getInt("count");
                                break;
                            case "wrong":
                                automatas_wrong = object.getInt("count");
                                break;
                            case "too_late":
                                automatas_too_late = object.getInt("count");
                                break;
                            default:
                                break;
                        }
                    }
                    edittext_automatas_new.setText(Integer.toString(automatas_new) + " / " + Integer.toString(automata_tasks_count));
                    edittext_automatas_in_progress.setText(Integer.toString(automatas_in_progress) + " / " + Integer.toString(automata_tasks_count));
                    edittext_automatas_correct.setText(Integer.toString(automatas_correct) + " / " + Integer.toString(automata_tasks_count));
                    edittext_automatas_wrong.setText(Integer.toString(automatas_wrong) + " / " + Integer.toString(automata_tasks_count));
                    edittext_automatas_too_late.setText(Integer.toString(automatas_too_late) + " / " + Integer.toString(automata_tasks_count));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(UserDetailActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    class FetchGrammarResultsAsync extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... integers) {
            ServerController serverController = new ServerController();
            UrlManager urlManager = new UrlManager();
            URL url = urlManager.getGrammarResultsOverviewURL(integers[0]);
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
                Toast.makeText(UserDetailActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray array = new JSONArray(s);
                    final int arraySize = array.length();

                    int grammars_correct = 0, grammars_new = 0, grammars_in_progress = 0, grammars_wrong = 0, grammars_too_late = 0;
                    for (int i = 0; i < arraySize; i++) {
                        JSONObject object = array.getJSONObject(i);
                        if (object.has("found"))
                            break;
                        String currentStatus = object.getString("task_status");

                        switch (currentStatus) {
                            case "new":
                                grammars_new = object.getInt("count");
                                break;
                            case "in_progress":
                                grammars_in_progress = object.getInt("count");
                                break;
                            case "correct":
                                grammars_correct = object.getInt("count");
                                break;
                            case "wrong":
                                grammars_wrong = object.getInt("count");
                                break;
                            case "too_late":
                                grammars_too_late = object.getInt("count");
                                break;
                            default:
                                break;
                        }
                    }
                    edittext_grammars_new.setText(Integer.toString(grammars_new) + " / " + Integer.toString(grammar_tasks_count));
                    edittext_grammars_in_progress.setText(Integer.toString(grammars_in_progress) + " / " + Integer.toString(grammar_tasks_count));
                    edittext_grammars_correct.setText(Integer.toString(grammars_correct) + " / " + Integer.toString(grammar_tasks_count));
                    edittext_grammars_wrong.setText(Integer.toString(grammars_wrong) + " / " + Integer.toString(grammar_tasks_count));
                    edittext_grammars_too_late.setText(Integer.toString(grammars_too_late) + " / " + Integer.toString(grammar_tasks_count));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(UserDetailActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    class FetchAutomataTasksCountAsync extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            ServerController serverController = new ServerController();
            UrlManager urlManager = new UrlManager();
            URL url = urlManager.getAutomataTasksCountURL();
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
                Toast.makeText(UserDetailActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray array = new JSONArray(s);
                    JSONObject object = array.getJSONObject(0);
                    automata_tasks_count = object.getInt("count");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(UserDetailActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                }
                new FetchUserResultsAsync().execute(user_id);
            }
        }
    }

    class FetchGrammarTasksCountAsync extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            ServerController serverController = new ServerController();
            UrlManager urlManager = new UrlManager();
            URL url = urlManager.getGrammarTasksCountURL();
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
                Toast.makeText(UserDetailActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray array = new JSONArray(s);
                    JSONObject object = array.getJSONObject(0);
                    grammar_tasks_count = object.getInt("count");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(UserDetailActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                }
            }
            new FetchGrammarResultsAsync().execute(user_id);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return false;
    }

    private void setConnectedTransition() {
        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(decor.findViewById(R.id.action_bar_container), true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
    }
}
