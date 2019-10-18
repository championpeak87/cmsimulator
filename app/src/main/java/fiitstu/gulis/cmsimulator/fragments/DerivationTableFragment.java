package fiitstu.gulis.cmsimulator.fragments;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.List;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.grammar.DerivationTableAdapter;
import fiitstu.gulis.cmsimulator.elements.GeneratedWord;

public class DerivationTableFragment extends Fragment {

    private List<GeneratedWord> derivationSequence;
    private static final String DERIVATION_SEQUENCE = "Derivation Sequence";

    public DerivationTableFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_derivation_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerViewDerivation;

        DerivationTableAdapter derivationTableAdapter = new DerivationTableAdapter(derivationSequence);
        recyclerViewDerivation = getView().findViewById(R.id.recyclerView_derivation_table);
        recyclerViewDerivation.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDerivation.scrollToPosition(derivationSequence.size()-1);
        recyclerViewDerivation.setAdapter(derivationTableAdapter);
    }

    public static DerivationTableFragment newInstance(List<GeneratedWord> derivationSequence) {
        DerivationTableFragment derivationTableFragment = new DerivationTableFragment();

        Bundle args = new Bundle();
        args.putSerializable(DERIVATION_SEQUENCE, (Serializable)derivationSequence);
        derivationTableFragment.setArguments(args);

        return derivationTableFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            derivationSequence = (List<GeneratedWord>) getArguments().getSerializable(DERIVATION_SEQUENCE);
        }

    }

}
