package fiitstu.gulis.cmsimulator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import fiitstu.gulis.cmsimulator.R;

public class ExitDialog extends DialogFragment {
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(android.R.string.cancel);
        alertDialog.setMessage(R.string.dialog_exit_message);
        alertDialog.setNeutralButton(android.R.string.cancel, null);
        alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ExitDialog.this.getActivity().finish();
            }
        });

        return alertDialog.create();
    }
}
