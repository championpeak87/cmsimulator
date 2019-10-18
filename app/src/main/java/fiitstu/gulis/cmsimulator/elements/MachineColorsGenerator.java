package fiitstu.gulis.cmsimulator.elements;

import android.content.Context;
import android.util.Log;

import java.util.List;

import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.R;

/**
 * A class for creating nad manipulating machine colors
 *
 * Created by Martin on 28. 2. 2017.
 */
public class MachineColorsGenerator {

    //log tag
    private static final String TAG = MachineColorsGenerator.class.getName();

    private List<MachineColor> machineColorsRawList;
    private MachineColor initialColor;
    private int nextUseColorIndex;

    public MachineColorsGenerator(Context context, DataSource dataSource) {
        loadColors(context, dataSource);
    }

    public void loadColors(Context context, DataSource dataSource) {
        machineColorsRawList = dataSource.getColors();
        if (machineColorsRawList.isEmpty()) {
            loadFromXML(context, dataSource);
        } else {
            initialColor = machineColorsRawList.get(0);
        }
        this.nextUseColorIndex = machineColorsRawList.size() - 1;
    }

    public void loadFromXML(Context context, DataSource dataSource) {
        int[] colorsRaw = context.getResources().getIntArray(R.array.machineColors);
        machineColorsRawList = dataSource.addColorList(colorsRaw);
        initialColor = machineColorsRawList.get(0);
    }

    public MachineColor getNextColor() {
        Log.v(TAG, "getNextColor assigned");
        nextUseColorIndex = (nextUseColorIndex + 1) % machineColorsRawList.size();
        return new MachineColor(machineColorsRawList.get(nextUseColorIndex).getValue());
    }

    public void resetCounter() {
        this.nextUseColorIndex = machineColorsRawList.size() - 1;
    }

    public List<MachineColor> getMachineColorsRawList() {
        return machineColorsRawList;
    }

    public MachineColor getInitialColor() {
        return initialColor;
    }
}
