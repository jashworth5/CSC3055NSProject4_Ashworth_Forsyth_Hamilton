package src.server;

import src.util.SocketWrapper;
import src.server.UserDatabase;
import src.server.BulletinBoard;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import src.util.SocketWrapper;

public class BulletinBoardService {

    public static void main(String[] args) {

        // check if config path is provided
        if (args.length < 2 || !args[0].equals("--config")) {
            System.out.println("usage: java BulletinBoardService --config <configfile>");
            return;
        }

        try {

            // read config file
            String configPath = args[1];
            JSONObject config = JsonIO.readObject(new File(configPath));

            // extract required values from config
            int port = ((Double) config.get("port")).intValue();
            String keystoreFile = (String) config.get("keystore-file");
            String keystorePass = (String) config.get("keystore-pass");
            String usersFile = (String) config.get("users-file");
            String boardFile = (String) config.get("board-file");

            // configure TLS system properties
            System.setProperty("javax.net.ssl.keyStore", keystoreFile);
            System.setProperty("javax.net.ssl.keyStorePassword", keystorePass);

            // create server socket
            SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(port);

            // initialize databases
            UserDatabase userDb = new UserDatabase(usersFile);
            BulletinBoard board = new BulletinBoard(boardFile);

            System.out.println("server started on port " + port);

        
            // server loop to accept clients
            while (true) {
                SSLSocket client = (SSLSocket) serverSocket.accept();
                SocketWrapper socket = new SocketWrapper(client);
                System.out.println("client connected");

                try {

                    // idea: receive the message as raw json
                    JSONObject message = socket.receiveMessage();
                    String type = (String) message.get("type");

                    if (type == null) {
                        System.out.println("missing type field in client message");
                        return;
                    }
                    
                    switch (type) {
                        case "Create":
                            try {

                                // deserialize into CreateMessage
                                CreateMessage createMsg = new CreateMessage();
                                createMsg.deserialize(message);
                    
                                String username = createMsg.getUser();
                                String password = createMsg.getPass();
                                String pubkey = createMsg.getPubkey();
                    
                                boolean success = userDb.createUser(username, password, pubkey);
                    
                                if (success) {
                                    // later : retrieve totpKey and include in payload
                                    common.StatusMessage response = new common.StatusMessage(true, "User created.");
                                    socket.sendMessage(response);

                                } else {
                                    common.StatusMessage response = new common.StatusMessage(false, "User already exists.");
                                    socket.sendMessage(response);
                                }
                    
                            } catch (Exception e) {
                                e.printStackTrace();
                                common.StatusMessage error = new common.StatusMessage(false, "Unexpected error.");
                                socket.sendMessage(error);
                            }
                            break;
                    
                        default:
                            StatusMessage unknown = new StatusMessage(false, "Unknown message type.");
                            socket.sendMessage(unknown);
                            break;
                    }
                    
                    // todo add routing logic here for different types like create authenticate post etc

                } catch (IOException e) {
                    System.out.println("error reading message");
                    e.printStackTrace();
                } finally {
                    socket.close();
                }
            }

        } catch (Exception e) {
            System.out.println("fatal error starting server");
            e.printStackTrace();
        }
    }
}
