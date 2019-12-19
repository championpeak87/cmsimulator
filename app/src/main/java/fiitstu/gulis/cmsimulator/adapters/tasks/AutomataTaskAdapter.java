package fiitstu.gulis.cmsimulator.adapters.tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.AutomataTaskDetailsActivity;
import fiitstu.gulis.cmsimulator.activities.BrowseAutomataTasksActivity;
import fiitstu.gulis.cmsimulator.activities.MainActivity;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.FiniteAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.LinearBoundedAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.PushdownAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class AutomataTaskAdapter extends RecyclerView.Adapter<AutomataTaskAdapter.CardViewBuilder> {

    private List<Task> listOfTasks = null;
    private Context mContext;

    public AutomataTaskAdapter(List<Task> listOfTasks, Context mContext) {
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
    public void onBindViewHolder(@NonNull CardViewBuilder holder, final int position) {
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
                detailsIntent.putExtra("TIME", currentTask.getMinutes());

                mContext.startActivity(detailsIntent, options.toBundle());
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

    public void setListOfTasks(List<Task> listOfTasks) {
        this.listOfTasks = listOfTasks;
    }

    public static class CardViewBuilder extends RecyclerView.ViewHolder {
        private TextView task_name;
        private TextView automata_type;
        private ImageButton delete_task;
        private ImageButton edit_task;
        private ImageButton help_task;
        private CardView cardView;

        public CardViewBuilder(View itemView) {
            super(itemView);
            this.task_name = itemView.findViewById(R.id.textview_task_name);
            this.automata_type = itemView.findViewById(R.id.textview_automata_type);
            this.delete_task = itemView.findViewById(R.id.button_delete_task);
            this.edit_task = itemView.findViewById(R.id.button_edit_task);
            this.help_task = itemView.findViewById(R.id.button_help_task);
            this.cardView = itemView.findViewById(R.id.cardview_task);
        }
    }

    private void removeTask(int position) {
        listOfTasks.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, listOfTasks.size());
    }
}
