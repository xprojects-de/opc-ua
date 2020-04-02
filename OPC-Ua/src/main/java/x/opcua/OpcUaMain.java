package x.opcua;

import java.util.concurrent.CompletableFuture;
import x.opcua.client.OPCUAClientBrowser;
import x.opcua.client.OPCUAClientMethod;
import x.opcua.client.OPCUAClientSubscription;
import x.opcua.client.OPCUAClientTrigger;
import x.opcua.server.ExampleServer;

public class OpcUaMain {

  public static void main(String[] args) throws Exception {
    ExampleServer server = new ExampleServer();
    server.startup().get();
    final CompletableFuture<Void> future = new CompletableFuture<>();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> future.complete(null)));

    //OPCUAClientSubscription c = new OPCUAClientSubscription(false);
    //OPCUAClientMethod m = new OPCUAClientMethod(false);
    //OPCUAClientBrowser b = new OPCUAClientBrowser(false);
    OPCUAClientTrigger b = new OPCUAClientTrigger(false);

    future.get();
  }

}
