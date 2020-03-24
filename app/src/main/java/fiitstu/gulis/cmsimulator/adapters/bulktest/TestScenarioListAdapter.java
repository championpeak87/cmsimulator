package fiitstu.gulis.cmsimulator.adapters.bulktest;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.app.CMSimulator;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.elements.TestScenario;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter for listing test scenarios, showing their index, input word, and optionally
 * also the result of their running. Also supports customizable background color for each entry.
 * <p>
 * Created by Jakub Sedlář on 21.01.2018.
 */
public class TestScenarioListAdapter extends RecyclerView.Adapter<TestScenarioListAdapter.ViewHolder> {

    public enum Status {
        ACCEPT(R.string.accept, Color.GREEN),
        REJECT(R.string.reject, 0xFFFF3333), //light red
        INCORRECT_OUTPUT(R.string.incorrect_output, 0xFFFF3333), //light red
        CORRECT_OUTPUT_REJECTED(R.string.correct_output_but_reject, Color.YELLOW),
        TOOK_TOO_LONG(R.string.not_halt, 0xFFFF8000); //orange

        public final String text;


        /**
         * The highlight color of the row
         */
        @ColorInt
        public final int color;

        Status(@StringRes int text, int color) {
            this.text = CMSimulator.getContext().getResources().getString(text);
            this.color = color;
        }
    }

    //log tag
    private static final String TAG = TestScenarioListAdapter.class.getName();

    private List<TestScenario> items;
    private boolean editable;
    private LayoutInflater inflater;
    private ItemClickCallback itemClickCallback;

    private SparseIntArray rowColors = new SparseIntArray();
    private SparseArray<String> statuses = new SparseArray<>();

    public interface ItemClickCallback {
        void onLongClick(TestScenario test);

        void onEditItemClick(TestScenario test);

        void onRemoveItemClick(TestScenario test);
    }

    private OnTestScenarioListChange onTestScenarioListChange;

    public interface OnTestScenarioListChange {
        void OnListChange();
    }

    public void setOnTestScenarioListChange(OnTestScenarioListChange onTestScenarioListChange) {
        this.onTestScenarioListChange = onTestScenarioListChange;
    }

    public void setItemClickCallback(final ItemClickCallback itemClickCallback) {
        this.itemClickCallback = itemClickCallback;
    }

