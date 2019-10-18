package fiitstu.gulis.cmsimulator.network;

import android.os.AsyncTask;
import android.util.Log;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.elements.TaskResult;
import fiitstu.gulis.cmsimulator.elements.TestScenario;
import fiitstu.gulis.cmsimulator.machines.MachineStep;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * A class for evaluating the results of a task and sending them back to the assigner.
 *
 * After being started, it runs tests in background and tries to send them. At the end,
 * onFinish action is called, followed either by onSuccess (if no exceptions occurred) or onFailure
 * (if the results weren't sent successfully for any reason).
 *
 * Created by Jakub Sedlář on 20.01.2018.
 */
public class TaskResultSender extends AsyncTask<Void, Void, TaskResult> {

    public interface OnSuccessAction {
        void run(TaskResult result);
    }

    //log tag
    private final String TAG = TaskResultSender.class.getName();

    private final Task task;
    private final int machineType;

    private final TaskSocketFactory socketFactory;

    private Runnable onFinish;
    private Runnable onFailure;
    private OnSuccessAction onSuccess;

    public TaskResultSender(Task task, int machineType) {
        this(task, machineType, new TaskSocketFactory());
    }

    public TaskResultSender(Task task, int machineType, TaskSocketFactory socketFactory) {
        this.task = task;
        this.machineType = machineType;
        this.socketFactory = socketFactory;
    }

    /**
     * Sets the action to be always run after the task is finished
     * @param onFinish the action to be always run after the task is finished
     */
    public void setOnFinish(Runnable onFinish) {
        this.onFinish = onFinish;
    }

    /**
     * Sets the action to be ran after the onFinish action if the
     * task ended in failure
     * @param onFailure the action to be ran after the onFinish action if the
     * task ended in failure
     */
    public void setOnFailure(Runnable onFailure) {
        this.onFailure = onFailure;
    }

    /**
     * Sets the action to be ran after the onFinish action if the
     * task ended in success
     * @param onSuccess the action to be ran after the onFinish action if the
     * task ended in success
     */
    public void setOnSuccess(OnSuccessAction onSuccess) {
        this.onSuccess = onSuccess;
    }

    @Override
    protected TaskResult doInBackground(Void... voids) {
        DataSource dataSource = DataSource.getInstance();
        int oldMaxSteps = dataSource.getMaxSteps();
        dataSource.updateMaxSteps(task.getMaxSteps());

        List<Symbol> alphabet = dataSource.getInputAlphabetFullExtract();

        int positiveSuccesses = 0;
        List<TestScenario> testScenarios = dataSource.getTestFullExtract(false, alphabet);
        for (TestScenario test: testScenarios) {
            MachineStep machineStep = test.prepareMachine(machineType, dataSource);
            machineStep.simulateFull();
            if (machineStep.getNondeterministicMachineStatus() == MachineStep.DONE) {
                positiveSuccesses++;
            }
        }

        int negativeSuccesses = 0;
        List<TestScenario> negativeScenarios = dataSource.getTestFullExtract(true, alphabet);
        for (TestScenario test: negativeScenarios) {
            MachineStep machineStep = test.prepareMachine(machineType, dataSource);
            machineStep.simulateFull();
            if (machineStep.getNondeterministicMachineStatus() != MachineStep.DONE) {
                negativeSuccesses++;
            }
        }

        dataSource.updateMaxSteps(oldMaxSteps);

        String result;
        TaskResult taskResult;
        try {
            taskResult = new TaskResult(dataSource.getUserName(),
                    positiveSuccesses, testScenarios.size(),
                    negativeSuccesses, negativeScenarios.size());
            taskResult.setVersion(Math.min(TaskResult.CURRENT_VERSION, task.getResultVersion()));
            result = taskResult.toXML();
        } catch (IOException e) {
            Log.e(TAG, "Error serializing task result", e);
            return null;
        }

        try {
            Socket socket = socketFactory.createSocket(task.getAssigner());
            DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
            stream.writeUTF(result);
        } catch (IOException e) {
            Log.e(TAG, "Error sending result data", e);
            return null;
        }

        return taskResult;
    }

    @Override
    protected void onPostExecute(TaskResult result) {
        if (onFinish != null) {
            onFinish.run();
        }

        if (result != null && onSuccess != null) {
            onSuccess.run(result);
        }
        else if (result == null && onFailure != null) {
            onFailure.run();
        }
    }
}
