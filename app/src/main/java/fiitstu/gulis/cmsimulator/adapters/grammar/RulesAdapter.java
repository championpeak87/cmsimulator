package fiitstu.gulis.cmsimulator.adapters.grammar;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.GrammarItemViewHolder;
import fiitstu.gulis.cmsimulator.elements.GrammarRule;

public class RulesAdapter extends RecyclerView.Adapter<GrammarItemViewHolder> {

    private int count;
    private List<GrammarRule> grammarRuleList;
    private EditText focusedEditText;

    public RulesAdapter(int count) {
        this.count = count;
        this.grammarRuleList = new ArrayList<>();

        for(int i = 0; i < this.count; i++) {
            this.grammarRuleList.add(new GrammarRule());
        }
    }

    @Override
    public GrammarItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grammar_item, parent, false);

        return new GrammarItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GrammarItemViewHolder holder, final int position) {

        final GrammarRule grammarRule = this.grammarRuleList.get(position);

        holder.editText_grammar_table_left.setText(grammarRule.getGrammarLeft());
        holder.editText_grammar_table_right.setText(grammarRule.getGrammarRight());

        holder.editText_grammar_table_left.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals(""))
                    grammarRule.setGrammarLeft(null);
                else
                    grammarRule.setGrammarLeft(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        holder.editText_grammar_table_right.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals(""))
                    grammarRule.setGrammarRight(null);
                else
                    grammarRule.setGrammarRight(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        holder.editText_grammar_table_left.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    focusedEditText = holder.editText_grammar_table_left;
                }
            }
        });

        holder.editText_grammar_table_right.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    focusedEditText = holder.editText_grammar_table_right;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.count;
    }

    public void insertSpecialChar(String character) {
        if(focusedEditText != null) {
            focusedEditText.getText().insert(focusedEditText.getSelectionStart(), character);
        }

    }

    public void setGrammarRuleList(List<GrammarRule> grammarRuleList){
        for(int i = 0; i < grammarRuleList.size() ; i++)
            this.grammarRuleList.add(i, grammarRuleList.get(i));
    }

    public List<GrammarRule> getGrammarRuleList() {
        return grammarRuleList;
    }

    public void addRow(){
        this.count++;
        this.grammarRuleList.add(new GrammarRule());
        notifyItemInserted(this.count-1);
    }
}