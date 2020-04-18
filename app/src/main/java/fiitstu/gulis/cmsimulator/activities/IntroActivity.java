package fiitstu.gulis.cmsimulator.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.hololo.tutorial.library.PermissionStep;
import com.hololo.tutorial.library.Step;
import com.hololo.tutorial.library.TutorialActivity;
import fiitstu.gulis.cmsimulator.BuildConfig;
import fiitstu.gulis.cmsimulator.R;

public class IntroActivity extends TutorialActivity {

    @Override
    public void currentFragmentPosition(int position) {

    }

    private boolean isFirstTimeLaunch() {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        int defaultValue = getResources().getInteger(R.integer.first_time_launch);
        int currentValue = sharedPreferences.getInt("FIRST_TIME_LAUNCH", defaultValue);

        if (currentValue == 0) {
            // SAVE FIRST TIME LAUNCH
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("FIRST_TIME_LAUNCH", 1);
            editor.putString("LAST_KNOWN_VERSION", BuildConfig.VERSION_NAME);
            editor.apply();
        }

        return currentValue == 0;
    }

    private boolean isNewVersion() {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        String lastKnownVersion = sharedPreferences.getString("LAST_KNOWN_VERSION", "");
        String currentVersion = BuildConfig.VERSION_NAME;

        if (lastKnownVersion.equals("") || !lastKnownVersion.equals(currentVersion)) {
            // SAVE LAST KNOWN VERSION
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("LAST_KNOWN_VERSION", BuildConfig.VERSION_NAME);
            editor.apply();
        }

        return !currentVersion.equals(lastKnownVersion);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean firstTimeLaunch = isFirstTimeLaunch(),
                newVersionLaunch = isNewVersion();

        setPrevText(getString(R.string.back));
        setNextText(getString(R.string.next));
        setCancelText(getString(R.string.skip));
        setGivePermissionText(getString(R.string.give));
        setFinishText(getString(R.string.finish));
        if (firstTimeLaunch)
        {

            addFragment(new Step.Builder().setTitle("")
                    .setBackgroundColor(getColor(R.color.primary_color)) // int background color
                    .setView(R.layout.intro_screen_welcome)
                    .build());

            // Permission Step
            addFragment(new PermissionStep.Builder().setTitle("")
                    .setBackgroundColor(getColor(R.color.primary_color_dark))
                    .setView(R.layout.intro_screen_permissions)
                    .setPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
                    .build());

            addFragment(new Step.Builder().setTitle("")
                    .setBackgroundColor(getColor(R.color.primary_color))
                    .setView(R.layout.intro_screen_finish)
                    .build());
        }
        else if (newVersionLaunch)
        {
            addFragment(new Step.Builder().setTitle("")
                    .setBackgroundColor(getColor(R.color.primary_color_dark))
                    .setView(R.layout.intro_screen_whats_new)
                    .build());
        }
        else
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void finishTutorial() {
        super.finishTutorial();

        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);
    }
}
