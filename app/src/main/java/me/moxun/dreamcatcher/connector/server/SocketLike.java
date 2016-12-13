package me.moxun.dreamcatcher.connector.server;

import android.net.LocalSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility to allow reading buffered data from a socket and then "unreading" the data
 * and combining it with the original unbuffered stream.  This is useful when
 * handing off from one logical protocol layer to the next, such as when upgrading an HTTP
 * connection to the websocket protocol.
 */
public class SocketLike {
    private final LeakyBufferedInputStream mLeakyInput;
    private final OutputStream outputStream;

    public SocketLike(SocketLike socketLike, LeakyBufferedInputStream leakyInput) {
        this(socketLike.outputStream, leakyInput);
    }

    public SocketLike(OutputStream output, LeakyBufferedInputStream leakyIn) {
        outputStream = output;
        mLeakyInput = leakyIn;
    }

    public SocketLike(LocalSocket socket, LeakyBufferedInputStream leakyInput) {
        OutputStream temp = null;
        try {
            temp = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        outputStream = temp;
        mLeakyInput = leakyInput;
    }

    public InputStream getInput() throws IOException {
        return mLeakyInput.leakBufferAndStream();
    }

    public OutputStream getOutput() throws IOException {
        return outputStream;
    }
}
