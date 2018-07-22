package ldc.vertx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import ldc.utils.GdaxMessageType;

public class GdaxClient extends AbstractVerticle {
    private static final Logger logger = LogManager.getLogger(GdaxClient.class);
    
    private final JsonObject subConfig;
    
    public GdaxClient(JsonObject subConfig) {
        this.subConfig = subConfig;
    } 
    
    @Override
    public void start() throws Exception {
        RequestOptions ro = new RequestOptions();
        ro.setHost("ws-feed.gdax.com").setPort(443).setSsl(true).setURI("/");
        
        HttpClient client = vertx.createHttpClient();
        client.websocket(ro, websocket -> {
            websocket.handler(data -> {
                
                handleData(data);
                
            });
            
            websocket.writeTextMessage(subConfig.toString());
        });
        
    }
    
    private void handleData(Buffer data) {
        // may optimize by only parse the "type" instead of dumping all to the json object, if buffer very large
        JsonObject dataJson = new JsonObject(data);
        GdaxMessageType messageType = GdaxMessageType.valueOf(dataJson.getString("type"));
        
        switch (messageType) {
            case subscriptions:
                logger.info("Successfully subscribing: {}", dataJson);
                break;
            case error:
                logger.error("Failed to subscribe: {}", dataJson);
                // close client?
                break;
            case last_match:
            case match:
                logger.info("Received trade data {}", dataJson);
                EventBus eb = vertx.eventBus();
                eb.send("gdax-price-receiver", dataJson);
                break;
            default:
                logger.error("Unknow message type: {}", dataJson);
        }
            
    }
    
}