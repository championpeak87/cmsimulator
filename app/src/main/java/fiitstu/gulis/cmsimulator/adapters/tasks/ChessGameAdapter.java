package fiitstu.gulis.cmsimulator.adapters.tasks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.models.ChessGame;

import java.util.ArrayList;
import java.util.List;

public class ChessGameAdapter extends RecyclerView.Adapter<ChessGameAdapter.ViewHolder> {
    private static final String TAG = "ChessGameAdapter";

    private List<ChessGame> listOfGames = new ArrayList<>();
    private Context mContext;

    public ChessGameAdapter(List<ChessGame> listOfGames, Context mContext) {
        this.listOfGames = listOfGames;
        this.mContext = mContext;
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
        // TODO: NETWORK FETCH GAMES
    }

    @Override
    public int getItemCount() {
        return listOfGames.size();
    }

    public void addGameToList(ChessGame game) {
        this.listOfGames.add(game);
        final int addPosition = listOfGames.indexOf(game);
        notifyItemInserted(addPosition);
    }

    public void removeGameFromList(ChessGame game) {
        final int gamePosition = listOfGames.indexOf(game);
        this.listOfGames.remove(game);
        notifyItemRemoved(gamePosition);
    }

    public void setGameList(List<ChessGame> listOfGames) {
        int count = listOfGames.size();
        this.listOfGames = listOfGames;
        notifyItemRangeChanged(0, count);
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