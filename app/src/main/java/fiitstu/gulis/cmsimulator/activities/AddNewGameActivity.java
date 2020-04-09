package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Pair;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.dialogs.ExitAddNewGameDialog;
import fiitstu.gulis.cmsimulator.exceptions.NotImplementedException;
import fiitstu.gulis.cmsimulator.views.ChessView;

public class AddNewGameActivity extends FragmentActivity {
    private static final String TAG = "AddNewGameActivity";

    // UI ELEMENTS
    LinearLayout linearlayout_task_description;
    EditText edittext_task_name;
    EditText edittext_task_description;
    ChessView chessview_task;
    Button button_zoom_in;
    Button button_zoom_out;

    private float[] touchCoordinates = {0, 0};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_game);

        setActionBar();
        setUIElements();
        setEvents();
    }

    private void setUIElements() {
        linearlayout_task_description = findViewById(R.id.linearlayout_task_description);
        edittext_task_name = findViewById(R.id.edittext_task_name);
        edittext_task_description = findViewById(R.id.edittext_task_description);
        chessview_task = findViewById(R.id.chessview_task);
        button_zoom_in = findViewById(R.id.button_zoom_in);
        button_zoom_out = findViewById(R.id.button_zoom_out);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_chessview_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final Pair<Integer, Integer> rectCoord = chessview_task.getRectCoordForTouchCoord(touchCoordinates[0], touchCoordinates[1]);
        switch (item.getItemId()) {
            case R.id.menu_set_as_start:
                try {
                    chessview_task.setStartField(rectCoord);
                } catch (ChessView.OutOfChessFieldException e) {
                    Toast.makeText(AddNewGameActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_set_as_finish:
                try {
                    chessview_task.setFinishField(rectCoord);
                } catch (ChessView.OutOfChessFieldException e) {
                    Toast.makeText(AddNewGameActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void setEvents() {
        registerForContextMenu(chessview_task);

        chessview_task.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final float x = event.getX();
                final float y = event.getY();
                touchCoordinates[0] = x;
                touchCoordinates[1] = y;

                return false;
            }
        });

        chessview_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Pair<Integer, Integer> rectCoord = chessview_task.getRectCoordForTouchCoord(touchCoordinates[0], touchCoordinates[1]);
                try {
                    if (chessview_task.isFieldInPath(rectCoord))
                        chessview_task.removeFieldFromPath(rectCoord);
                    else
                        chessview_task.addFieldToPath(rectCoord);
                } catch (ChessView.OutOfChessFieldException e) {
                    Toast.makeText(AddNewGameActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_zoom_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int chessFieldWidth = chessview_task.getChessFieldWidth();
                final int chessFieldHeight = chessview_task.getChessFieldHeight();

                final Pair<Integer, Integer> targetSize = new Pair<>(chessFieldWidth - 1, chessFieldHeight - 1);
                try {
                    chessview_task.setChessFieldWidth(targetSize.first);
                    chessview_task.setChessFieldHeight(targetSize.second);
                } catch (ChessView.OutOfChessFieldBoundariesException e) {
                    Toast.makeText(AddNewGameActivity.this, R.string.game_field_size_exceeded, Toast.LENGTH_LONG).show();
                }
            }
        });

        button_zoom_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int chessFieldWidth = chessview_task.getChessFieldWidth();
                final int chessFieldHeight = chessview_task.getChessFieldHeight();

                final Pair<Integer, Integer> targetSize = new Pair<>(chessFieldWidth + 1, chessFieldHeight + 1);

                try {
                    chessview_task.setChessFieldWidth(targetSize.first);
                    chessview_task.setChessFieldHeight(targetSize.second);
                } catch (ChessView.OutOfChessFieldBoundariesException e) {
                    Toast.makeText(AddNewGameActivity.this, R.string.game_field_size_exceeded, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_add_new_game, menu);

        return true;
    }

    private void setActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.add_new_game);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_upload_task:
                // TODO: IMPLEMENT GAME UPLOADING
                try {
                    throw new NotImplementedException(this);
                } catch (NotImplementedException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.menu_settings:
                // TODO: IMPLEMENT INTENT TO SETTINGS
                try {
                    throw new NotImplementedException(this);
                } catch (NotImplementedException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.menu_help:
                // TODO: SET HELP
                try {
                    throw new NotImplementedException(this);
                } catch (NotImplementedException e) {
                    e.printStackTrace();
                }
                return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        boolean modified = checkIfGameModified();
        if (modified) {
            final FragmentManager fragmentManager = this.getSupportFragmentManager();
            ExitAddNewGameDialog exitAddNewGameDialog = new ExitAddNewGameDialog();
            exitAddNewGameDialog.show(fragmentManager, TAG);
        } else super.onBackPressed();
    }

    private boolean checkIfGameModified() {
        boolean taskNameModified;
        boolean taskDescriptionModified;
        boolean hasStartField;
        boolean hasFinishField;
        boolean hasPath;

        taskNameModified = !edittext_task_name.getText().toString().isEmpty();
        taskDescriptionModified = !edittext_task_description.getText().toString().isEmpty();
        hasStartField = chessview_task.getStartField().first != -1 || chessview_task.getStartField().second != -1;
        hasFinishField = chessview_task.getFinishField().first != -1 || chessview_task.getFinishField().second != -1;
        hasPath = chessview_task.getPath().size() > 0;

        return taskNameModified || taskDescriptionModified || hasStartField || hasFinishField || hasPath;
    }
}
