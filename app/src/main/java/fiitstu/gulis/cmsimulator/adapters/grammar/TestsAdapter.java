package fiitstu.gulis.cmsimulator.adapters.grammar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.dialogs.EditGrammarTestDialog;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class TestsAdapter extends RecyclerView.Adapter<TestsAdapter.TestHolder> {
    private static final String TAG = "TestsAdapter";

    private Context mContext;
    private List<String> listOfInputWords = new ArrayList<>();
    private OnDataSetChangedListener onDataSetChangedListener = null;

    public interface OnDataSetChangedListener {
        void OnDataChanged();
    }

    public void setOnDataSetChangedListener(OnDataSetChangedListener onDataSetChangedListener) {
        this.onDataSetChangedListener = onDataSetChangedListener;
    }

    public TestsAdapter(Context mContext, List<String> listOfInputWords) {
        this.mContext = mContext;
        this.listOfInputWords = listOfInputWords;
    }

    @NonNull
    @Override
    public TestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.list_element_test, parent, false);

        return new TestHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestHolder holder, final int position) {
        holder.resetDetails();
        final String inputWord = listOfInputWords.get(position);
        holder.positionTextView.setText(Integer.toString(position + 1) + ".");

        holder.inputWordTextView.setText(inputWord);
        holder.inputWordDetailsTextView.setText(inputWord);

        holder.removeTestImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataSource dataSource = DataSource.getInstance();
                dataSource.open();
                dataSource.deleteGrammarTest(inputWord);
                dataSource.close();

                TestsAdapter.this.deleteTest(position);
            }
        });

        holder.editTestImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditGrammarTestDialog editGrammarTestDialog = new EditGrammarTestDialog(inputWord, position, TestsAdapter.this);
                FragmentManager fm = ((FragmentActivity)mContext).getSupportFragmentManager();
                editGrammarTestDialog.show(fm, TAG);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listOfInputWords.size();
    }

    class TestHolder extends RecyclerView.ViewHolder {
        private TextView positionTextView;
        private TextView inputWordTextView;
        private TextView testStatusTextView;
        private ImageButton editTestImageButton;
        private ImageButton removeTestImageButton;
        private ImageButton moreImageButton;
        private TextView inputWordDetailsTextView;
        private LinearLayout detailsLayout;

        private boolean detailsShown = false;

        public TestHolder(View itemView) {
            super(itemView);

            this.positionTextView = itemView.findViewById(R.id.textView_list_test_position);
            this.inputWordTextView = itemView.findViewById(R.id.textView_list_test_word);
            this.testStatusTextView = itemView.findViewById(R.id.textView_list_test_status);
            this.editTestImageButton = itemView.findViewById(R.id.imageButton_list_test_edit);
            this.removeTestImageButton = itemView.findViewById(R.id.imageButton_list_test_remove);
            this.moreImageButton = itemView.findViewById(R.id.imageButton_list_test_more);
            this.inputWordDetailsTextView = itemView.findViewById(R.id.textView_list_test_word_details);
            this.detailsLayout = itemView.findViewById(R.id.linearlayout_test_details);

            moreImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    detailsShown = !detailsShown;
                    moreImageButton.setImageResource(detailsShown ? R.drawable.baseline_arrow_drop_up_24 : R.drawable.baseline_arrow_drop_down_24);
                    testStatusTextView.setVisibility(detailsShown ? View.VISIBLE : View.GONE);
                    inputWordTextView.setVisibility(detailsShown ? View.VISIBLE : View.GONE);
                    detailsLayout.setVisibility(detailsShown ? View.VISIBLE : View.GONE);
                }
            });
        }

        public void resetDetails() {
            this.detailsShown = false;
        }
    }

    public void addNewTest(String inputWord) {
        int position = listOfInputWords.size();
        listOfInputWords.add(inputWord);
        this.notifyItemInserted(position);
        if (onDataSetChangedListener != null)
            onDataSetChangedListener.OnDataChanged();
    }

    public void deleteTest(int position) {
        listOfInputWords.remove(position);
        this.notifyItemRemoved(position);
        if (onDataSetChangedListener != null)
            onDataSetChangedListener.OnDataChanged();
    }

    public void updateTest(String inputWord, int position)
    {
        listOfInputWords.set(position, inputWord);
        notifyItemChanged(position);
    }
}
