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

        System.out.println("=== Secure Bulletin Board Client ===");
        
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            
            try {
                switch (choice) {
                    case "1":
                        createAccount();
                        break;
                    case "2":
                        authenticate();
                        break;
                    case "3":
                        postMessage();
                        break;
                    case "4":
                        getMessages();
                        break;
                    case "5":
                        running = false;
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\nWhat would you like to do?");
        System.out.println("1. Create a new account");
        System.out.println("2. Login to existing account");
        System.out.println("3. Post a message");
        System.out.println("4. Get your messages");
        System.out.println("5. Exit");
        System.out.print("> ");
    }

    //Create Account
    private static void createAccount() throws Exception {
        System.out.println("\n=== Create New Account ===");
        
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        
        System.out.println("Creating account...");
        String totpKey = client.createAccount(username, password);
        
        System.out.println("\nAccount created successfully!");
        System.out.println("Your TOTP key (Base64): " + totpKey);
        System.out.println("Please save this key and use it with a TOTP app like Google Authenticator");
        System.out.println("For testing, you can use https://totp.danhersam.com/ with this key");
    }


    //Authenticate with server

    private static void authenticate() throws Exception {
        System.out.println("\n=== Login ===");
        
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        
        System.out.print("Enter TOTP code: ");
        String otpCode = scanner.nextLine().trim();
        
        System.out.println("Authenticating...");
        boolean success = client.authenticate(username, password, otpCode);
        
        if (success) {
            System.out.println("Authentication successful!");
        } else {
            System.out.println("Authentication failed. Please check your credentials.");
        }
    }

    //Post message to board

    private static void postMessage() throws Exception {
        if (client.getUsername() == null) {
            System.out.println("You must log in first!");
            return;
        }
        
        System.out.println("\n=== Post Message ===");
        
        System.out.print("Enter recipient username: ");
        String recipient = scanner.nextLine().trim();
        
        System.out.print("Enter message text: ");
        String message = scanner.nextLine();
        
        System.out.println("Posting message...");
        boolean success = client.postMessage(recipient, message);
        
        if (success) {
            System.out.println("Message posted successfully!");
        } else {
            System.out.println("Failed to post message.");
        }
    }

    //Get messages from board

    private static void getMessages() throws Exception {
        if (client.getUsername() == null) {
            System.out.println("You must log in first!");
            return;
        }
        
        System.out.println("\n=== Your Messages ===");
        System.out.println("Retrieving messages...");
        
        List<String> messages = client.getMessages();
        
        if (messages.isEmpty()) {
            System.out.println("You have no messages.");
        } else {
            System.out.println("You have " + messages.size() + " message(s):");
            for (int i = 0; i < messages.size(); i++) {
                System.out.println((i + 1) + ". " + messages.get(i));
            }
        }
    }
}
