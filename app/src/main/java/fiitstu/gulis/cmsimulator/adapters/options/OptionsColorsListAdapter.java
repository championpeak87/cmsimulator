package fiitstu.gulis.cmsimulator.adapters.options;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.MachineColorsGenerator;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.elements.MachineColor;
/*library used from https://github.com/yukuku/ambilwarna*/
import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Adapter for showing list of colors in the settings
 *
 * Created by Martin on 28. 3. 2017.
 */
public class OptionsColorsListAdapter extends RecyclerView.Adapter<OptionsColorsListAdapter.ViewHolder> {

    //log tag
    private static final String TAG = OptionsColorsListAdapter.class.getName();

    private MachineColorsGenerator machineColorsGenerator;
    private Context context;

    private ItemClickCallback itemClickCallback;

    public interface ItemClickCallback {
        void onColorAddClick(int color, int order);

        void onColorUpdateClick(int position, int color);

        void onColorRemoveClick(int position);
    }

    public void setItemClickCallback(final ItemClickCallback itemClickCallback) {
        this.itemClickCallback = itemClickCallback;
    }

    public OptionsColorsListAdapter(Context context, DataSource dataSource) {
        this.machineColorsGenerator = new MachineColorsGenerator(context, dataSource);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = new Button(context);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.button.setText(String.valueOf(position + 1));
        holder.button.getBackground().setColorFilter(machineColorsGenerator.getMachineColorsRawList().get(position).getValue(), PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public int getItemCount() {
        return machineColorsGenerator.getMachineColorsRawList().size();
    }

    public MachineColorsGenerator getMachineColorsGenerator() {
        return machineColorsGenerator;
    }

    public void addItem() {
        Log.v(TAG, "addItem color added");
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(context,
                ContextCompat.getColor(context, R.color.md_black_1000), new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                itemClickCallback.onColorAddClick(color, machineColorsGenerator.getMachineColorsRawList().size());
            }
        });
        dialog.show();
    }

    public void removeItem(MachineColor machineColor) {
        Log.v(TAG, "removeItem color removed");
        int position = machineColorsGenerator.getMachineColorsRawList().indexOf(machineColor);
        machineColorsGenerator.getMachineColorsRawList().remove(machineColor);
        notifyItemRemoved(position);
        //also change numbers of next elements
        notifyItemRangeChanged(position, machineColorsGenerator.getMachineColorsRawList().size() - position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        //listItem content
        private Button button;

        public ViewHolder(View itemView) {
            super(itemView);
            button = (Button) itemView;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            button.setLayoutParams(params);
            button.setOnClickListener(this);
            button.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            AmbilWarnaDialog dialog = new AmbilWarnaDialog(context,
                    machineColorsGenerator.getMachineColorsRawList().get(getAdapterPosition()).getValue(), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onCancel(AmbilWarnaDialog dialog) {

                }

                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    Log.v(TAG, "onOK color changed");
                    itemClickCallback.onColorUpdateClick(getAdapterPosition(), color);
                }
            });
            dialog.show();
        }

        @Override
        public boolean onLongClick(View view) {
            Log.v(TAG, "removeItem color removed");
            itemClickCallback.onColorRemoveClick(getAdapterPosition());
            return false;
        }
    }
}
