package fiitstu.gulis.cmsimulator.adapters.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.exceptions.NotImplementedException;
import fiitstu.gulis.cmsimulator.models.ChessGame;
import fiitstu.gulis.cmsimulator.models.ChessGameModel;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChessGameAdapter extends RecyclerView.Adapter<ChessGameAdapter.ViewHolder> {
    private static final String TAG = "ChessGameAdapter";

    public interface OnDataSetChangedListener {
        void onChange();
    }

    private List<ChessGameModel> listOfGames = new ArrayList<>();
    private Context mContext;
    private OnDataSetChangedListener onDataSetChangedListener = null;

    public ChessGameAdapter(List<ChessGameModel> listOfGames, Context mContext) {
        this.listOfGames = listOfGames;
        this.mContext = mContext;
    }

    public void setOnDataSetChangedListener(OnDataSetChangedListener onDataSetChangedListener) {
        this.onDataSetChangedListener = onDataSetChangedListener;
    }

    public ChessGameAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = ((FragmentActivity) mContext).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.list_element_game, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final ChessGameModel currentItem = listOfGames.get(i);

        viewHolder.textview_task_name.setText(currentItem.getTask_name());
        viewHolder.cardview_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: IMPLEMENT GAME START
                try {
                    throw new NotImplementedException(mContext);
                } catch (NotImplementedException e) {
                    e.printStackTrace();
                }
            }
        });

        viewHolder.button_delete_task.setOnClickListener(new View.OnClickListener() {
            final int task_id = currentItem.getTask_id();
            class DeleteGameAsync extends AsyncTask<Void, Void, String> {
                @Override
                protected String doInBackground(Void... voids) {
                    UrlManager urlManager = new UrlManager();
                    URL deleteURL = urlManager.getDeleteGameURL(task_id);
                    ServerController serverController = new ServerController();
                    String output = null;

                    try {
                        output = serverController.getResponseFromServer(deleteURL);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        return output;
                    }
                }

                @Override
                protected void onPostExecute(String s) {
                    if (s == null || s.isEmpty()) {
                        Toast.makeText(mContext, R.string.generic_error, Toast.LENGTH_SHORT).show();
                    } else {
                        removeGameFromList(currentItem);
                        Toast.makeText(mContext, R.string.game_removed, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onClick(View v) {
                new DeleteGameAsync().execute();
            }
        });

        viewHolder.button_help_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: IMPLEMENT GAME HELP
                try {
                    throw new NotImplementedException(mContext);
                } catch (NotImplementedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listOfGames.size();
    }

    public void addGameToList(ChessGameModel game) {
        this.listOfGames.add(game);
        final int addPosition = listOfGames.indexOf(game);
        notifyItemInserted(addPosition);
        if (onDataSetChangedListener != null)
            onDataSetChangedListener.onChange();
    }

    public void removeGameFromList(ChessGameModel game) {
        final int gamePosition = listOfGames.indexOf(game);
        this.listOfGames.remove(game);
        notifyItemRemoved(gamePosition);
        if (onDataSetChangedListener != null)
            onDataSetChangedListener.onChange();
    }

    public void setGameList(List<ChessGameModel> listOfGames) {
        int count = listOfGames.size();
        this.listOfGames = listOfGames;
        notifyItemRangeChanged(0, count);
        if (onDataSetChangedListener != null)
            onDataSetChangedListener.onChange();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardview_task;
        private FrameLayout framelayout_context;
        private ImageButton button_delete_task;
        private ImageButton button_help_task;
        private ProgressBar progressbar_task_loading;
        private TextView textview_task_name;

        private int task_id;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardview_task = itemView.findViewById(R.id.cardview_task);
            framelayout_context = itemView.findViewById(R.id.framelayout_context);
            button_delete_task = itemView.findViewById(R.id.button_delete_task);
            button_help_task = itemView.findViewById(R.id.button_help_task);
            progressbar_task_loading = itemView.findViewById(R.id.progressbar_task_loading);
            textview_task_name = itemView.findViewById(R.id.textview_task_name);
        }
    }
}