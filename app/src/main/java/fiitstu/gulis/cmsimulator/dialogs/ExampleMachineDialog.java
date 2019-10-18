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
 * The dialog for selecting an example machine
 *
 * Created by Martin on 16. 4. 2017.
 */
public class ExampleMachineDialog extends DialogFragment implements View.OnClickListener {

    //log tag
    private static final String TAG = ExampleMachineDialog.class.getName();

    public interface ExampleMachineDialogListener {
        void exampleMachineDialogClick(Bundle outputBundle);
    }

    public ExampleMachineDialog() {
        // Empty constructor required for DialogFragment
    }

    public static ExampleMachineDialog newInstance() {
        return new ExampleMachineDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.dialog_main_example, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //finite state automaton
        Button finiteStateAutomatonB1 = view.findViewById(R.id.button_popup_main_example1_finite_state_automatom);
        finiteStateAutomatonB1.setOnClickListener(this);
        Button finiteStateAutomatonB2 = view.findViewById(R.id.button_popup_main_example2_finite_state_automatom);
        finiteStateAutomatonB2.setOnClickListener(this);
        Button finiteStateAutomatonB3 = view.findViewById(R.id.button_popup_main_example3_finite_state_automatom);
        finiteStateAutomatonB3.setOnClickListener(this);

        //pushdown automaton
        Button pushdownAutomatonB1 = view.findViewById(R.id.button_popup_main_example1_pushdown_automaton);
        pushdownAutomatonB1.setOnClickListener(this);
        Button pushdownAutomatonB2 = view.findViewById(R.id.button_popup_main_example2_pushdown_automaton);
        pushdownAutomatonB2.setOnClickListener(this);
        Button pushdownAutomatonB3 = view.findViewById(R.id.button_popup_main_example3_pushdown_automaton);
        pushdownAutomatonB3.setOnClickListener(this);

        //linear bounded automaton
        Button linearBoundedAutomatonB1 = view.findViewById(R.id.button_popup_main_example1_linear_bounded_automaton);
        linearBoundedAutomatonB1.setOnClickListener(this);
        Button linearBoundedAutomatonB2 = view.findViewById(R.id.button_popup_main_example2_linear_bounded_automaton);
        linearBoundedAutomatonB2.setOnClickListener(this);

        //turing machine
        Button turingMachineB1 = view.findViewById(R.id.button_popup_main_example1_turing_machine);
        turingMachineB1.setOnClickListener(this);
        Button turingMachineB2 = view.findViewById(R.id.button_popup_main_example2_turing_machine);
        turingMachineB2.setOnClickListener(this);
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
        switch (view.getId()) {
            case R.id.button_popup_main_example1_finite_state_automatom:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE1);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
                break;
            case R.id.button_popup_main_example2_finite_state_automatom:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE2);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
                break;
            case R.id.button_popup_main_example3_finite_state_automatom:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE3);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
                break;
            case R.id.button_popup_main_example1_pushdown_automaton:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE1);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.PUSHDOWN_AUTOMATON);
                break;
            case R.id.button_popup_main_example2_pushdown_automaton:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE2);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.PUSHDOWN_AUTOMATON);
                break;
            case R.id.button_popup_main_example3_pushdown_automaton:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE3);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.PUSHDOWN_AUTOMATON);
                break;
            case R.id.button_popup_main_example1_linear_bounded_automaton:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE1);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.LINEAR_BOUNDED_AUTOMATON);
                break;
            case R.id.button_popup_main_example2_linear_bounded_automaton:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE2);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.LINEAR_BOUNDED_AUTOMATON);
                break;
            case R.id.button_popup_main_example1_turing_machine:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE1);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.TURING_MACHINE);
                break;
            case R.id.button_popup_main_example2_turing_machine:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE2);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.TURING_MACHINE);
                break;
        }
        ((ExampleMachineDialogListener) getActivity()).exampleMachineDialogClick(outputBundle);
    }
}