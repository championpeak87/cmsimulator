package fiitstu.gulis.cmsimulator.adapters.configuration;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import fiitstu.gulis.cmsimulator.activities.ConfigurationActivity;
import fiitstu.gulis.cmsimulator.elements.State;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.R;

/**
 * SpinnerAdapter for selecting symbols and states
 *
 * Created by Martin on 9. 2. 2017.
 */
public class ConfigurationSpinnerAdapter extends ArrayAdapter {

    //log tag
    private static final String TAG = ConfigurationSpinnerAdapter.class.getName();

    private List items;
    private int elementType;
    private LayoutInflater inflater;

    public ConfigurationSpinnerAdapter(Context context, List items, int elementType) {
        super(context, android.R.layout.simple_spinner_item, items);
        this.items = items;
        if (elementType == ConfigurationActivity.INPUT_SYMBOL) {
            this.items.add(new Symbol(-1, "<" + getContext().getResources().getString(R.string.new_symbol) + ">"));
        }
        else if (elementType == ConfigurationActivity.STATE) {
            this.items.add(new State(-1, "<" + getContext().getResources().getString(R.string.new_state) + ">", 0, 0, false, false));
        }
        this.elementType = elementType;
        this.inflater = LayoutInflater.from(context);
    }

    public List getItems() {
        return items;
    }

    private class ViewHolder {
        private TextView label;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.label = convertView.findViewById(android.R.id.text1);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        switch (elementType) {
            case ConfigurationActivity.INPUT_SYMBOL:
                //get specific element
                Symbol symbol = (Symbol) getItem(position);
                if (symbol != null) {
                    viewHolder.label.setText(symbol.getValue());
                }
                break;
            case ConfigurationActivity.STATE:
                //get specific element
                State state = (State) getItem(position);
                if (state != null) {
                    viewHolder.label.setText(state.getValue() +
                            (state.isInitialState() ? " " + getContext().getResources().getString(R.string.initial_state_mark) : "") +
                            (state.isFinalState() ? " " + getContext().getResources().getString(R.string.final_state_mark) : ""));
                }
                break;
        }
        return convertView;
    }

    @Override
    public View getDropDownView(final int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.select_dialog_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.label = convertView.findViewById(android.R.id.text1);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        switch (elementType) {
            case ConfigurationActivity.INPUT_SYMBOL:
                //get specific element
                Symbol symbol = (Symbol) getItem(position);
                if (symbol != null) {
                    viewHolder.label.setText(symbol.getValue());
                }
                break;
            case ConfigurationActivity.STATE:
                //get specific element
                State state = (State) getItem(position);
                if (state != null) {
                    viewHolder.label.setText(state.getValue() +
                            (state.isInitialState() ? " " + getContext().getResources().getString(R.string.initial_state_mark) : "") +
                            (state.isFinalState() ? " " + getContext().getResources().getString(R.string.final_state_mark) : ""));
                }
                break;
        }

        return convertView;
    }

}
