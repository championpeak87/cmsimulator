package fiitstu.gulis.cmsimulator.adapters.grammar;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.MultipleTestsItemViewHolder;
import fiitstu.gulis.cmsimulator.elements.TestWord;

public class MultipleTestsAdapter extends RecyclerView.Adapter<MultipleTestsItemViewHolder> {

    private int count;
    private List<TestWord> testWordList;

    public MultipleTestsAdapter(int count){
        this.count = count;
        this.testWordList = new ArrayList<>();

        for(int i = 0; i < this.count;i++){
            this.testWordList.add(new TestWord());
        }
    }

    @Override
    public MultipleTestsItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.multiple_tests_item, parent, false);

        return new MultipleTestsItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MultipleTestsItemViewHolder holder, int position) {

        final TestWord testWord = testWordList.get(position);

        if(testWord.getResult() != null) {
            holder.checkBox_accept.setState(testWord.isAccepted());
            holder.checkBox_accept.setButtonDrawable(R.drawable.ic_checkbox_unchecked_red);
            if (!testWord.getResult()) {
                holder.linearLayout_row.setBackgroundColor(Color.RED);
                if(testWord.isAccepted() == -1){
                    holder.checkBox_accept.setButtonDrawable(R.drawable.ic_checkbox_indeterminate_green);
                }
                else if(testWord.isAccepted() == 1){
                    holder.checkBox_accept.setButtonDrawable(R.drawable.ic_checkbox_checked_red);
                }
            } else if (testWord.getResult()) {
                holder.linearLayout_row.setBackgroundColor(Color.GREEN);
                if(testWord.isAccepted() == -1){
                    holder.checkBox_accept.setButtonDrawable(R.drawable.ic_checkbox_indeterminate_red);
                }
                else if(testWord.isAccepted() == 1){
                    holder.checkBox_accept.setButtonDrawable(R.drawable.ic_checkbox_checked_green);
                }
            }
        }
        holder.editText_input.setText(testWord.getWord());

        holder.editText_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                testWord.setWord(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        holder.checkBox_accept.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                testWord.setAccepted(holder.checkBox_accept.getState());
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.count;
    }

    public List<TestWord> getTestWordList(){return this.testWordList;}

    public void setTestWordList(List<TestWord> testWordList){
        for(int i = 0; i< testWordList.size(); i++){
            this.testWordList.add(i, testWordList.get(i));
        }
    }

}
