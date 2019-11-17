package fiitstu.gulis.cmsimulator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import android.widget.RadioButton;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.MainActivity;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;

/**
 * A dialog for selecting tye of machine to be created
 *
 * Created by Martin on 15. 4. 2017.
 */
public class NewMachineDialog extends DialogFragment {
    private RadioButton finiteAutomata;
    private RadioButton pushdownAutomata;
    private RadioButton linearBoundedAutomata;
    private RadioButton turingMachine;

    private automata_type selectedAutomata = automata_type.FINITE_AUTOMATA;

    private NewMachineDialogListener listener;

    public interface NewMachineDialogListener{
        void newMachineDialogClick(Bundle outputBundle);
    }

    public static NewMachineDialog newInstance()
    {
        return new NewMachineDialog();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        listener = (NewMachineDialogListener)context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder((getActivity()));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_new_machine, null);

        builder.setView(view)
                .setTitle(R.string.new_machine)
                .setNeutralButton(R.string.cancel, null)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle bundle = getOutputBundle();
                        listener.newMachineDialogClick(bundle);
                    }
                });

        finiteAutomata = view.findViewById(R.id.radiobutton_finite_automata);
        finiteAutomata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAutomata = automata_type.FINITE_AUTOMATA;
            }
        });

        pushdownAutomata = view.findViewById(R.id.radiobutton_pushdown_automata);
        pushdownAutomata.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                selectedAutomata = automata_type.PUSHDOWN_AUTOMATA;
            }
        });

        linearBoundedAutomata = view.findViewById(R.id.radiobutton_linear_bounded_automata);
        linearBoundedAutomata.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                selectedAutomata = automata_type.LINEAR_BOUNDED_AUTOMATA;
            }
        });

        turingMachine = view.findViewById(R.id.radiobutton_turing_machine);
        turingMachine.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                selectedAutomata = automata_type.TURING_MACHINE;
            }
        });

        return builder.create();
    }

    private Bundle getOutputBundle()
    {
        Bundle outputBundle = new Bundle();
        outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.NEW_MACHINE);
        switch (selectedAutomata)
        {
            case FINITE_AUTOMATA:
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
                break;
            case PUSHDOWN_AUTOMATA:
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.PUSHDOWN_AUTOMATON);
                break;
            case LINEAR_BOUNDED_AUTOMATA:
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.LINEAR_BOUNDED_AUTOMATON);
                break;
            case TURING_MACHINE:
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.TURING_MACHINE);
                break;
        }

       return outputBundle;
    }

}
