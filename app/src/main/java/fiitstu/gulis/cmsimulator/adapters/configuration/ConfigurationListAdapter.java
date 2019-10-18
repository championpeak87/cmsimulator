package fiitstu.gulis.cmsimulator.adapters.configuration;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.activities.ConfigurationActivity;
import fiitstu.gulis.cmsimulator.elements.State;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.elements.Transition;
import fiitstu.gulis.cmsimulator.R;

/**
 * An adapter for displaying list of states, symbols, or transitions.
 * Shows their index and for states and symbols their name, for transitions their formal notation.
 * Also offers buttons for editing or removing each element
 *
 * Created by Martin on 21. 2. 2017.
 */
public class ConfigurationListAdapter extends RecyclerView.Adapter<ConfigurationListAdapter.ViewHolder> {

    //log tag
    private static final String TAG = ConfigurationListAdapter.class.getName();

    private List items;
    private int elementType;
    private boolean hideFirstItem;
    private Context context;
    private LayoutInflater inflater;
    private ItemClickCallback itemClickCallback;

    public interface ItemClickCallback {
        void onEditItemClick(int position, int elementType);

        void onRemoveItemClick(int position, int elementType);
    }

    public void setItemClickCallback(final ItemClickCallback itemClickCallback) {
        this.itemClickCallback = itemClickCallback;
    }

    public ConfigurationListAdapter(Context context, int elementType, boolean hideFirstItem) {
        this.items = new ArrayList<>();
        this.elementType = elementType;
        this.hideFirstItem = hideFirstItem;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_element_configuration, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (hideFirstItem) {
            position += 1;
        }

        switch (elementType) {
            case ConfigurationActivity.INPUT_SYMBOL:
                Symbol inputSymbol = (Symbol) items.get(position);
                if (inputSymbol.isEmpty()) {
                    holder.valueTextView.setText(inputSymbol.getValue() + " " + context.getResources().getString(R.string.empty_symbol_mark));
                    holder.removeImageButton.setVisibility(View.INVISIBLE);
                }
                else if (inputSymbol.isRightBound()) {
                    holder.valueTextView.setText(inputSymbol.getValue() + " " + context.getResources().getString(R.string.right_bound_mark));
                    holder.removeImageButton.setVisibility(View.INVISIBLE);
                }
                else if (inputSymbol.isLeftBound()) {
                    holder.valueTextView.setText(inputSymbol.getValue() + " " + context.getResources().getString(R.string.left_bound_mark));
                    holder.removeImageButton.setVisibility(View.INVISIBLE);
                }
                else {
                    holder.valueTextView.setText(inputSymbol.getValue());
                    holder.removeImageButton.setVisibility(View.VISIBLE);
                }
                break;
            case ConfigurationActivity.STACK_SYMBOL:
                //first element for stack symbol is epsilon/blank
                Symbol stackSymbol = (Symbol) items.get(position);
                //second element for stack symbol is default, cannot be edited nor removed
                if (stackSymbol.isStackBotom()) {
                    holder.valueTextView.setText(stackSymbol.getValue() + " " + context.getResources().getString(R.string.start_stack_symbol_mark));
                    holder.removeImageButton.setVisibility(View.INVISIBLE);
                } else {
                    holder.valueTextView.setText(stackSymbol.getValue());
                    holder.removeImageButton.setVisibility(View.VISIBLE);
                }
                break;
            case ConfigurationActivity.STATE:
                State state = (State) items.get(position);
                holder.valueTextView.setText(state.getValue() +
                        (state.isInitialState() ? " " + context.getResources().getString(R.string.initial_state_mark) : "") +
                        (state.isFinalState() ? " " + context.getResources().getString(R.string.final_state_mark) : ""));
                break;
            default:
                //transitions
                Transition transition = (Transition) items.get(position);
                holder.valueTextView.setText(transition.getDesc());
                break;
        }
        holder.positionTextView.setText(String.valueOf(position + (hideFirstItem ? 0 : 1)) + ".");
    }

    @Override
    public int getItemCount() {
        if (hideFirstItem) {
            return items.size() - 1;
        } else {
            return items.size();
        }
    }

    public List getItems() {
        return items;
    }

    public void setItems(List items) {
        //cannot set, because the same list is used in spinners
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void addItem(Object item) {
        Log.v(TAG, "addItem item added");
        items.add(item);
        if (hideFirstItem) {
            //first element for input and stack symbol is epsilon/blank
            notifyItemInserted(items.size() - 2);
        } else {
            notifyItemInserted(items.size() - 1);
        }
    }

    public void removeItem(Object item) {
        Log.v(TAG, "removeItem item removed");
        int position = items.indexOf(item);
        items.remove(item);
        if (hideFirstItem) {
            //first element for input and stack symbol is epsilon/blank
            notifyItemRemoved(position - 1);
            //also change numbers of next elements
            notifyItemRangeChanged(position - 1, items.size() - position);
        } else {
            notifyItemRemoved(position);
            //also change numbers of next elements
            notifyItemRangeChanged(position, items.size() - position);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //listItem content
        private TextView positionTextView;
        private TextView valueTextView;
        private TextView statusTextView;
        private ImageButton editImageButton;
        private ImageButton removeImageButton;

        ViewHolder(View itemView) {
            super(itemView);
            positionTextView = (TextView) itemView.findViewById(R.id.textView_list_configuration_position);
            valueTextView = (TextView) itemView.findViewById(R.id.textView_list_configuration_value);
            editImageButton = (ImageButton) itemView.findViewById(R.id.imageButton_list_configuration_edit);
            editImageButton.setOnClickListener(this);
            removeImageButton = (ImageButton) itemView.findViewById(R.id.imageButton_list_configuration_remove);
            removeImageButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.imageButton_list_configuration_edit:
                    Log.v(TAG, "editElement button click noted");
                    if (hideFirstItem) {
                        //first element for input and stack symbol is epsilon/blank
                        itemClickCallback.onEditItemClick(getAdapterPosition() + 1, elementType);
                    } else {
                        itemClickCallback.onEditItemClick(getAdapterPosition(), elementType);
                    }
                    break;
                case R.id.imageButton_list_configuration_remove:
                    Log.v(TAG, "removeElement button click noted");
                    if (hideFirstItem) {
                        //first element for input and stack symbol is epsilon/blank
                        itemClickCallback.onRemoveItemClick(getAdapterPosition() + 1, elementType);
                    } else {
                        itemClickCallback.onRemoveItemClick(getAdapterPosition(), elementType);
                    }
                    break;
            }
        }
    }
}
