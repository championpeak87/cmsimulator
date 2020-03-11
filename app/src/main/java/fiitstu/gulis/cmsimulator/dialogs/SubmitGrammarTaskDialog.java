package fiitstu.gulis.cmsimulator.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.UsersManagmentActivity;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.elements.GrammarRule;
import fiitstu.gulis.cmsimulator.elements.TestWord;
import fiitstu.gulis.cmsimulator.elements.UniqueQueue;
import fiitstu.gulis.cmsimulator.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ValidFragment")
public class SubmitGrammarTaskDialog extends DialogFragment {
    private static final String TAG = "SubmitGrammarTaskDialog";

    private List<String> inputWordList;
    private UniqueQueue<String> queue = new UniqueQueue<>();
    private String grammarType;

    // UI ELEMENTS
    private ProgressBar progressBar_test_executing;
    private LinearLayout linearlayout_submit_warning_message;
    private EditText edittext_positive_test_results;
    private ProgressBar progressBar_test_executing_values;

    @SuppressLint("ValidFragment")
    public SubmitGrammarTaskDialog(String grammarType) {
        DataSource dataSource = DataSource.getInstance();
        dataSource.open();
        this.inputWordList = dataSource.getGrammarTests();
        this.grammarType = grammarType;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.dialog_submit_grammar_task, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.submit_task)
                .setView(view)
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.submit_task, null);

        setUIElements(view);

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        View view = getView();
        AlertDialog dialog = (AlertDialog) getDialog();
        final Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        class ExecuteTestsAsync extends AsyncTask<Void, Integer, Void> {
            private boolean hasRejectedTests = false;
            private int acceptedTests = 0;
            private int numberOfTests = inputWordList.size();

            @Override
            protected void onPreExecute() {
                positiveButton.setEnabled(false);
                showLoadingScreen(true);
                progressBar_test_executing_values.setMax(numberOfTests);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                List<String> stringList = inputWordList;
                List<TestWord> testWordList = new ArrayList<>();
                for (String test : stringList) {
                    testWordList.add(new TestWord(test, 0, false));
                }

                String result;

                for (int i = 0; i < testWordList.size(); i++) {
                    TestWord testWord = testWordList.get(i);
                    if (testWord.getWord() != null) {
                        result = simulateGrammar(testWord.getWord(), grammarType);

                        if (result.equals(getString(R.string.accept))) {
                            testWord.setResult(true);
                            acceptedTests++;
                        } else {
                            testWord.setResult(false);
                            hasRejectedTests = true;
                        }

                        testWordList.set(i, testWord);
                        queue.clear();
                    }
                    publishProgress(i);
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                String resultString = String.format("%d / %d", acceptedTests, numberOfTests);
                edittext_positive_test_results.setText(resultString);
                progressBar_test_executing_values.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                positiveButton.setEnabled(true);
                showLoadingScreen(false);
                linearlayout_submit_warning_message.setVisibility(hasRejectedTests ? View.VISIBLE : View.GONE);
                edittext_positive_test_results.setTextColor(getActivity().getColor(acceptedTests == numberOfTests ? R.color.md_green_400 : R.color.md_red_500));
            }
        }

        new ExecuteTestsAsync().execute();
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    throw new NotImplementedException(getContext());
                } catch (NotImplementedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String simulateGrammar(String input, String grammarType) {
        List<GrammarRule> startingRules = filterStartingRules();
        String current;
        StringBuilder temp;
        long startTime, stopTime;
        DataSource dataSource = DataSource.getInstance();
        dataSource.open();
        List<GrammarRule> grammarRuleList = dataSource.getGrammarRuleFullExtract();

        for (GrammarRule grammarRule : startingRules) {
            current = grammarRule.getGrammarRight();
            queue.add(current);
        }

        startTime = System.currentTimeMillis();
        while (!queue.isEmpty()) {
            current = queue.remove();
            stopTime = System.currentTimeMillis();

            if (current.equals(input)) {
                return getString(R.string.accept);
            }

            if (stopTime - startTime > 3000)
                return getString(R.string.reject);

            for (GrammarRule grammarRule : grammarRuleList) {
                int index = 0;

                String rightGrammarRule = grammarRule.getGrammarRight();
                String leftGrammarRule = grammarRule.getGrammarLeft();
                if (rightGrammarRule != null && leftGrammarRule != null)
                    while ((index = (current.indexOf(leftGrammarRule, index) + 1)) > 0) {
                        temp = new StringBuilder(current);
                        if (rightGrammarRule != null && leftGrammarRule != null && rightGrammarRule.equals("ε")) {
                            temp.replace(index - 1, index + leftGrammarRule.length() - 1, "");
                            if (temp.toString().equals(input)) {
                                return getString(R.string.accept);
                            }
                        } else {
                            if (leftGrammarRule != null && rightGrammarRule != null) {
                                temp.replace(index - 1, index + leftGrammarRule.length() - 1, rightGrammarRule);
                                if (temp.toString().equals(input)) {
                                    return getString(R.string.accept);
                                }
                            }
                        }

                        if (grammarType.equals("Unrestricted")) {
                            queue.add(temp.toString());
                        } else {
                            if (temp.length() <= input.length() + 1) {
                                queue.add(temp.toString());
                            }
                        }
                    }
            }
        }
        return getString(R.string.reject);
    }

    private List<GrammarRule> filterStartingRules() {
        List<GrammarRule> startingRules = new ArrayList<>();

        DataSource dataSource = DataSource.getInstance();
        dataSource.open();
        List<GrammarRule> grammarRuleList = dataSource.getGrammarRuleFullExtract();
        for (GrammarRule grammarRule : grammarRuleList) {
            if (grammarRule != null) {
                String leftRule = grammarRule.getGrammarLeft();
                if (leftRule != null && leftRule.equals("S")) {
                    startingRules.add(grammarRule);
                }
            }
        }

        return startingRules;
    }

    private void showLoadingScreen(boolean visibility) {
        this.progressBar_test_executing.setVisibility(visibility ? View.VISIBLE : View.GONE);
        this.progressBar_test_executing_values.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    private void setUIElements(View view) {
        this.progressBar_test_executing = view.findViewById(R.id.progressBar_test_executing);
        this.linearlayout_submit_warning_message = view.findViewById(R.id.linearlayout_submit_warning_message);
        this.edittext_positive_test_results = view.findViewById(R.id.edittext_positive_test_results);
        this.progressBar_test_executing_values = view.findViewById(R.id.progressBar_test_executing_values);
    }

}
