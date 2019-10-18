package fiitstu.gulis.cmsimulator.app;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.v4.os.ConfigurationCompat;
import android.util.TypedValue;

import java.util.Locale;

/**
 * The application class, provides convenient access to the application's global context
 * and some utility methods.
 *
 * Created by Jakub Sedlář on 01.01.2018.
 */
public class CMSimulator extends Application {

    private static CMSimulator instance;

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    /**
     * Obtains the text size, in pixels, associated with the given attribute
     * @param context the context whose theme is to be used
     * @param attribute the ID of the attribute
     * @return the text size, in pixels, associated with the given attribute
     */
    public static int getTextSize(Context context, @AttrRes int attribute) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attribute, typedValue, true);
        int[] textSizeAttr = new int[] { android.R.attr.textSize };
        int indexOfAttrTextSize = 0;
        TypedArray a = context.obtainStyledAttributes(typedValue.data, textSizeAttr);
        int size = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();

        return size;
    }
}
