package ldc.vertx;

import java.util.ArrayList;
import java.util.Arrays;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import ldc.utils.Runner;

public class GdaxClient extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Runner.runClusteredExample(GdaxClient.class);
    }

    @Override
    public void start() throws Exception {
        
        JsonArray currency = new JsonArray(new ArrayList<String>(Arrays.asList("BTC-USD")));
        JsonArray field = new JsonArray(new ArrayList<String>(Arrays.asList("matches")));
        
        final JsonObject jo = new JsonObject();
        jo.put("type", "subscribe");
        jo.put("product_ids", currency);
        jo.put("channels", field);
        System.out.println(jo);

        RequestOptions ro = new RequestOptions();
        ro.setHost("ws-feed.gdax.com").setPort(443).setSsl(true).setURI("/");

        EventBus eb = vertx.eventBus();
        
        HttpClient client = vertx.createHttpClient();
        client.websocket(ro, websocket -> {
            websocket.handler(data -> {
                String info = data.toString("ISO-8859-1");
                System.out.println("Received data " + info);
                
                eb.send("gdax-price-receiver", info);
            });
            
            websocket.writeTextMessage(jo.toString());
        });
    
        
    }
    
    
}