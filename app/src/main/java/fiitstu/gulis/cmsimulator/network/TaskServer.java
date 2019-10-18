package fiitstu.gulis.cmsimulator.network;

import android.support.annotation.VisibleForTesting;
import android.util.Log;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.app.CMSimulator;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * A class for sending a task to all who ask for it and listening for results.
 *
 * Created by Jakub Sedlář on 05.01.2018.
 */
public class TaskServer {

    public interface ResultConsumer {
        /**
         * Receive a result
         * @param result a received String (normally an XML-serialized {@link fiitstu.gulis.cmsimulator.elements.TaskResult})
         */
        void receive(String result);
    }

    //log tag
    private static final String TAG = TaskServer.class.getName();

    //a magic number to identify data sent by our application
    private static final int REQUEST_MAGIC = CMSimulator.getContext().getResources().getInteger(R.integer.request_magic_number);
    private static final String ENCODING = CMSimulator.getContext().getResources().getString(R.string.network_encoding);
    //maximum number of milliseconds a client will be remembered without having heard from them.
    private static final int MAX_LIFETIME = 10000;

    //the data that is sent to all who request it
    private final byte[] taskData;
    //maps clients (their addressed) to the ID of their most recent request and their remaining lifetimes;
    private final Map<InetAddress, ClientInfo> clients = new HashMap<>();
    //the socket that listens for requests
    private final DatagramSocket requestSocket;
    //the socket that listens for results being sent back
    private final ServerSocket resultsSocket;

    //the consumer that will be notified when result is received
    private final ResultConsumer resultConsumer;

    private final TaskSocketFactory socketFactory;

    private Timer reaper;

    //the thread where requests are handled
    private Thread requestThread;
    //the thread where answers are received
    private Thread answerThread;
    private volatile boolean running;

    public TaskServer(String task, ResultConsumer resultConsumer) throws IOException {
        this(task, resultConsumer, new TaskSocketFactory());
    }

    public TaskServer(String task, ResultConsumer resultConsumer, TaskSocketFactory socketFactory) throws IOException {
        this.taskData = task.getBytes(ENCODING);
        this.resultConsumer = resultConsumer;
        this.socketFactory = socketFactory;
        requestSocket = socketFactory.createBoundDatagramSocket();
        resultsSocket = socketFactory.createServerSocket();
    }

    public void start() {
        requestThread = new Thread() {
            @Override
            public void run() {
                sendTasks();
            }
        };
        answerThread = new Thread() {
            @Override
            public void run() {
                receiveResults();
            }
        };
        running = true;
        requestThread.start();
        answerThread.start();

        reaper = new Timer();
        reaper.schedule(new TimerTask() {
            @Override
            public void run() {
                List<InetAddress> removedClients = new ArrayList<>();
                for (Map.Entry<InetAddress, ClientInfo> client : clients.entrySet()) {
                    client.getValue().setLifetime(client.getValue().getLifetime() - 1000);
                    if (client.getValue().getLifetime() <= 0) {
                        removedClients.add(client.getKey());
                    }
                }

                synchronized (clients) {
                    for (InetAddress client : removedClients) {
                        clients.remove(client);
                    }
                }
            }
        }, 1000, 1000);
    }

    public void close() {
        running = false;
        reaper.cancel();
        requestSocket.close();
        try {
            resultsSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred while closing socket", e);
        }

        try {
            requestThread.join();
            answerThread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for threads", e);
        }
    }

    /**
     * Listen for request and send back the task. Loops while {@link #running} is true.
     */
    private void sendTasks() {
        DatagramPacket request = new DatagramPacket(new byte[4], 4);
        while (running) {
            try {
                requestSocket.receive(request);

                //check if the received data came from our application
                int requestContent = ByteBuffer.wrap(request.getData()).getInt();
                if ((requestContent & 0xFFFFFF00) == REQUEST_MAGIC) {
                    Socket answerSocket = socketFactory.createSocket(request.getAddress());
                    if (!running) { //if the socket was created after the null-check in close
                        answerSocket.close();
                        break;
                    }
                    DataOutputStream stream = new DataOutputStream(answerSocket.getOutputStream());

                    ClientInfo clientInfo;
                    synchronized (clients) {
                        clientInfo = clients.get(request.getAddress());
                    }

                    if (clientInfo != null && clientInfo.getRequestID() == (byte)(requestContent & 0xFF)) {
                        //an old client with old request ID, no need to resend the data, just send a small keep-alive
                        clientInfo.setLifetime(MAX_LIFETIME);
                        stream.writeInt(0);
                        stream.flush();
                    }
                    else {
                        //send the data size, then the data itself
                        stream.writeInt(taskData.length);
                        stream.flush();

                        stream.write(taskData);
                        stream.flush();

                        answerSocket.close();

                        InetAddress address = request.getAddress();
                        synchronized (clients) {
                            clients.put(address, new ClientInfo((byte) (requestContent & 0xFF)));
                        }
                    }
                }
            } catch (IOException e) {
                Log.i(TAG, "Exception occurred while handling a request", e);
            }
        }
    }

    /**
     * Listen for results coming back. Loops while {@link #running} is true.
     */
    private void receiveResults() {
        while (running) {
            try {
                Socket socket = resultsSocket.accept();
                DataInputStream stream = new DataInputStream(socket.getInputStream());
                String result = stream.readUTF();
                socket.close();

                resultConsumer.receive(result);
            } catch (IOException e) {
                Log.i(TAG, "Exception occurred while handling a request", e);
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public static class ClientInfo {
        private byte requestID;
        private int lifetime;

        public ClientInfo(byte requestID) {
            this.requestID = requestID;
            this.lifetime = MAX_LIFETIME;
        }

        public byte getRequestID() {
            return requestID;
        }

        public void setRequestID(byte requestID) {
            this.requestID = requestID;
        }

        public int getLifetime() {
            return lifetime;
        }

        public void setLifetime(int lifetime) {
            this.lifetime = lifetime;
        }
    }
}
