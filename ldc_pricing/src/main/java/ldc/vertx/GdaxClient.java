package ldc.vertx;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import ldc.utils.GdaxMessageType;

public class GdaxClient extends AbstractVerticle {
    private static final Logger logger = LogManager.getLogger(GdaxClient.class);

    private static final String BTC_USD = "BTC-USD";
    
    @Override
    public void start() throws Exception {
        RequestOptions ro = new RequestOptions();
        ro.setHost("ws-feed.gdax.com").setPort(443).setSsl(true).setURI("/");
        
        JsonObject subscription = createSubscription();
        
        HttpClient client = vertx.createHttpClient();
        client.websocket(ro, websocket -> {
            websocket.handler(data -> {
                handleData(data);              
//                String info = data.toString("ISO-8859-1");

            });
            
            websocket.writeTextMessage(subscription.toString());
        });
    
        
    }
    
    private JsonObject createSubscription() {
        // a bit hard coded. should be some xml configurable if multiple subscriptions. 
        JsonArray currency = new JsonArray(new ArrayList<String>(Arrays.asList("BTC-USD")));
        JsonArray field = new JsonArray(new ArrayList<String>(Arrays.asList("matches")));
        
        JsonObject subscription = new JsonObject();
        subscription.put("type", "subscribe");
        subscription.put("product_ids", currency);
        subscription.put("channels", field);
        
        return subscription;
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