package x.opcua.client;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.l;

public class OPCUAClientMethod implements OPCUAClientInterface {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public OPCUAClientMethod(boolean exampleServer) {
    try {
      new OPCUAClientRunner(this, exampleServer).run();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
    // synchronous connect
    client.connect().get();

    // call the sqrt(x) function
    int loops = 1000;

    long startTime = System.currentTimeMillis();
    for (int k = 1; k < loops; k++) {
      sqrt(client, (double) k, k, startTime).exceptionally(ex -> {
        logger.error("error invoking sqrt()", ex);
        return -1.0;
      }).thenAccept(v -> {
        logger.info("sqrt(16)={}", v);
      });
    }
    future.complete(client);
  }

  private CompletableFuture<Double> sqrt(OpcUaClient client, Double input, int counter, long startTime) {
    NodeId objectId = NodeId.parse("ns=2;s=HelloWorld");
    NodeId methodId = NodeId.parse("ns=2;s=HelloWorld/sqrt(x)");
    CallMethodRequest request = new CallMethodRequest(objectId, methodId, new Variant[]{new Variant(input)});
    return client.call(request).thenCompose(result -> {
      StatusCode statusCode = result.getStatusCode();
      long elapsed = System.currentTimeMillis() - startTime;
      double elapsed_single = (double) elapsed / (double) counter;
      logger.info("EL (" + counter + "): " + elapsed + " ELG (S): " + elapsed_single);
      if (statusCode.isGood()) {
        Double value = (Double) l(result.getOutputArguments()).get(0).getValue();
        return CompletableFuture.completedFuture(value);
      } else {
        StatusCode[] inputArgumentResults = result.getInputArgumentResults();
        for (int i = 0; i < inputArgumentResults.length; i++) {
          logger.error("inputArgumentResults[{}]={}", i, inputArgumentResults[i]);
        }
        CompletableFuture<Double> f = new CompletableFuture<>();
        f.completeExceptionally(new UaException(statusCode));
        return f;
      }
    });
  }

}
