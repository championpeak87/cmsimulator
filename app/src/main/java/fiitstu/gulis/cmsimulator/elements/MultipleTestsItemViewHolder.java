package fiitstu.gulis.cmsimulator.elements;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import fiitstu.gulis.cmsimulator.R;

public class MultipleTestsItemViewHolder extends RecyclerView.ViewHolder {

    public EditText editText_input;
    public ThreeStateCheckBox checkBox_accept;
    public LinearLayout linearLayout_row;

    public MultipleTestsItemViewHolder(View itemView) {
        super(itemView);

        this.editText_input = itemView.findViewById(R.id.editText_multiple_tests_input);
        this.checkBox_accept = itemView.findViewById(R.id.checkBox_multiple_tests_accept);
        this.linearLayout_row = itemView.findViewById(R.id.linearLayout_multiple_tests_row);
    }
}
