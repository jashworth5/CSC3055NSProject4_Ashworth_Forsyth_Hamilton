package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;


/**
 * SocketWrapper is a utility class that wraps a Socket object to provide
 * convenient methods for sending and receiving JSON messages.
 * 
 * Add encryption on top afterwards
 */
public class SocketWrapper {

    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    /**
     * Wrap a connected Socket or SSLSocket.
     */
    public SocketWrapper(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }

    /**
     * Sends a JSONSerializable object as a one-line JSON string.
     */
    public void sendMessage(JSONSerializable message) {
        String serialized = message.serialize().replace("\n", "").replace("\r", "").trim();
        writer.println(serialized);
        writer.flush();
    }

    /**
     * Receives a JSON object from the socket stream.
     */
    public JSONObject receiveMessage() throws IOException {
        String jsonString = reader.readLine();
        if (jsonString == null) {
            throw new IOException("Connection closed by remote host");
        }
        return JsonIO.readObject(jsonString);
    }

    /**
     * Close all resources.
     */
    public void close() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Optional accessors
    public PrintWriter getWriter() {
        return writer;
    }

    public BufferedReader getReader() {
        return reader;
    }
}