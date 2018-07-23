package ldc.vertx;

import java.io.File;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Deploy a GdaxClient and GdaxTradeServer to receive bitcoin trade data from GDAX and available on localhost:8080
 * 
 * @author chengli
 *
 */
public class LDCService {
    private static final Logger logger = LogManager.getLogger(LDCService.class);
    private static final String BTC_SUB_CONFIG_FILE = "src/main/resources/btc_sub.json";
    
    public static void main(String[] argv) {
        Vertx vertx = Vertx.vertx();

        logger.info("Starting {} and {}", GdaxTradeServer.class.getSimpleName(), GdaxClient.class.getSimpleName());
        
        JsonObject subConfig = createSubConfig();
        if (subConfig == null || subConfig.isEmpty()) {
            // fail fast
            logger.fatal("No subscription config found for GdaxClient, json object: {}", subConfig);
            System.exit(1);
        } else {
            logger.info("Subscription json object: {}", subConfig);
        }
        
        GdaxClient gdaxClient = new GdaxClient(subConfig);
        vertx.deployVerticle(gdaxClient);
        vertx.deployVerticle(GdaxTradeServer.class.getName());
    }
    
    private static JsonObject createSubConfig() {
        JsonObject subConfig = null;
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> objectMap = mapper.readValue(new File(BTC_SUB_CONFIG_FILE), Map.class);
            subConfig = new JsonObject(objectMap);
            
        } catch (Exception e) {
            logger.error("Failed to read subscription file {} to json object", BTC_SUB_CONFIG_FILE, e);
        }
        
        return subConfig;
    }
}
