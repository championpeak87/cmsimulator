package fiitstu.gulis.cmsimulator.adapters.simulation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.TextViewCompat;
import android.util.TypedValue;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.app.CMSimulator;
import fiitstu.gulis.cmsimulator.elements.Symbol;

/**
 * Adapter for selecting a symbol to be displayed on a tape
 *
 * Created by Martin on 7. 3. 2017.
 */
public class MachineTapeSpinnerAdapter extends ArrayAdapter<Symbol> {

    //log tag
    private static final String TAG = MachineTapeSpinnerAdapter.class.getName();

    private LayoutInflater inflater;

    public MachineTapeSpinnerAdapter(Context context, List<Symbol> items) {
        super(context, R.layout.tape_spinner_list_item, items);
        this.inflater = LayoutInflater.from(context);
    }

    private class ViewHolder {
        private TextView label;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.tape_spinner_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.label = convertView.findViewById(android.R.id.text1);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //get specific element
        Symbol symbol = getItem(position);
        if (symbol != null) {
            viewHolder.label.setText(symbol.getValue());
        }
        viewHolder.label.setGravity(Gravity.CENTER);

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(viewHolder.label,
                1,
                CMSimulator.getTextSize(getContext(), android.R.attr.textAppearanceListItemSmall),
                1,
                TypedValue.COMPLEX_UNIT_PX);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.select_dialog_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.label = convertView.findViewById(android.R.id.text1);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //get specific element
        Symbol symbol = getItem(position);
        if (symbol != null) {
            viewHolder.label.setText(symbol.getValue());
        }
        return convertView;
    }
}