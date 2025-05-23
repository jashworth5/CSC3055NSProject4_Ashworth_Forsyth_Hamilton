package server;

import util.SocketWrapper;
import common.AuthenticateMessage;
import common.CreateMessage;
import common.GetMessage;
import common.PostMessage;
import common.PostObject;
import common.PubKeyRequestMessage;
import common.ResponseMessage;
import common.StatusMessage;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.IOException;
import java.util.List;

import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import util.SocketWrapper;

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

            System.out.println("[+] server started on port " + port);

        

            // server loop to accept clients
            while (true) {

                SSLSocket client = (SSLSocket) serverSocket.accept();
                System.out.println("[+] client connected");
                SocketWrapper socket = new SocketWrapper(client);
                
                try {

                    // read message from client, get type
                    JSONObject message = socket.receiveMessage();
                    String type = (String) message.get("type");

                    // check if type is null
                    if (type == null) {
                        System.out.println("missing type field in client message");
                        return;
                    }
                    
                    System.out.println("Got type: " + type);


                    // switch statement depending on which type of message the client sent
                    switch (type) {
                        case "Create":
                            CreateMessage createMsg = new CreateMessage();
                            createMsg.deserialize(message);
                            
                            boolean created = userDb.createUser(
                                createMsg.getUser(),
                                createMsg.getPass(),
                                createMsg.getPubkey()
                            );

                            if (created) {

                                // fetch the generated TOTP key and send it back to the client
                                String totpKey = userDb.getTotpKey(createMsg.getUser());
                                socket.sendMessage(new StatusMessage(true, totpKey));
                            } else {
                                socket.sendMessage(new StatusMessage(false, "User already exists."));
                            }
                            break;

                        case "Authenticate":
                            AuthenticateMessage auth = new AuthenticateMessage();
                            auth.deserialize(message);  // ✅ deserialize first
                        
                            boolean passOk = userDb.validatePassword(auth.getUser(), auth.getPass());
                            boolean totpOk = userDb.validateTOTP(auth.getUser(), auth.getOtp());
                        
                            System.out.println("Pass OK? " + passOk);
                            System.out.println("TOTP OK? " + totpOk);
                        
                            boolean valid = passOk && totpOk;
                            socket.sendMessage(new StatusMessage(valid, valid ? "Authentication successful." : "Authentication failed."));
                            break;

                        case "PubKeyRequest":
                            PubKeyRequestMessage req = new PubKeyRequestMessage();
                            req.deserialize(message);
                            String pubkey = userDb.getPublicKey(req.getUser());
                            boolean found = pubkey != null;
                            socket.sendMessage(new StatusMessage(found, found ? pubkey : "User not found."));
                            break;

                        case "Post":
                            PostMessage postMsg = new PostMessage();
                            postMsg.deserialize(message);
                            boolean exists = userDb.userExists(postMsg.getUser());
                            boolean saved = exists && board.addPost(new PostObject(
                                postMsg.getUser(),
                                postMsg.getMessage(),
                                postMsg.getWrappedKey(),
                                postMsg.getIv()
                            ));
                            socket.sendMessage(new StatusMessage(saved, saved ? "Message posted." : "Target user not found."));
                            break;

                        case "GetMessage":
                            GetMessage getMsg = new GetMessage();
                            getMsg.deserialize(message);
                        
                            List<PostObject> posts = board.getPosts(getMsg.getUser());
                            System.out.println("[GetMessage] Posts found: " + posts.size());
                        
                            if (posts.isEmpty()) {
                                System.out.println("[GetMessage] No posts — sending status.");
                                socket.sendMessage(new StatusMessage(false, "No such user or no messages."));
                            } else {
                                System.out.println("[GetMessage] Sending ResponseMessage...");
                                socket.sendMessage(new ResponseMessage(posts));
                            }
                            break;
                        
                    }
                    
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
