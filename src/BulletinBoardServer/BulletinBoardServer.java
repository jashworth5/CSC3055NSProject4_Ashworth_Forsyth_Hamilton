import java.io.FileReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class BulletinBoardServer {
    private static final String CONFIG_KEY_PORT = "port";
    private static final String CONFIG_KEY_KEYSTORE_FILE = "keystore-file";
    private static final String CONFIG_KEY_KEYSTORE_PASS = "keystore-pass";

    public static void main(String[] args) {
        if (args.length < 2 || !args[0].equals("--config")) {
            System.out.println("Usage: java BulletinBoardServer --config <configfile>");
            return;
        }

        String configPath = args[1];

        try {
            // Load configuration
            JSONParser parser = new JSONParser();
            JSONObject config = (JSONObject) parser.parse(new FileReader(configPath));

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