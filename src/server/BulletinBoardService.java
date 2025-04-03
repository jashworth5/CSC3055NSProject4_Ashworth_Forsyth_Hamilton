package server;
import java.io.File;

import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;



public class BulletinBoardService {
    private static final String CONFIG_KEY_PORT = "port";
    private static final String CONFIG_KEY_KEYSTORE_FILE = "keystore-file";
    private static final String CONFIG_KEY_KEYSTORE_PASS = "keystore-pass";

    public static void main(String[] args) {
        if (args.length < 2 || !args[0].equals("--config")) {
            System.out.println("Usage: java BulletinBoardService --config <configfile>");
            return;
        }

        String configPath = args[1];

        try {
            // Load configuration
            JSONObject config = JsonIO.readObject(new File("config.json"));

            if (config.get(CONFIG_KEY_PORT) == null ||
                    config.get(CONFIG_KEY_KEYSTORE_FILE) == null ||
                    config.get(CONFIG_KEY_KEYSTORE_PASS) == null) {
                throw new IllegalArgumentException("Missing required configuration keys.");
            }

            int port = Math.toIntExact((Long) config.get(CONFIG_KEY_PORT));
            String keystorePath = (String) config.get(CONFIG_KEY_KEYSTORE_FILE);
            String keystorePass = (String) config.get(CONFIG_KEY_KEYSTORE_PASS);

            System.out.println("[+] Config loaded. Port: " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}