package fiitstu.gulis.cmsimulator.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.MainActivity;

/**
 * A dialog for selecting tye of machine to be created
 *
 * Created by Martin on 15. 4. 2017.
 */
public class NewMachineDialog extends DialogFragment implements View.OnClickListener {

    //log tag
    private static final String TAG = NewMachineDialog.class.getName();

    public interface NewMachineDialogListener {
        void newMachineDialogClick(Bundle outputBundle);
    }

    public NewMachineDialog() {
        // Empty constructor required for DialogFragment
    }

    public static NewMachineDialog newInstance() {
        return new NewMachineDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setTitle(R.string.new_machine);
        return inflater.inflate(R.layout.dialog_main_new, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //finite state automaton
        Button finiteStateAutomatonB = view.findViewById(R.id.button_popup_main_new_finite_state_automatom);
        finiteStateAutomatonB.setOnClickListener(this);

        //pushdown automaton
        Button pushdownAutomatonB = view.findViewById(R.id.button_popup_main_new_pushdown_automaton);
        pushdownAutomatonB.setOnClickListener(this);

        //linear bounded automaton
        Button linearBoundedAutomatonB = view.findViewById(R.id.button_popup_main_new_linear_bounded_automaton);
        linearBoundedAutomatonB.setOnClickListener(this);

        //turing machine
        Button turingMachineB = view.findViewById(R.id.button_popup_main_new_turing_machine);
        turingMachineB.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.8),
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.8));
        }
    }

    @Override
    public void onClick(View view) {
        Bundle outputBundle = new Bundle();
        outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.NEW_MACHINE);
        switch (view.getId()) {
            case R.id.button_popup_main_new_finite_state_automatom:
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
                break;
            case R.id.button_popup_main_new_pushdown_automaton:
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.PUSHDOWN_AUTOMATON);
                break;
            case R.id.button_popup_main_new_linear_bounded_automaton:
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.LINEAR_BOUNDED_AUTOMATON);
                break;
            case R.id.button_popup_main_new_turing_machine:
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.TURING_MACHINE);
                break;
        }
        ((NewMachineDialogListener) getActivity()).newMachineDialogClick(outputBundle);
    }
}
