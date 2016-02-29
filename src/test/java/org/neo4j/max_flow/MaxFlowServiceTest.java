package org.neo4j.max_flow;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class MaxFlowServiceTest {

  private GraphDatabaseService db;
  private MaxFlowService service;
  private ObjectMapper objectMapper = new ObjectMapper();
  private Node nodes[] = new Node[7];
  private static final RelationshipType CONNECTED = DynamicRelationshipType
    .withName("CONNECTED");

  @Before
  public void setUp() {
    db = new TestGraphDatabaseFactory().newImpermanentDatabase();
    populateDb(db);
    service = new MaxFlowService();
  }

  private void dropRootNode(GraphDatabaseService db){
    try (Transaction tx = db.beginTx()){
      Node root = db.getNodeById(0);
      root.delete();
      tx.success();
    }
  }

  private void populateDb(GraphDatabaseService db) {
    try (Transaction tx = db.beginTx()) {
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
    }
  }

  private Node createNode(GraphDatabaseService db, String name) {
    Node node = db.createNode();
    node.setProperty("name", name);
    return node;
  }

  private void connectNodes(GraphDatabaseService db,
    Node from,
    Node to,
    RelationshipType type,
    Integer weight) {

    Relationship r = from.createRelationshipTo(to, type);
    r.setProperty("weight", weight);
  }


  @After
  public void tearDown() throws Exception {
    db.shutdown();
  }

  @Test
  public void shouldGetConnectedComponentsCount() throws IOException {
    assertEquals("5", service.getMaxFlow(0L, 6L, db));
  }


  public GraphDatabaseService graphdb() {
    return db;
  }
}
