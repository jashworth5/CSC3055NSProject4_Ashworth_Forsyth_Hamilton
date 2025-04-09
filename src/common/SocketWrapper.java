package src.common;

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

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    /**
     * Constructs a new SocketWrapper around the provided socket
     *
     * @param socket  underlying socket
     * @throws IOException if an I/O error occurs when creating the input/output streams
     */
    public SocketWrapper(Socket socket) throws IOException {
        
        this.socket = socket;
        // Create a reader to read lines from the socket's input stream
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Create a writer to send data to the socket's output stream
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }

    /**
     * Sends a JSON message over the socket
     *
     * @param serviceMessage JSONType message to send
     * @throws IOException if an I/O error occurs during sending
     */
    public void sendMessage(JSONSerializable message) throws IOException {

        // Serialize the message into JSON string
        String serialized = message.serialize();
        
        // Remove newlines and extra whitespace to send it as a single line
        serialized = serialized.replace("\n", "").replace("\r", "").trim();
        
        writer.println(serialized);
        writer.flush();
    }
    
    

    /**
     * Receives a JSON message from the socket.
     *
     * @return the received JSONType message
     * @throws IOException if an I/O error occurs during receiving or if the connection is closed
     */
    public JSONObject receiveMessage() throws IOException {
        
        // Read a line from the socket's input stream.
        String jsonString = reader.readLine();
        if (jsonString == null) {
            throw new IOException("Connection closed by remote host");
        }

        // Parse the JSON string into a JSONType object.
        // once encryption/decryption are added, can decrypt this before parsing
        JSONObject message = JsonUtils.parse(jsonString);
        return message;
    }


    /**
     * Closes the socket and its associated streams.
     */
    public void close() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Optionally log the exception.
            e.printStackTrace();
        }
    }

    /*
     * GETTERS
     */
    public PrintWriter getWriter() {
        return writer;
    }

    public BufferedReader getReader() {
        return reader;
    }
}