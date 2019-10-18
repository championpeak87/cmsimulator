package fiitstu.gulis.cmsimulator.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.diagram.DerivationTreeNode;
import fiitstu.gulis.cmsimulator.diagram.DerivationTreeView;
import fiitstu.gulis.cmsimulator.elements.DerivationTreeStateNode;
import fiitstu.gulis.cmsimulator.elements.GeneratedWord;

public class DerivationTreeFragment extends Fragment {

    private List<GeneratedWord> derivationSequence;
    private List<DerivationTreeStateNode> derivationTreeStateNodeList;
    private List<DerivationTreeStateNode> predecessorList;
    private static final String DERIVATION_SEQUENCE = "Derivation Sequence";

    public DerivationTreeFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_derivation_tree, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DerivationTreeView derivationTreeView = getView().findViewById(R.id.view_grammar_simulation_derivation_tree);
        GeneratedWord generatedWord;
        char[] rightRuleCharArray;
        derivationTreeStateNodeList = new ArrayList<>();
        predecessorList = new ArrayList<>();
        DerivationTreeStateNode derivationTreeStateNode;
        DerivationTreeStateNode predecessorNode;
        int nodeToRomove = -1;

        if(derivationSequence.size() > 0) {
            derivationTreeStateNode = new DerivationTreeStateNode(derivationSequence.get(0).getWord().charAt(0), null, 0, 0, 0, 1, Color.YELLOW);
            predecessorNode = derivationTreeStateNode;
            predecessorList.add(0, predecessorNode);
            derivationTreeStateNodeList.add(derivationTreeStateNode);
        }

        for(int i = 1; i < derivationSequence.size(); i++){
            generatedWord = derivationSequence.get(i);
            if(nodeToRomove != -1) {
                predecessorList.remove(nodeToRomove);
            }
            rightRuleCharArray = generatedWord.getUsedRule().getGrammarRight().toCharArray();

            for(int j = 0; j < generatedWord.getUsedRule().getGrammarRight().length(); j++) {
                derivationTreeStateNode = new DerivationTreeStateNode(rightRuleCharArray[j], null, 0, 0, i, generatedWord.getUsedRule().getGrammarRight().length(), 0);

                for(int k = 0; k < predecessorList.size(); k++){
                    if(String.valueOf(predecessorList.get(k).getSymbol()).equals(generatedWord.getUsedRule().getGrammarLeft())){
                        derivationTreeStateNode.setPredecessor(predecessorList.get(k));
                        nodeToRomove = k;
                        break;
                    }
                }
                if(Character.isUpperCase(rightRuleCharArray[j])){
                    derivationTreeStateNode.setColor(Color.YELLOW);
                    predecessorList.add(derivationTreeStateNode);
                }else{
                    derivationTreeStateNode.setColor(Color.GREEN);
                }

                derivationTreeStateNodeList.add(derivationTreeStateNode);
            }
        }
        derivationTreeView.drawTree(derivationTreeStateNodeList);
    }

    public static DerivationTreeFragment newInstance(List<GeneratedWord> derivationSequence) {
        DerivationTreeFragment derivationTreeFragment = new DerivationTreeFragment();

        Bundle args = new Bundle();
        args.putSerializable(DERIVATION_SEQUENCE, (Serializable)derivationSequence);
        derivationTreeFragment.setArguments(args);

        return derivationTreeFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            derivationSequence = (List<GeneratedWord>) getArguments().getSerializable(DERIVATION_SEQUENCE);
        }
    }

}
