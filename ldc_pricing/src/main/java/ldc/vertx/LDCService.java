package ldc.vertx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class LDCService {
    private static final Logger logger = LogManager.getLogger(LDCService.class);
    private static final String BTC_SUB_CONFIG_FILE = "resources/btc_sub.json";
    
    public static void main(String[] argv) {
        Vertx vertx = Vertx.vertx();

        logger.info("Starting {} and {}", GdaxTradeServer.class.getSimpleName(), GdaxClient.class.getSimpleName());
        
        JsonObject subConfig = createSubConfig(vertx);
        if (subConfig.isEmpty()) {
            // fail fast
            logger.fatal("No subscription config found for GdaxClient. Exit!");
            System.exit(1);
        }
        
        GdaxClient gdaxClient = new GdaxClient(subConfig);
        vertx.deployVerticle(gdaxClient);
        vertx.deployVerticle(GdaxTradeServer.class.getName());
    }
    
    private static JsonObject createSubConfig(Vertx vertx) {
        JsonObject subConfig = new JsonObject();
        
        vertx.fileSystem().readFile(BTC_SUB_CONFIG_FILE, result -> {
           if (result.succeeded()) {
               subConfig.mergeIn(new JsonObject(result.result()));
           } else {
               logger.fatal("Failed to read subscription file: {}", BTC_SUB_CONFIG_FILE, result.cause());
           }
        });
        
        return subConfig;
    }
}