    public TestScenarioListAdapter(Context context, boolean editable) {
        this.items = new ArrayList<>();
        this.editable = editable;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_element_test, parent, false);
        return new ViewHolder(view, editable);
    }

    /**
     * Sets the background color of all rows to transparent
     */
    public void clearRowColors() {
        rowColors.clear();
        notifyDataSetChanged();
    }

    /**
     * Sets the row's status/result message
     *
     * @param row    the row index
     * @param status the status of the test
     */
    public void setRowStatus(int row, Status status) {
        statuses.put(row, status.text);
        rowColors.put(row, status.color);
        notifyItemChanged(row);
    }

    public void notifyItemChanged(TestScenario testScenario) {
        notifyItemChanged(items.indexOf(testScenario));
        if (this.onTestScenarioListChange != null)
            this.onTestScenarioListChange.OnListChange();
    }

    /**
     * Deletes all status messages previously set by
     */
    public void clearStatuses() {
        statuses.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.resetDetails();
        TestScenario test = items.get(position);
        holder.valueTextView.setText(Symbol.listToWord(test.getInputWord()));
        holder.inputWordDetails.setText(Symbol.listToWord(test.getInputWord()));

        if (test.getOutputWord() != null) {
            holder.outputWordDetails.setText(Symbol.listToWord(test.getOutputWord()));
        } else {
            holder.outputWordDetails.setVisibility(View.GONE);
            holder.outputWordTestLabel.setVisibility(View.GONE);
        }

        holder.positionTextView.setText(String.valueOf(position + 1) + ".");

        holder.background.setBackgroundColor(rowColors.get(position));

        if (statuses.get(position) != null) {
            holder.setStatus(statuses.get(position));
        } else {
            holder.setStatus("");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public TestScenario getItem(int index) {
        return items.get(index);
    }

    public void setItems(List items) {
        //cannot set, because the same list is used in spinners
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
        if (this.onTestScenarioListChange != null)
            this.onTestScenarioListChange.OnListChange();
    }

    public void addItem(TestScenario item) {
        Log.v(TAG, "addItem item added");
        items.add(item);
        notifyItemInserted(items.size() - 1);
        if (this.onTestScenarioListChange != null)
            this.onTestScenarioListChange.OnListChange();
    }

    public void removeItem(TestScenario item) {
        Log.v(TAG, "removeItem item removed");
        int position = items.indexOf(item);
        items.remove(position);
        statuses.delete(position);
        rowColors.delete(position);
        notifyItemRemoved(position);
        //also change numbers of next elements
        notifyItemRangeChanged(position, items.size() - position);
        if (this.onTestScenarioListChange != null)
            this.onTestScenarioListChange.OnListChange();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        //listItem content
        private TextView positionTextView;
        private TextView valueTextView;
        private TextView statusTextView;
        private ImageButton editImageButton;
        private ImageButton removeImageButton;
        private ImageButton moreImageButton;
        private LinearLayout background;
        private LinearLayout detailsLayout;
        private TextView inputWordDetails;
        private TextView outputWordDetails;
        private TextView outputWordTestLabel;
        private boolean detailsShown = false;

        ViewHolder(View itemView, boolean editable) {
            super(itemView);
            positionTextView = itemView.findViewById(R.id.textView_list_test_position);
            valueTextView = itemView.findViewById(R.id.textView_list_test_word);
            statusTextView = itemView.findViewById(R.id.textView_list_test_status);
            editImageButton = itemView.findViewById(R.id.imageButton_list_test_edit);
            removeImageButton = itemView.findViewById(R.id.imageButton_list_test_remove);
            moreImageButton = itemView.findViewById(R.id.imageButton_list_test_more);
            detailsLayout = itemView.findViewById(R.id.linearlayout_test_details);
            inputWordDetails = itemView.findViewById(R.id.textView_list_test_word_details);
            outputWordDetails = itemView.findViewById(R.id.textView_list_test_output_word_details);
            outputWordTestLabel = itemView.findViewById(R.id.textview_output_word_test_label);


            if (editable) {
                editImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v(TAG, "editElement button click noted");
                        itemClickCallback.onEditItemClick(items.get(getAdapterPosition()));
                    }
                });
                removeImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v(TAG, "removeElement button click noted");
                        itemClickCallback.onRemoveItemClick(items.get(getAdapterPosition()));
                    }
                });
            } else {
                editImageButton.setVisibility(View.GONE);
                removeImageButton.setVisibility(View.GONE);
            }
            moreImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    detailsShown = !detailsShown;
                    valueTextView.setVisibility(detailsShown ? View.GONE : View.VISIBLE);
                    statusTextView.setVisibility(detailsShown ? View.VISIBLE : View.GONE);
                    detailsLayout.setVisibility(detailsShown ? View.VISIBLE : View.GONE);
                    moreImageButton.setImageResource(detailsShown ? R.drawable.baseline_arrow_drop_up_24 : R.drawable.baseline_arrow_drop_down_24);
                }
            });
            background = itemView.findViewById(R.id.linearLayout_list_test_background);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    itemClickCallback.onLongClick(items.get(getAdapterPosition()));
                    return true;
                }
            });
        }

        public void setStatus(String status) {
            statusTextView.setText(status);
        }

        public void resetDetails() {
            this.detailsShown = false;
            valueTextView.setVisibility(detailsShown ? View.GONE : View.VISIBLE);
            statusTextView.setVisibility(detailsShown ? View.VISIBLE : View.GONE);
            detailsLayout.setVisibility(detailsShown ? View.VISIBLE : View.GONE);
            moreImageButton.setImageResource(detailsShown ? R.drawable.baseline_arrow_drop_up_24 : R.drawable.baseline_arrow_drop_down_24);
        }

    }
}
