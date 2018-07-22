package ldc.vertx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.Vertx;

public class LDCService {
    private static final Logger logger = LogManager.getLogger(LDCService.class);
    
    public static void main(String[] argv) {
        Vertx vertx = Vertx.vertx();

        logger.info("Starting {} and {}", GdaxTradeServer.class.getSimpleName(), GdaxClient.class.getSimpleName());
        vertx.deployVerticle(GdaxTradeServer.class.getName());
        vertx.deployVerticle(GdaxClient.class.getName());
    }
}
