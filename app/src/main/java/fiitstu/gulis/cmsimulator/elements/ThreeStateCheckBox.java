package fiitstu.gulis.cmsimulator.elements;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import fiitstu.gulis.cmsimulator.R;

public class ThreeStateCheckBox extends CheckBox {

    static private final int UNKNOW = -1;

    static private final int UNCHECKED = 0;

    static private final int CHECKED = 1;

    private int state = UNCHECKED;

    private final OnCheckedChangeListener privateListener = new CompoundButton.OnCheckedChangeListener() {

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (state) {
                case UNKNOW:
                    setState(UNCHECKED);
                    break;
                case UNCHECKED:
                    setState(CHECKED);
                    break;
                case CHECKED:
                    setState(UNKNOW);
                    break;
            }
        }
    };

    private OnCheckedChangeListener clientListener;
    private boolean restoring;

    public ThreeStateCheckBox(Context context) {
        super(context);
        init();
    }

    public ThreeStateCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThreeStateCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        if(!this.restoring && this.state != state) {
            this.state = state;

            if(this.clientListener != null) {
                this.clientListener.onCheckedChanged(this, this.isChecked());
            }

            updateBtn();
        }
    }

    @Override
    public void setOnCheckedChangeListener(@Nullable OnCheckedChangeListener listener) {
        if(this.privateListener != listener) {
            this.clientListener = listener;
        }
        super.setOnCheckedChangeListener(privateListener);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);

        ss.state = state;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        this.restoring = true; // indicates that the ui is restoring its state
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setState(ss.state);
        requestLayout();
        this.restoring = false;
    }

    private void init() {
        updateBtn();
        setOnCheckedChangeListener(this.privateListener);
    }

    private void updateBtn() {
        int btnDrawable = R.drawable.ic_checkbox_unchecked_black;
        switch (state) {
            case UNKNOW:
                btnDrawable = R.drawable.ic_checkbox_indeterminate_black;
                break;
            case UNCHECKED:
                btnDrawable = R.drawable.ic_checkbox_unchecked_black;
                break;
            case CHECKED:
                btnDrawable = R.drawable.ic_checkbox_checked_black;
                break;
        }
        setButtonDrawable(btnDrawable);
    }

    static class SavedState extends BaseSavedState {
        int state;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            state = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(state);
        }

        @Override
        public String toString() {
            return "CheckboxTriState.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " state=" + state + "}";
        }

        @SuppressWarnings("hiding")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}