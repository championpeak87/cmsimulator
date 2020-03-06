package fiitstu.gulis.cmsimulator.activities;

import android.Manifest;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.database.FileHandler;
import fiitstu.gulis.cmsimulator.dialogs.ExitDialog;
import fiitstu.gulis.cmsimulator.dialogs.NewMachineDialog;
import fiitstu.gulis.cmsimulator.elements.GrammarRule;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

public class NewGrammarTaskActivity extends FragmentActivity {
    private static final String TAG = "NewGrammarTaskActivity";

    // UI ELEMENTS
    private EditText taskNameEditText;
    private EditText taskTextEditText;
    private Button setTaskButton;
    private CheckBox publicTestsCheckBox;
    private CheckBox timerCheckBox;
    private EditText timerEditText;

    private boolean hasTaskSet = false;
    private boolean modified = false;

    //storage permissions
    public static final int REQUEST_READ_STORAGE = 0;
    public static final int REQUEST_WRITE_STORAGE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_new_grammar_task);

        setActionBar();
        setUIElements();
        setEvents();
    }

    @Override
    public void onBackPressed() {
        if (modified) {
            FragmentManager fm = getFragmentManager();
            ExitDialog exitDialog = new ExitDialog();
            exitDialog.setOnExitListener(new ExitDialog.OnExitListener() {
                @Override
                public void onExit() {
                    DataSource dataSource = DataSource.getInstance();
                    dataSource.open();
                    dataSource.globalDrop();
                    dataSource.close();
                }
            });
            exitDialog.show(fm, TAG);
        } else super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_new_grammar_task, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_help:
                /* TODO: IMPLEMENT HELP */
                Toast.makeText(this, "NOT IMPLEMENTED", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_publish_grammar_task:
                // TODO: IMPLEMENT TASK UPLOADING
                if (!modified && !hasTaskSet) {
                    Toast.makeText(this, R.string.task_not_set, Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (Build.VERSION.SDK_INT > 15
                        && ContextCompat.checkSelfPermission(NewGrammarTaskActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(NewGrammarTaskActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_STORAGE);
                } else {
                    DataSource dataSource = DataSource.getInstance();
                    dataSource.open();
                    List<GrammarRule> grammarRuleList = dataSource.getGrammarRuleFullExtract();
                    FileHandler fileHandler = new FileHandler(FileHandler.Format.CMSG);
                    try {
                        fileHandler.setData(grammarRuleList);
                        fileHandler.writeFile("grammarTask");
                    } catch (ParserConfigurationException | IOException | TransformerException e) {
                        e.printStackTrace();
                    }

                    final File file = new File(FileHandler.PATH, "grammarTask" + ".cmsg");
                    try {
                        Scanner scanner = new Scanner(file);
                        String line;
                        while (scanner.hasNextLine()){
                            line = scanner.nextLine();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    final String file_name = taskNameEditText.getText() + ".cmsg";

                    class UploadGrammarTask extends AsyncTask<File, Void, String> {
                        @Override
                        protected String doInBackground(File... files) {
                            UrlManager urlManager = new UrlManager();
                            URL url = urlManager.getUploadGrammarTaskURL(file_name);
                            ServerController serverController = new ServerController();
                            String output = null;

                            output = serverController.doPostRequest(url, files[0]);

                            return output;

                        }

                        @Override
                        protected void onPostExecute(String s) {
                            if (s == null || s.isEmpty()) {
                                Toast.makeText(NewGrammarTaskActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(NewGrammarTaskActivity.this, R.string.upload_task_complete, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    new UploadGrammarTask().execute(file);
                }

                return true;
        }

        return false;
    }

    private void setActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.new_grammar_task);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setUIElements() {
        this.taskNameEditText = findViewById(R.id.edittext_task_name);
        this.taskTextEditText = findViewById(R.id.edittext_task_text);
        this.setTaskButton = findViewById(R.id.button_set_task_grammar);
        this.publicTestsCheckBox = findViewById(R.id.checkbox_input_tests);
        this.timerCheckBox = findViewById(R.id.checkbox_timer_grammar);
        this.timerEditText = findViewById(R.id.edittext_timer_grammar);
    }

    private void setEvents() {
        this.timerCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean timerEnabled = NewGrammarTaskActivity.this.timerCheckBox.isChecked();
                NewGrammarTaskActivity.this.timerEditText.setEnabled(timerEnabled);
            }
        });

        this.setTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent grammarConfiguration = new Intent(NewGrammarTaskActivity.this, GrammarActivity.class);
                grammarConfiguration.putExtra("GRAMMAR_TASK_CONFIGURATION", true);
                startActivity(grammarConfiguration);
                hasTaskSet = true;
                modified = true;
            }
        });
    }

}
