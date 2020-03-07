package fiitstu.gulis.cmsimulator.exceptions;

import android.content.Context;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;

public class NotImplementedException extends Exception {
    private Context mContext;

    public NotImplementedException(Context mContext) {
        super(mContext.getString(R.string.not_implemented));
        this.mContext = mContext;
        Toast.makeText(this.mContext, mContext.getText(R.string.not_implemented), Toast.LENGTH_LONG).show();
    }
}