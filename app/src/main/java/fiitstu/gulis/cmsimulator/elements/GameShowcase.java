package fiitstu.gulis.cmsimulator.elements;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.ConfigurationActivity;
import fiitstu.gulis.cmsimulator.activities.MainActivity;
import fiitstu.gulis.cmsimulator.activities.TasksActivity;
import io.blushine.android.ui.showcase.MaterialShowcaseSequence;
import io.blushine.android.ui.showcase.MaterialShowcaseView;

public class GameShowcase {
    public static int stateCounter = 0;

    public void showTutorial(int gameNumber, Activity activity) {
        Bundle inputBundle = ConfigurationActivity.inputBundle;
        int taskConfiguration = inputBundle.getInt(ConfigurationActivity.TASK_CONFIGURATION);
        if (taskConfiguration == MainActivity.GAME_MACHINE) {
            switch (gameNumber) {
                case TasksActivity.GAME_EXAMPLE_PREVIEW:
                    MaterialShowcaseSequence mySequence = new MaterialShowcaseSequence(activity);
                    switch (stateCounter) {
                        case 0:

                            mySequence.addSequenceItem(new MaterialShowcaseView.Builder(activity)
                                    .renderOverNavigationBar()
                                    .setTitleText(R.string.welcome)
                                    .setContentText(R.string.game_welcome1)
                                    .setDelay(500)
                                    .setDismissBackgroundColor(activity.getColor(R.color.primary_color_dark))
                                    .setDismissText(R.string.gotit)
                                    .build());
                            mySequence.addSequenceItem(new MaterialShowcaseView.Builder(activity)
                                    .renderOverNavigationBar()
                                    .setTarget(activity.findViewById(R.id.imageButton_configuration_diagram_state))
                                    .setDelay(500)
                                    .setTitleText(R.string.game_new_state_title)
                                    .setContentText(R.string.game_create_state)
                                    .setDismissBackgroundColor(activity.getColor(R.color.primary_color_dark))
                                    .build());

                            mySequence.show();
                            break;

                        case 1:
                            mySequence.addSequenceItem(new MaterialShowcaseView.Builder(activity)
                                    .renderOverNavigationBar()
                                    .setTarget(activity.findViewById(R.id.imageButton_configuration_diagram_state))
                                    .setTitleText(R.string.game_new_state_title)
                                    .setContentText("Vytvorime si novy stav aby sme mohli vytvorit prechod")
                                    .setDelay(500)
                                    .build());

                            mySequence.show();
                            break;

                        case 2:
                            mySequence.addSequenceItem(new MaterialShowcaseView.Builder(activity)
                                    .renderOverNavigationBar()
                                    .setTarget(activity.findViewById(R.id.imageButton_configuration_diagram_transition))
                                    .setDelay(500)
                                    .setTitleText(R.string.game_new_state_title)
                                    .setContentText("Vytvorime prechod")
                                    .build());

                            mySequence.show();
                            break;

                        default:
                            stateCounter = 0;
                            Toast.makeText(activity, R.string.generic_error, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    if (++stateCounter > 2)
                        stateCounter = 0;

                    break;
            }
        }
    }

}
