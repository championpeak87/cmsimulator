package fiitstu.gulis.cmsimulator.adapters.configuration;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.Symbol;

/**
 * A SpinnerAdapter for selecting stack symbols. Used together with {@link ConfigurationStackListAdapter}
 *
 * Created by Martin on 26. 3. 2017.
 */
public class ConfigurationStackSpinnerAdapter extends ArrayAdapter<Symbol> {

    //log tag
    private static final String TAG = ConfigurationStackSpinnerAdapter.class.getName();

    private List<Symbol> items;
    private LayoutInflater inflater;

    public ConfigurationStackSpinnerAdapter(Context context, List<Symbol> items) {
        super(context, android.R.layout.select_dialog_item, items);
        this.items = items;
        items.add(new Symbol(-1, "<" + getContext().getResources().getString(R.string.new_symbol) + ">"));
        this.inflater = LayoutInflater.from(context);
    }

    public List<Symbol> getItems() {
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
