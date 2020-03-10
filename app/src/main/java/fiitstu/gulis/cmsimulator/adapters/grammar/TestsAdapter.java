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
import fiitstu.gulis.cmsimulator.elements.TestWord;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class TestsAdapter extends RecyclerView.Adapter<TestsAdapter.TestHolder> {
    private static final String TAG = "TestsAdapter";

    private Context mContext;
    private List<String> listOfInputWords;
    private List<TestWord> testWordList = new ArrayList<>();
    private OnDataSetChangedListener onDataSetChangedListener = null;
    private boolean solveMode = false;

    public interface OnDataSetChangedListener {
        void OnDataChanged();
    }

    public List<String> getListOfInputWords() {
        return listOfInputWords;
    }

    public void setOnDataSetChangedListener(OnDataSetChangedListener onDataSetChangedListener) {
        this.onDataSetChangedListener = onDataSetChangedListener;
    }

    public TestsAdapter(Context mContext, List<String> listOfInputWords, boolean solveMode) {
        this.mContext = mContext;
        this.listOfInputWords = listOfInputWords;
        this.solveMode = solveMode;

        for (String inputWord :
                listOfInputWords) {
            testWordList.add(new TestWord(inputWord, 0, null));
        }
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
        final TestWord testWord = testWordList.get(position);
        holder.positionTextView.setText(Integer.toString(position + 1) + ".");

        holder.inputWordTextView.setText(inputWord);
        holder.inputWordDetailsTextView.setText(inputWord);

        if (testWord.getResult() != null) {
            holder.linearLayout_list_test_background.setBackgroundColor(mContext.getColor(testWord.getResult() ? R.color.md_green_400 : R.color.md_red_500));
            holder.testStatusTextView.setText(testWord.getResult() ? R.string.accept : R.string.reject);
        }

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
                FragmentManager fm = ((FragmentActivity) mContext).getSupportFragmentManager();
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
        private LinearLayout linearLayout_list_test_background;

        private TextView outputTestLabel;
        private TextView outputTest;

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
            this.linearLayout_list_test_background = itemView.findViewById(R.id.linearLayout_list_test_background);

            this.outputTestLabel = itemView.findViewById(R.id.textview_output_word_test_label);
            this.outputTestLabel.setVisibility(View.GONE);

            this.outputTest = itemView.findViewById(R.id.textView_list_test_output_word_details);
            this.outputTest.setVisibility(View.GONE);

            moreImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    detailsShown = !detailsShown;
                    moreImageButton.setImageResource(detailsShown ? R.drawable.baseline_arrow_drop_up_24 : R.drawable.baseline_arrow_drop_down_24);
                    testStatusTextView.setVisibility(detailsShown ? View.VISIBLE : View.GONE);
                    inputWordTextView.setVisibility(detailsShown ? View.GONE : View.VISIBLE);
                    detailsLayout.setVisibility(detailsShown ? View.VISIBLE : View.GONE);
                }
            });

            if (solveMode) {
                removeTestImageButton.setVisibility(View.GONE);
                editTestImageButton.setVisibility(View.GONE);
            }
        }

        public void resetDetails() {
            this.detailsShown = false;

            inputWordTextView.setVisibility(detailsShown ? View.GONE : View.VISIBLE);
            testStatusTextView.setVisibility(detailsShown ? View.VISIBLE : View.GONE);
            detailsLayout.setVisibility(detailsShown ? View.VISIBLE : View.GONE);
            moreImageButton.setImageResource(detailsShown ? R.drawable.baseline_arrow_drop_up_24 : R.drawable.baseline_arrow_drop_down_24);
        }
    }

    public void addNewTest(String inputWord) {
        int position = listOfInputWords.size();
        listOfInputWords.add(inputWord);
        testWordList.add(new TestWord(inputWord, 0, null));
        this.notifyItemInserted(position);
        if (onDataSetChangedListener != null)
            onDataSetChangedListener.OnDataChanged();
    }

    public void deleteTest(int position) {
        listOfInputWords.remove(position);
        testWordList.remove(position);
        this.notifyItemRemoved(position);

        int sizeOfList = listOfInputWords.size();
        for (int i = position; i < sizeOfList; i++)
            this.notifyItemChanged(i);
        if (onDataSetChangedListener != null)
            onDataSetChangedListener.OnDataChanged();
    }

    public void updateTest(String inputWord, int position) {
        listOfInputWords.set(position, inputWord);
        testWordList.set(position, new TestWord(inputWord, 0, null));
        notifyItemChanged(position);
    }

    public void markTestResult(String inputWord, boolean result) {
        for (int i = 0; i < listOfInputWords.size(); i++) {
            final String word = listOfInputWords.get(i);
            if (word.equals(inputWord)) {
                for (TestWord testWord :
                        testWordList) {
                    if (testWord.getWord().equals(inputWord)) {
                        testWord.setResult(true);
                        notifyItemChanged(i);
                        break;
                    }
                }
                break;
            }
        }
    }
}
