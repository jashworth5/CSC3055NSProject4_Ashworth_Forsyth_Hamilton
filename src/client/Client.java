package client;

import common.*;
import util.SocketWrapper;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;

import util.EncryptionUtil;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;


public class Client {
    private String serverHost;
    private int serverPort;
    private String trustStoreFile;
    private String trustStorePassword;

    private String username;
    private String totpKey;
    private String privateKey;
    
    public Client(String serverHost, int serverPort, String trustStoreFile, String trustStorePassword) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.trustStoreFile = trustStoreFile;
        this.trustStorePassword = trustStorePassword;
    }

    private SocketWrapper connect() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        System.setProperty("javax.net.ssl.trustStore", trustStoreFile);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        SSLSocket socket = (SSLSocket) factory.createSocket(serverHost, serverPort);
        socket.startHandshake();
        return new SocketWrapper(socket);
    }

    public String createAccount(String username, String password) throws Exception {
        // Generate ElGamal key pair
        String[] keyPair = generateKeyPair();
        String publicKey = keyPair[0];
        this.privateKey = keyPair[1];
        
        // Connect to server
        SocketWrapper socket = connect();
        try {
            // Send create account message
            CreateMessage createMsg = new CreateMessage(username, password, publicKey);
            socket.sendMessage(createMsg);
            
            // Get response
            StatusMessage response = new StatusMessage();
            response.deserialize(socket.receiveMessage());
            
            if (!response.getStatus()) {
                throw new Exception("Account creation failed: " + response.getPayload());
            }
            
            // Save the returned TOTP key
            this.username = username;
            this.totpKey = response.getPayload();
            
            return this.totpKey;
        } finally {
            socket.close();
        }
    }

    public boolean authenticate(String username, String password, String otpCode) throws Exception {
        SocketWrapper socket = connect();
        try {
            // Send authentication message
            AuthenticateMessage authMsg = new AuthenticateMessage(username, password, otpCode);
            socket.sendMessage(authMsg);
            
            // Get response
            StatusMessage response = new StatusMessage();
            response.deserialize(socket.receiveMessage());
            
            if (response.getStatus()) {
                this.username = username;
                return true;
            } else {
                return false;
            }
        } finally {
            socket.close();
        }
    }

     /**
     * Retrieves the public key for a specific user
     * 
     * @param targetUser The username to get the public key for
     * @return The public key as a Base64 string
     * @throws Exception If the request fails
     */
    public String getPublicKey(String targetUser) throws Exception {
        if (username == null) {
            throw new IllegalStateException("Must be authenticated first");
        }
        
        SocketWrapper socket = connect();
        try {
            PubKeyRequestMessage keyRequest = new PubKeyRequestMessage(targetUser);
            socket.sendMessage(keyRequest);
            
            StatusMessage response = new StatusMessage();
            response.deserialize(socket.receiveMessage());
            
            if (!response.getStatus()) {
                throw new Exception("Public key request failed: " + response.getPayload());
            }
            
            return response.getPayload();
        } finally {
            socket.close();
        }
    }

    public boolean postMessage(String recipient, String message) throws Exception {
        if (username == null) {
            throw new IllegalStateException("Must be authenticated first");
        }
        
        // Get recipient's public key
        String recipientPublicKey = getPublicKey(recipient);
        
        // Encrypt the message
        // This would use EncryptionUtil methods
        String[] encrypted = encryptMessage(message, recipientPublicKey);
        String encryptedMessage = encrypted[0];     // AES-encrypted message
        String wrappedKey = encrypted[1];           // ElGamal-encrypted AES key
        String iv = encrypted[2];                   // Initialization vector
        
        // Send post request
        SocketWrapper socket = connect();
        try {
            PostMessage postMsg = new PostMessage(recipient, encryptedMessage, wrappedKey, iv);
            socket.sendMessage(postMsg);
            
            // Get response
            StatusMessage response = new StatusMessage();
            response.deserialize(socket.receiveMessage());
            
            return response.getStatus();
        } finally {
            socket.close();
        }
    }

    public List<String> getMessages() throws Exception {
        if (username == null) {
            throw new IllegalStateException("Must be authenticated first");
        }
        
        SocketWrapper socket = connect();
        try {
            // Send get message request
            GetMessage getMsg = new GetMessage(username);
            socket.sendMessage(getMsg);
            
            // Get response with messages
            StatusMessage response = new StatusMessage();
            response.deserialize(socket.receiveMessage());
            
            if (!response.getStatus()) {
                throw new Exception("Message retrieval failed: " + response.getPayload());
            }
            
            // Parse the JSON array of posts in the payload
            List<PostObject> posts = parsePostObjects(response.getPayload());
            
            // Decrypt each message
            return decryptPosts(posts, this.privateKey);
        } finally {
            socket.close();
        }
    }

    private String[] generateKeyPair() throws Exception {
        return EncryptionUtil.generateKeyPair();
    }

    private String[] encryptMessage(String message, String publicKey) throws Exception {
        return EncryptionUtil.encryptMessage(message, publicKey);
    }

    private List<PostObject> parsePostObjects(String jsonPayload) {
        try{
            JSONArray postsArray = JsonIO.readArray(jsonPayload);

            List<PostObject> posts = new ArrayList<>();

            for (int i = 0; i < postsArray.size(); i++) {
                PostObject post = new PostObject();
                post.deserialize((JSONType) postsArray.get(i));
                posts.add(post);
            }
            return posts;
        } catch (Exception e) {
            System.err.println("Failed to parse posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<String> decryptPosts(List<PostObject> posts, String privateKey) {
        List<String> decryptedMessages = new java.util.ArrayList<>();
        
        for (PostObject post : posts) {
            try {
                // Extract the encrypted components
                String encryptedMessage = post.getMessage();
                String wrappedKey = post.getWrappedKey();
                String iv = post.getIv();
                
                // Decrypt the message using the private key
                String decrypted = EncryptionUtil.decryptMessage(
                    encryptedMessage, wrappedKey, iv, privateKey);
                
                decryptedMessages.add(decrypted);
            } catch (Exception e) {
                // If we can't decrypt, add a placeholder message
                decryptedMessages.add("[Encrypted message - cannot decrypt]");
            }
        }
        
        return decryptedMessages;
    }

    public String getTotpKey(){
        return this.totpKey;
    }

    public String getUsername(){
        return this.username;
    }


}
