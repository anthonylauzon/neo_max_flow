package org.neo4j.max_flow;

import java.io.IOException;

import com.sun.jersey.api.client.Client;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.server.NeoServer;
import org.neo4j.server.helpers.CommunityServerBuilder;
import org.neo4j.server.rest.JaxRsResponse;
import org.neo4j.server.rest.RestRequest;
import static org.junit.Assert.assertEquals;


public class MaxFlowServiceFunctionalTest {

  public static final Client CLIENT = Client.create();
  public static final String MOUNT_POINT = "/ext";
  private ObjectMapper objectMapper = new ObjectMapper();
  private Node nodes[] = new Node[7];

  private static final RelationshipType CONNECTED = DynamicRelationshipType
    .withName("CONNECTED");

  @Test
  public void shouldReturnConnectedComponentCount() throws IOException {
    NeoServer server = CommunityServerBuilder
      .server()
      .withThirdPartyJaxRsPackage("org.neo4j.max_flow", MOUNT_POINT)
      .build();

    server.start();

    populateDb(server.getDatabase().getGraph());

    RestRequest restRequest = new RestRequest(server.baseUri()
      .resolve(MOUNT_POINT), CLIENT);
    JaxRsResponse response = restRequest.get("service/max_flow/0/6");

    assertEquals("5", response.getEntity());

    server.stop();

  }

  private void populateDb(GraphDatabaseService db) {
    Transaction tx = db.beginTx();
    try {
      nodes[0] = createNode(db, "A");
      nodes[1] = createNode(db, "AA");
      nodes[2] = createNode(db, "AB");
      nodes[3] = createNode(db, "AC");
      nodes[4] = createNode(db, "BA");
      nodes[5] = createNode(db, "BB");
      nodes[6] = createNode(db, "CA");

      connectNodes(db, nodes[0], nodes[1], CONNECTED, 1);
      connectNodes(db, nodes[0], nodes[2], CONNECTED, 3);
      connectNodes(db, nodes[0], nodes[3], CONNECTED, 1);
      connectNodes(db, nodes[1], nodes[4], CONNECTED, 1);
      connectNodes(db, nodes[2], nodes[4], CONNECTED, 1);
      connectNodes(db, nodes[2], nodes[5], CONNECTED, 2);
      connectNodes(db, nodes[3], nodes[5], CONNECTED, 1);
      connectNodes(db, nodes[4], nodes[6], CONNECTED, 2);
      connectNodes(db, nodes[5], nodes[6], CONNECTED, 3);

      tx.success();
    } finally {
      tx.finish();
    }
  }

  private Node createNode(GraphDatabaseService db, String name) {
    Node node = db.createNode();
    node.setProperty("name", name);
    return node;
  }

  private void connectNodes(GraphDatabaseService db, Node from, Node to,
    RelationshipType type, Integer weight) {

    Relationship r = from.createRelationshipTo(to, type);
    r.setProperty("weight", weight);
  }

}
