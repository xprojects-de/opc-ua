package x.opcua.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseResult;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.toList;

public class OPCUAClientBrowser implements OPCUAClientInterface {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public OPCUAClientBrowser(boolean exampleServer) {
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

    // start browsing at root folder
    browseNode("", client, Identifiers.RootFolder);

    future.complete(client);
  }

  private void browseNode(String indent, OpcUaClient client, NodeId browseRoot) {
    BrowseDescription browse = new BrowseDescription(
            browseRoot,
            BrowseDirection.Forward,
            Identifiers.References,
            true,
            uint(NodeClass.Object.getValue() | NodeClass.Variable.getValue()),
            uint(BrowseResultMask.All.getValue())
    );

    try {
      BrowseResult browseResult = client.browse(browse).get();

      List<ReferenceDescription> references = toList(browseResult.getReferences());

      for (ReferenceDescription rd : references) {
        logger.info("{} Node={}", indent, rd.getBrowseName().getName());

        // recursively browse to children
        rd.getNodeId().local().ifPresent(nodeId -> browseNode(indent + "  ", client, nodeId));
      }
    } catch (InterruptedException | ExecutionException e) {
      logger.error("Browsing nodeId={} failed: {}", browseRoot, e.getMessage(), e);
    }
  }

}
