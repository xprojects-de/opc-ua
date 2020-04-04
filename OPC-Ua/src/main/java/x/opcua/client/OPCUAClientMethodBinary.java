package x.opcua.client;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.l;

public class OPCUAClientMethodBinary implements OPCUAClientInterface {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public OPCUAClientMethodBinary(boolean exampleServer) {
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
    int size = (1024 * 1024);

    long startTime = System.currentTimeMillis();
    for (int k = 1; k < loops; k++) {
      startTime = System.currentTimeMillis();
      CompletableFuture<ByteString> req = sqrt(client, size, k, startTime);
      ByteString v = req.get();
      if (v != null) {
        long elapsed = System.currentTimeMillis() - startTime;
        logger.info("EL (" + k + "): " + elapsed + " datalength: " + v.length());
      }
    }
    future.complete(client);
  }

  private CompletableFuture<ByteString> sqrt(OpcUaClient client, int input, int counter, long startTime) {
    NodeId objectId = NodeId.parse("ns=2;s=HelloWorld");
    NodeId methodId = NodeId.parse("ns=2;s=HelloWorld/binarydata(x)");
    CallMethodRequest request = new CallMethodRequest(objectId, methodId, new Variant[]{new Variant(input)});
    return client.call(request).thenCompose(result -> {
      StatusCode statusCode = result.getStatusCode();
      if (statusCode.isGood()) {
        ByteString value = (ByteString) l(result.getOutputArguments()).get(0).getValue();
        return CompletableFuture.completedFuture(value);
      } else {
        return null;
      }
    });
  }
}
