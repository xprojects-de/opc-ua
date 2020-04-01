package x.opcua;

import java.util.concurrent.CompletableFuture;
import x.opcua.client.OPCUAClient;
import x.opcua.server.ExampleServer;

public class OpcUaMain {

  public static void main(String[] args) throws Exception {
    ExampleServer server = new ExampleServer();
    server.startup().get();
    final CompletableFuture<Void> future = new CompletableFuture<>();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> future.complete(null)));
    OPCUAClient c = new OPCUAClient(false);
    future.get();
  }

}
