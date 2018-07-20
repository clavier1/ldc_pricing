package ldc.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import ldc.utils.Runner;

public class GdaxTradeServer extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Runner.runClusteredExample(GdaxTradeServer.class);
    }

    @Override
    public void start() throws Exception {

        EventBus eb = vertx.eventBus();

        vertx.createHttpServer().requestHandler(req -> {

            req.response().end("Starting streaming GDAX price");
            
 

        }).listen(8080, listenResult -> {
            if (listenResult.failed()) {
                System.out.println("Could not start HTTP server");
                listenResult.cause().printStackTrace();
            } else {
                System.out.println("Server started");
                
                eb.consumer("gdax-price-receiver", message -> {

                    System.out.println("[GdaxTradeServer] Received message: " + message.body());

                });
            }
        });
        
        eb.consumer("gdax-price-receiver", message -> {

            System.out.println("[GdaxTradeServer] Received message: " + message);

        });

    }
}

