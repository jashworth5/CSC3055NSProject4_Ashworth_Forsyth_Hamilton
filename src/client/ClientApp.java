package src.client;

import java.util.List;
import java.util.Scanner;
import java.util.Base64;

public class ClientApp {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 4444;
    private static final String TRUSTSTORE_FILE = "clienttruststore.p12";
    private static final String TRUSTSTORE_PASSWORD = "password123";
    
    private static Client client;
    private static Scanner scanner;
    
    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        client = new Client(SERVER_HOST, SERVER_PORT, TRUSTSTORE_FILE, TRUSTSTORE_PASSWORD);
        
        boolean running = true;

        
    }
}
