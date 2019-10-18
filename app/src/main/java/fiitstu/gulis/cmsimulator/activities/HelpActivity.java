package fiitstu.gulis.cmsimulator.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import fiitstu.gulis.cmsimulator.R;

/**
 * Activity dedicated entirely to showing help.
 *
 * Created by Martin on 13. 4. 2017.
 */
public class HelpActivity extends FragmentActivity implements View.OnClickListener {

    //log tag
    private static final String TAG = HelpActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Log.v(TAG, "onCreate initialization started");

        ////buttons initialization
        //back
        ImageButton backB = findViewById(R.id.imageButton_help_back);
        backB.setOnClickListener(this);

        Log.i(TAG, "onCreate initialized");
    }

    @Override
    public void onBackPressed() {
        Log.v(TAG, "onBackPressed method started");
        finish();
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.imageButton_help_back) {
            onBackPressed();
        }
    }
}
