package fiitstu.gulis.cmsimulator.dialogs;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.MainActivity;

public class ExampleGrammarDialog extends DialogFragment implements View.OnClickListener {

    //log tag
    private static final String TAG = ExampleGrammarDialog.class.getName();

    public interface ExampleGrammarDialogListener {
        void exampleGrammarDialogClick(Bundle outputBundle);
    }

    public ExampleGrammarDialog() {
        // Empty constructor required for DialogFragment
    }

    public static ExampleGrammarDialog newInstance() {
        return new ExampleGrammarDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.dialog_main_example_grammar, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button exampleGrammar1 = view.findViewById(R.id.button_popup_main_example_grammar1);
        exampleGrammar1.setOnClickListener(this);

        Button exampleGrammar2 = view.findViewById(R.id.button_popup_main_example_grammar2);
        exampleGrammar2.setOnClickListener(this);

        Button exampleGrammar3 = view.findViewById(R.id.button_popup_main_example_grammar3);
        exampleGrammar3.setOnClickListener(this);

        Button exampleGrammar4 = view.findViewById(R.id.button_popup_main_example_grammar4);
        exampleGrammar4.setOnClickListener(this);

        Button exampleGrammar5 = view.findViewById(R.id.button_popup_main_example_grammar5);
        exampleGrammar5.setOnClickListener(this);

        Button exampleGrammar6 = view.findViewById(R.id.button_popup_main_example_grammar6);
        exampleGrammar6.setOnClickListener(this);

        Button exampleGrammar7 = view.findViewById(R.id.button_popup_main_example_grammar7);
        exampleGrammar7.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.8),
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.7));
        }
    }

    @Override
    public void onClick(View view) {
        Bundle outputBundle = new Bundle();
        switch (view.getId()) {
            case R.id.button_popup_main_example_grammar1:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_GRAMMAR1);
                break;
            case R.id.button_popup_main_example_grammar2:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_GRAMMAR2);
                break;
            case R.id.button_popup_main_example_grammar3:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_GRAMMAR3);
                break;
            case R.id.button_popup_main_example_grammar4:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_GRAMMAR4);
                break;
            case R.id.button_popup_main_example_grammar5:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_GRAMMAR5);
                break;
            case R.id.button_popup_main_example_grammar6:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_GRAMMAR6);
                break;
            case R.id.button_popup_main_example_grammar7:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_GRAMMAR7);
                break;
        }
        ((ExampleGrammarDialogListener) getActivity()).exampleGrammarDialogClick(outputBundle);
    }
}
