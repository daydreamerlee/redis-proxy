package com.segment.proxy.helper;


import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

/**
 * From : http://fahdshariff.blogspot.com/2012/10/java-find-available-port-number.html
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