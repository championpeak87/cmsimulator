package fiitstu.gulis.cmsimulator.network;


import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.app.CMSimulator;

import java.io.IOException;
import java.net.*;

/**
 * A factory for creating sockets. This class is used instead of just creating the sockets
 * directly because it is much easier to mock socket communication for unit testing this way
 *
 * Created by Jakub Sedlář on 19.03.2018.
 */
public class TaskSocketFactory {

    public static final int PORT = CMSimulator.getContext().getResources().getInteger(R.integer.port);

    /**
     * Returns a new DatagramSocket bound to any available port on local machine
     * @return a new datagram socket
     * @throws SocketException if the socket could not be opened.
     */
    public DatagramSocket createDatagramSocket() throws SocketException {
        return new DatagramSocket();
    }

    /**
     * Returns a new DatagramSocket bound to to the application's standard
     * @return a new datagram socket
     * @throws SocketException if the socket could not be opened, or the socket could not bind to the specified local port.
     */
    public DatagramSocket createBoundDatagramSocket() throws SocketException {
        return new DatagramSocket(PORT);
    }

    /**
     * Returns a new ServerSocket bound to the application's standard port
     * @return a new ServerSocket bound to the application's standard port
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    public ServerSocket createServerSocket() throws IOException {
        return new ServerSocket(PORT);
    }

    /**
     * Creates a new stream socket that connects to the application's standard port at the specified IP address
     * @param address the IP address
     * @return a new stream socket that connects to the application's standard port at the specified IP address
     * @throws IOException if an I/O error occurs when creating the socket.
     */
    public Socket createSocket(InetAddress address) throws IOException {
        return new Socket(address, PORT);
    }

    /**
     * Creates a new stream socket that connects to the application's standard port at the specified IP address
     * @param address the IP address
     * @return a new stream socket that connects to the application's standard port at the specified IP address
     * @throws IOException if an I/O error occurs when creating the socket.
     */
    public Socket createSocket(String address) throws IOException {
        return new Socket(address, PORT);
    }
}
