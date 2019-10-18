package fiitstu.gulis.cmsimulator.util;

import android.os.AsyncTask;
import android.view.View;

/**
 * A class for handling potentially long-running jobs outside of GUI thread.
 * It runs an action in the background. If this action takes longer than a specified threshold,
 * it makes the specified view visible (this view can be e.g. a progress bar). After the action ends,
 * it makes the view "gone" and executes a post-action, if one was set.
 *
 * Created by Jakub Sedlář on 25.03.2018.
 */
public class ProgressWorker extends AsyncTask<Void, Void, Void> {

    //log tag
    private final String TAG = ProgressWorker.class.getName();

    //the view to be shown/hidden
    private final View view;

    //the main action
    private final Runnable action;
    //the action performed at the end
    private Runnable postAction;

    //the time after which the view is shown
    private final long delay;

    public ProgressWorker(long delay, View view, Runnable action) {
        this.delay = delay;
        this.view = view;
        this.action = action;
    }

    public void setPostAction(Runnable postAction) {
        this.postAction = postAction;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Thread shower = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setVisibility(View.VISIBLE);
                        }
                    });
                }
                catch (InterruptedException e) {
                    //do nothing
                }
            }
        });
        shower.start();

        action.run();
        shower.interrupt(); //if action finished running before the thread woke, never show the view
        try {
            shower.join();
        } catch (InterruptedException e) {
            //do nothing
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        view.post(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.GONE);
            }
        });

        if (postAction != null) {
            postAction.run();
        }
    }
}
