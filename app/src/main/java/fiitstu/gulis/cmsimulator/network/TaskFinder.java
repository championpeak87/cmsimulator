package fiitstu.gulis.cmsimulator.network;

import android.os.SystemClock;
import android.util.Log;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.app.CMSimulator;
import fiitstu.gulis.cmsimulator.database.DataSource;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * A class for finding tasks that are being offered by other devices in the network.
 *
 * Created by Jakub Sedlář on 05.01.2018.
 */
public class TaskFinder {

    public interface TaskConsumer {
        /**
         * Receive a task
         * @param task a received String (normally an XLM-serialized automaton + task)
         * @param assigner the address the task was received from
         */
        void receive(String task, InetAddress assigner);

        /**
         * Register a keep-alive message
         * @param assigner the address the keep-alive message was received from
         */
        void refresh(InetAddress assigner);
    }

    //log tag
    private static final String TAG = TaskFinder.class.getName();

    private static final int REQUEST_MAGIC = CMSimulator.getContext().getResources().getInteger(R.integer.request_magic_number);
    private static final String ENCODING = CMSimulator.getContext().getResources().getString(R.string.network_encoding);
    //the time period between sending broadcasts (milliseconds)
    private static final int REFRESH_PERIOD = 500;

    //used by the server to identify whether or not it needs to resend its data
    private volatile byte requestId;

    private final TaskSocketFactory socketFactory;

    //the socket used for broadcasting requests
    private final DatagramSocket requestSocket;

    private final TaskConsumer taskConsumer;

    //the thread where the requests are sent
    private Thread requestThread;
    private volatile boolean running;

    public TaskFinder(TaskConsumer taskConsumer) throws SocketException {
        this(taskConsumer, new TaskSocketFactory());
    }

    public TaskFinder(TaskConsumer taskConsumer, TaskSocketFactory socketFactory) throws SocketException {
        this.taskConsumer = taskConsumer;
        this.socketFactory = socketFactory;
        requestSocket = socketFactory.createDatagramSocket();
        requestId = DataSource.getInstance().getNextRequestId();
        Log.v(TAG, "new TaskFinder created with ID: " + requestId);
    }

    /**
     * Start finding tasks. Can only be called once on each instance
     * (the TaskFinder cannot be re-started after {@link #close()} is called)
     */
    public void start() {
        requestThread = new Thread() {
            @Override
            public void run() {
                findTasks();
            }
        };
        running = true;
        requestThread.start();
    }

    public void changeID() {
        requestId = DataSource.getInstance().getNextRequestId();
    }

    public void close() {
        running = false;
        requestSocket.close();

        try {
            requestThread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for thread", e);
        }
    }

    /**
     * Periodically broadcasts requests and listens for answers. Loops while {@link #running} is true.
     */
    private void findTasks() {
        DatagramPacket request;
        try {
            request = new DatagramPacket(new byte[4], 4, InetAddress.getByName("255.255.255.255"), TaskSocketFactory.PORT);
        } catch (UnknownHostException e) {
            Log.e(TAG, "The system did not recognize the broadcast address", e);
            return;
        }

        ServerSocket serverSocket;
        try {
            serverSocket = socketFactory.createServerSocket();
        } catch (IOException e) {
            Log.e(TAG, "Could not create socket", e);
            return;
        }

        while (running) {
            try {
                //"& 0xFF" needed because integer promotion of a negative byte would otherwise ruin everything
                ByteBuffer.wrap(request.getData()).putInt(REQUEST_MAGIC | (requestId & 0xFF));
                requestSocket.send(request);

                long start = SystemClock.elapsedRealtime();

                boolean timeout = false;
                while (!timeout) {
                    long end = SystemClock.elapsedRealtime();
                    try {
                        int time = (int)(end - start);
                        if (REFRESH_PERIOD - time <= 0) {
                            //a bit on the ugly side, but it works... calling setSoTimeout with a negative value
                            //would throw an IllegalArgumentException, with zero it would would DISABLE timeout
                            throw new SocketTimeoutException();
                        }
                        serverSocket.setSoTimeout(REFRESH_PERIOD - time);
                        Socket socket = serverSocket.accept();

                        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                        int taskSize = inputStream.readInt();

                        if (taskSize == 0) {
                            //we already have this task (or at least the sender thinks so)
                            taskConsumer.refresh(socket.getInetAddress());
                        } else {
                            //receive the task
                            byte[] taskData = new byte[taskSize];
                            inputStream.readFully(taskData);
                            taskConsumer.receive(new String(taskData, ENCODING), socket.getInetAddress());
                        }
                        socket.close();
                    } catch (SocketTimeoutException e) {
                        timeout = true;
                    }
                }
            } catch (IOException e) {
                Log.i(TAG, "Exception occurred while handling a request", e);
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close socket", e);
        }
    }
}
