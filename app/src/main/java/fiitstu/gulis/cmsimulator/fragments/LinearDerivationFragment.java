package fiitstu.gulis.cmsimulator.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.GeneratedWord;

public class LinearDerivationFragment extends Fragment {

    private List<GeneratedWord> derivationSequence;
    private boolean lastStep;
    private static final String DERIVATION_SEQUENCE = "Derivation Sequence";
    private static final String LAST_STEP = "Last Step";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_derivation_linear, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView linearDerivationTextView = getView().findViewById(R.id.textView_linear_derivation);
        final ScrollView linearDerivationScrollView = getView().findViewById(R.id.scrollView_linear_derivation);
        String simulationString = "";

        GeneratedWord generatedWord;
        SpannableString spannableSimulationString = new SpannableString(simulationString);
        int startRed, endRed, startGreen, endGreen;
        for(int i = 0; i < derivationSequence.size(); i++){
            generatedWord = derivationSequence.get(i);
            if(generatedWord.getUsedRule() == null){
                simulationString = simulationString.concat(generatedWord.getWord());
                spannableSimulationString = new SpannableString(simulationString);
            }else{
                if(i == derivationSequence.size()-1){
                    startRed = simulationString.lastIndexOf(derivationSequence.get(derivationSequence.size()-1).getUsedRule().getGrammarLeft());
                    endRed = startRed + derivationSequence.get(derivationSequence.size()-1).getUsedRule().getGrammarLeft().length();

                    simulationString = simulationString.concat(" => ").concat(generatedWord.getWord());

                    startGreen = simulationString.lastIndexOf(derivationSequence.get(derivationSequence.size()-1).getUsedRule().getGrammarRight());
                    endGreen = startGreen + derivationSequence.get(derivationSequence.size()-1).getUsedRule().getGrammarRight().length();

                    spannableSimulationString = new SpannableString(simulationString);
                    if(!lastStep) {
                        spannableSimulationString.setSpan(new BackgroundColorSpan(Color.YELLOW), startRed, endRed, 0);
                        if (!derivationSequence.get(derivationSequence.size() - 1).getUsedRule().getGrammarRight().equals("ε")) {
                            spannableSimulationString.setSpan(new BackgroundColorSpan(Color.GREEN), startGreen, endGreen, 0);
                        }
                    }else{
                        spannableSimulationString.setSpan(new StyleSpan(Typeface.BOLD), simulationString.lastIndexOf("=>")+3, spannableSimulationString.length(), 0);
                        spannableSimulationString.setSpan(new UnderlineSpan(), simulationString.lastIndexOf("=>")+3, spannableSimulationString.length(), 0);
                    }
                }else{
                    simulationString = simulationString.concat(" => ").concat(generatedWord.getWord());
                }
            }
        }

        linearDerivationTextView.setText(spannableSimulationString);
        linearDerivationScrollView.post(new Runnable() {
            public void run() {
                linearDerivationScrollView.fullScroll(linearDerivationScrollView.FOCUS_DOWN);
            }
        });
    }

    public static LinearDerivationFragment newInstance(List<GeneratedWord> derivationSequence, boolean lastStep) {
        LinearDerivationFragment linearDerivationFragment = new LinearDerivationFragment();

        Bundle args = new Bundle();
        args.putSerializable(DERIVATION_SEQUENCE, (Serializable)derivationSequence);
        args.putBoolean(LAST_STEP, lastStep);
        linearDerivationFragment.setArguments(args);

        return linearDerivationFragment;
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
