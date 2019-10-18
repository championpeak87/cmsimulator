package fiitstu.gulis.cmsimulator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.database.FileHandler;

public class SaveGrammarDialog extends DialogFragment implements View.OnClickListener{
    //log tag
    private static final String TAG = SaveGrammarDialog.class.getName();

    //bundle values
    public static final String FILENAME = "FILENAME";
    public static final String FORMAT = "FORMAT";
    public static final String EXIT = "EXIT";

    FileHandler.Format format;
    private EditText inputFileName;
    private boolean exit;
    private RadioButton cmsgRadio;
    private RadioButton jffRadio;

    public interface SaveDialogListener {
        void saveDialogClick(String filename, FileHandler.Format format, boolean exit);
    }

    public SaveGrammarDialog() {
        // Empty constructor required for DialogFragment
    }

    /**
     * Returns a new instance of the SaveGrammarDialog with given arguments set.
     * @param filename the default name of the file
     * @param format the format of the file.
     * @param exit check if the activity needs to be closed after save
     * @return a new instance of the SaveGrammarDialog with given arguments set
     */
    public static SaveGrammarDialog newInstance(String filename, FileHandler.Format format, boolean exit) {
        SaveGrammarDialog frag = new SaveGrammarDialog();
        Bundle args = new Bundle();
        args.putString(FILENAME, filename);
        args.putSerializable(FORMAT, format);
        args.putBoolean(EXIT, exit);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_save_grammar, null);

        inputFileName = view.findViewById(R.id.editText_filename);
        //get arguments from bundle
        String filename = getArguments().getString(FILENAME);
        if (filename != null) {
            inputFileName.setText(filename);
        }

        cmsgRadio = view.findViewById(R.id.radioButton_cmsg);
        jffRadio = view.findViewById(R.id.radioButton_jff);
        cmsgRadio.setOnClickListener(this);
        jffRadio.setOnClickListener(this);
        cmsgRadio.setChecked(true);

        Bundle args = getArguments();
        format = (FileHandler.Format) args.getSerializable(FORMAT);
        if (format != null) {
            jffRadio.setVisibility(View.GONE);
            cmsgRadio.setVisibility(View.GONE);
        }
        exit = args.getBoolean(EXIT);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getResources().getString(R.string.save_file));
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setInverseBackgroundForced(true);
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.save), null);
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.discard), null);

        return alertDialogBuilder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (format == null) {
                        format = cmsgRadio.isChecked() ? FileHandler.Format.CMSG : FileHandler.Format.JFF;
                    }
                    ((SaveGrammarDialog.SaveDialogListener) getActivity()).saveDialogClick(inputFileName.getText().toString(),
                            format, exit);
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.radioButton_cmsg:
                jffRadio.setChecked(false);
                break;
            case R.id.radioButton_jff:
                cmsgRadio.setChecked(false);
                break;
        }
    }

}
