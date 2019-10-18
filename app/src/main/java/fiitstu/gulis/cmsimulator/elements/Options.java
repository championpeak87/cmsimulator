package fiitstu.gulis.cmsimulator.elements;

import android.os.Build;

/**
 * A class that represents all options. Mostly a legacy now, but still
 * used for its static members and has a corresponding DB table
 *
 * Created by Martin on 23. 4. 2017.
 */
public class Options {

    public static final boolean MARK_NONDETERMINISM_DEFAULT = false;
    public static final String USER_NAME_DEFAULT = Build.PRODUCT;
    public static final byte REQUEST_ID_DEFAULT = 0;
    public static final int MAX_STEPS_DEFAULT = 100;

    private boolean markNondeterminism;
    private String userName;
    private byte requestId;
    private int maxSteps;

    public boolean getMarkNondeterminism() {
        return markNondeterminism;
    }

    public void setMarkNondeterminism(boolean markNondeterminism) {
        this.markNondeterminism = markNondeterminism;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public byte getRequestId() {
        return requestId;
    }

    public void setRequestId(byte requestId) {
        this.requestId = requestId;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }
}
