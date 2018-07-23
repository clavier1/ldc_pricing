package ldc.vertx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;

public class GdaxTradeServer extends AbstractVerticle {
    private static final Logger logger = LogManager.getLogger(GdaxTradeServer.class);
    
    private static final String BTC_USD = "BTC-USD";
    private static final int PORT = 8080;

    @Override
    public void start() throws Exception {
        JsonObject latestPrice = initPrice();
        
        EventBus eb = vertx.eventBus();
        eb.consumer("gdax-price-receiver", message -> {
            logger.info("Received message: {}", message.body());
            updatePrice(latestPrice, message.body());
        });
        
        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(req -> {
            
            if (req.method() == HttpMethod.GET) {
                req.response().end(latestPrice.toString());
            }

        });
        
        httpServer.listen(PORT, listenResult -> {
            if (listenResult.failed()) {
                logger.error("Failed to start HTTP server. {}", listenResult.cause());
            } else {
                logger.info("{} start listening to port: {}", this.getClass().getSimpleName(), PORT);
            }
        });  
    }
    
    private JsonObject initPrice() {
        JsonObject priceJson = new JsonObject();
        priceJson.put(BTC_USD, Double.NaN);
        return priceJson;
    }
    
    private void updatePrice(JsonObject priceJson, Object messageBody) {
        JsonObject messageJson = (JsonObject) messageBody;
        priceJson.put(BTC_USD, messageJson.getString("price"));
    }
}

