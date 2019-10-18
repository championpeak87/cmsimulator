package fiitstu.gulis.cmsimulator.elements;

import android.support.annotation.ColorInt;

/**
 * The color of a machine during simulation
 *
 * Created by Martin on 7. 3. 2017.
 */
public class MachineColor {

    private long id;
    @ColorInt
    private int value;

    public MachineColor(long id, @ColorInt int value) {
        this.id = id;
        this.value = value;
    }

    public MachineColor(@ColorInt int value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }

    @ColorInt
    public int getValue() {
        return value;
    }

    public void setValue(@ColorInt int value) {
        this.value = value;
    }
}
