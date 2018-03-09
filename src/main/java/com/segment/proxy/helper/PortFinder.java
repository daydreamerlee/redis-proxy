package com.segment.proxy.helper;


import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

/**
 * Helps to find random port on the host. Used for Testing.
 */
public class PortFinder {

    public static synchronized  Integer findRandomOpenPort() throws IOException {
        try (
                ServerSocket socket = new ServerSocket(0);
        ) {
            return socket.getLocalPort();

        }
    }
}