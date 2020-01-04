package fiitstu.gulis.cmsimulator.adapters.tasks;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.*;
import fiitstu.gulis.cmsimulator.database.FileFormatException;
import fiitstu.gulis.cmsimulator.database.FileHandler;
import fiitstu.gulis.cmsimulator.dialogs.TaskDialog;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.FiniteAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.LinearBoundedAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.PushdownAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class AutomataTaskAdapter extends RecyclerView.Adapter<AutomataTaskAdapter.CardViewBuilder> {

    private static final String TASK_DIALOG = "TASK_DIALOG";

    private List<Task> listOfTasks = null;
    private BrowseAutomataTasksActivity mContext;

    public AutomataTaskAdapter(List<Task> listOfTasks, BrowseAutomataTasksActivity mContext) {
        this.listOfTasks = listOfTasks;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public CardViewBuilder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.list_view_automata_task_item, parent, false);

        return new AutomataTaskAdapter.CardViewBuilder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CardViewBuilder holder, final int position) {
        final Task currentTask = listOfTasks.get(position);
        holder.task_name.setText(currentTask.getTitle());
        final String automataType;
        if (currentTask instanceof FiniteAutomataTask) {
            automataType = automata_type.FINITE_AUTOMATA.toString();
        } else if (currentTask instanceof PushdownAutomataTask) {
            automataType = automata_type.PUSHDOWN_AUTOMATA.toString();
        } else if (currentTask instanceof LinearBoundedAutomataTask) {
            automataType = automata_type.LINEAR_BOUNDED_AUTOMATA.toString();
        } else {
            automataType = automata_type.TURING_MACHINE.toString();
        }
        holder.automata_type.setText(automataType);

        Task.TASK_STATUS currentStatus = currentTask.getStatus();
        final int primary;
        final int light_primary;


        switch (currentStatus) {

            case WRONG:
                primary = mContext.getColor(R.color.wrong_answer_top_bar);
                light_primary = mContext.getColor(R.color.wrong_answer_bottom_bar);
                break;

            case IN_PROGRESS:
                primary = mContext.getColor(R.color.in_progress_top_bar);
                light_primary = mContext.getColor(R.color.in_progress_bottom_bar);
                break;

            case CORRECT:
                primary = mContext.getColor(R.color.correct_answer_top_bar);
                light_primary = mContext.getColor(R.color.correct_answer_bottom_bar);
                break;

            case TOO_LATE:
                primary = mContext.getColor(R.color.md_black_1000);
                light_primary = mContext.getColor(R.color.bootstrap_gray);
                break;

            case NEW:
            default:
                primary = mContext.getColor(R.color.primary_color);
                light_primary = mContext.getColor(R.color.primary_color_light);
                break;
        }

        holder.topBar.setBackgroundColor(primary);
        holder.bottomBar.setBackgroundColor(light_primary);

        final CardView cardView = holder.cardView;
        holder.help_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailsIntent = new Intent(mContext, AutomataTaskDetailsActivity.class);

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, cardView, ViewCompat.getTransitionName(cardView));
                detailsIntent.putExtra("TASK_TYPE", automataType);
                detailsIntent.putExtra("TASK_NAME", currentTask.getTitle());
                detailsIntent.putExtra("TASK_DESCRIPTION", currentTask.getText());
                detailsIntent.putExtra("PUBLIC_INPUT", currentTask.getPublicInputs());
                detailsIntent.putExtra("PRIMARY_COLOR", primary);
                detailsIntent.putExtra("LIGHT_PRIMARY", light_primary);
                detailsIntent.putExtra("TIME", currentTask.getMinutes());

                mContext.startActivity(detailsIntent, options.toBundle());
            }
        });

        final AutomataTaskAdapter thisAdapter = this;
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                class DownloadTaskAsync extends AsyncTask<Void, Void, String> {
                    @Override
                    protected String doInBackground(Void... voids) {
                        UrlManager urlManager = new UrlManager();
                        URL downloadUrl = urlManager.getAutomataTaskDownloadURL(currentTask.getTask_id(), BrowseAutomataTasksActivity.user_id);
                        ServerController serverController = new ServerController();
                        String output = null;
                        try {
                            output = serverController.getResponseFromServer(downloadUrl);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            return output;
                        }
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        if (!saveDownloadedFile(s)) {
                            Toast.makeText(mContext, R.string.error_no_permissions, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int machineType;
                        if (currentTask instanceof FiniteAutomataTask) {
                            machineType = MainActivity.FINITE_STATE_AUTOMATON;
                        } else if (currentTask instanceof PushdownAutomataTask) {
                            machineType = MainActivity.PUSHDOWN_AUTOMATON;
                        } else if (currentTask instanceof LinearBoundedAutomataTask) {
                            machineType = MainActivity.LINEAR_BOUNDED_AUTOMATON;
                        } else {
                            machineType = MainActivity.TURING_MACHINE;
                        }

                        TaskDialog taskDialog = TaskDialog.newInstance(currentTask, TaskDialog.ENTERING, machineType);
                        taskDialog.setAdapter(thisAdapter);
                        taskDialog.show(mContext.getSupportFragmentManager(), TASK_DIALOG);
                    }
                }

                new DownloadTaskAsync().execute();
            }
        });

        holder.delete_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.delete_task)
                        .setMessage(R.string.task_delete_message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                class DeleteTaskAsync extends AsyncTask<URL, Void, String> {
                                    @Override
                                    protected String doInBackground(URL... urls) {
                                        ServerController serverController = new ServerController();
                                        String output = null;
                                        try {
                                            output = serverController.getResponseFromServer(urls[0]);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } finally {
                                            return output;
                                        }
                                    }

                                    @Override
                                    protected void onPostExecute(String s) {
                                        if (s == null) {
                                            Toast.makeText(mContext, R.string.generic_error, Toast.LENGTH_SHORT).show();
                                        } else {
                                            try {
                                                JSONObject reader = new JSONObject(s);
                                                if (reader.getBoolean("deleted")) {
                                                    Toast.makeText(mContext, R.string.task_deleted, Toast.LENGTH_SHORT).show();
                                                    removeTask(position);
                                                } else
                                                    Toast.makeText(mContext, R.string.task_not_found, Toast.LENGTH_SHORT).show();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }

                                UrlManager urlManager = new UrlManager();
                                URL deleteUrl = urlManager.getDeleteAutomataTaskURL(currentTask.getTask_id());
                                new DeleteTaskAsync().execute(deleteUrl);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });


    }

    @Override
    public int getItemCount() {
        if (listOfTasks == null)
            return 0;
        else
            return listOfTasks.size();
    }

    public static class CardViewBuilder extends RecyclerView.ViewHolder {
        private TextView task_name;
        private TextView automata_type;
        private ImageButton delete_task;
        private ImageButton help_task;
        private CardView cardView;
        private LinearLayout bottomBar;
        private TextView topBar;

        public CardViewBuilder(View itemView) {
            super(itemView);
            this.task_name = itemView.findViewById(R.id.textview_task_name);
            this.automata_type = itemView.findViewById(R.id.textview_automata_type);
            this.delete_task = itemView.findViewById(R.id.button_delete_task);
            this.help_task = itemView.findViewById(R.id.button_help_task);
            this.cardView = itemView.findViewById(R.id.cardview_task);
            this.bottomBar = itemView.findViewById(R.id.task_bottom_bar);
            this.topBar = itemView.findViewById(R.id.textview_automata_type);
        }
    }

    private void removeTask(int position) {
        listOfTasks.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, listOfTasks.size());
    }

    private boolean saveDownloadedFile(String input) {
        File file = new File(FileHandler.PATH + "/automataTask.cmst");
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mContext,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return false;
        } else {
            BufferedWriter writer = null;
            try {
                FileWriter fileWriter = new FileWriter(file);
                writer = new BufferedWriter(fileWriter);
                writer.write(input);
                writer.close();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public List<Task> getListOfTasks() {
        return listOfTasks;
    }

    public void notifyChange(int position) {
        notifyItemChanged(position);
    }

    public void notifyStatusChange(int task_id, Task.TASK_STATUS status) {
        for (int i = 0; i < listOfTasks.size(); i++) {
            Task currentTask = listOfTasks.get(i);
            if (currentTask.getTask_id() == task_id) {
                currentTask.setStatus(status);
                notifyItemChanged(i);
                break;
            }
        }
    }

}
