package fiitstu.gulis.cmsimulator.fragments;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.GeneratedWord;

public class FixedDerivationFragment extends Fragment {

    private List<GeneratedWord> derivationSequence;
    private boolean lastStep;
    private static final String DERIVATION_SEQUENCE = "Derivation Sequence";
    private static  final String LAST_STEP = "Last Step";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_derivation_fixed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView fixedDerivationTextView = getView().findViewById(R.id.textView_fixed_derivation);
        TextView fixedDerivationProductionsTextView = getView().findViewById(R.id.textView_fixed_derivation_productions);
        final ScrollView fixedDerivationScrollView = getView().findViewById(R.id.scrollView_fixed_derivation);
        String simulationString = "";
        String currentDerivation = "";
        int startGreen, endGreen;
        SpannableString spannableCurrentDerivation = new SpannableString(currentDerivation);
        GeneratedWord generatedWord;

        for(int i = 0; i < derivationSequence.size(); i++){
            generatedWord = derivationSequence.get(i);
            if(generatedWord.getUsedRule() != null){
                simulationString = simulationString.concat(generatedWord.getUsedRule().getGrammarLeft())
                        .concat(" -> ")
                        .concat(generatedWord.getUsedRule().getGrammarRight())
                        .concat(", ");

                currentDerivation = generatedWord.getWord();
                if(i == derivationSequence.size()-1) {
                    startGreen = currentDerivation.lastIndexOf(derivationSequence.get(derivationSequence.size() - 1).getUsedRule().getGrammarRight());
                    endGreen = startGreen + derivationSequence.get(derivationSequence.size() - 1).getUsedRule().getGrammarRight().length();

                    spannableCurrentDerivation = new SpannableString(currentDerivation);
                    if(!derivationSequence.get(derivationSequence.size()-1).getUsedRule().getGrammarRight().equals("ε") && !lastStep) {
                        spannableCurrentDerivation.setSpan(new BackgroundColorSpan(Color.GREEN), startGreen, endGreen, 0);
                    }
                }
            }else{
                currentDerivation = generatedWord.getWord();
                spannableCurrentDerivation = new SpannableString(currentDerivation);
            }
        }

        if(lastStep){
            simulationString = simulationString.substring(0, simulationString.length()-2);
            fixedDerivationTextView.setTypeface(null, Typeface.BOLD);
            fixedDerivationTextView.setPaintFlags(fixedDerivationTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
        fixedDerivationTextView.setText(spannableCurrentDerivation);
        fixedDerivationProductionsTextView.setText(simulationString);
        fixedDerivationScrollView.post(new Runnable() {
            public void run() {
                fixedDerivationScrollView.fullScroll(fixedDerivationScrollView.FOCUS_DOWN);
            }
        });
    }

    public static FixedDerivationFragment newInstance(List<GeneratedWord> derivationSequence, boolean lastStep) {
        FixedDerivationFragment fixedDerivationFragment = new FixedDerivationFragment();

        Bundle args = new Bundle();
        args.putSerializable(DERIVATION_SEQUENCE, (Serializable)derivationSequence);
        args.putBoolean(LAST_STEP, lastStep);
        fixedDerivationFragment.setArguments(args);

        return fixedDerivationFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            derivationSequence = (List<GeneratedWord>) getArguments().getSerializable(DERIVATION_SEQUENCE);
            lastStep = getArguments().getBoolean(LAST_STEP);
        }
    }
}
