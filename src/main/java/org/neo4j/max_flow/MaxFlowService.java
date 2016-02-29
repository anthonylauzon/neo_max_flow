package org.neo4j.max_flow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphalgo.*;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;
import static org.neo4j.graphalgo.GraphAlgoFactory.allPaths;


@Path("/service")
public class MaxFlowService {

  private int getFlow(GraphDatabaseService db, Long source_node_id,
    Long sink_node_id) {

    int maxDepth = 10000;
    int flow = 0;
    int accumulator = 0;

    List<Integer> flows = new ArrayList<Integer>();

    Transaction tx = db.beginTx();
    try {
      Node source_node = db.getNodeById(source_node_id);
      Node sink_node = db.getNodeById(sink_node_id);

      for (org.neo4j.graphdb.Path p :
        allPaths(PathExpanders.forDirection(Direction.OUTGOING), maxDepth)
        .findAllPaths(source_node, sink_node)) {

        for (Relationship r : p.relationships()) {
          flows.add((Integer)r.getProperty("weight"));
        }

        flow = Collections.min(flows);
        flows.clear();
        accumulator = accumulator + flow;

        for (Relationship r : p.relationships()) {
          r.setProperty("weight", (Integer)r.getProperty("weight") - flow );
        }

      }
      tx.success();
    } catch (Exception e) {
      tx.failure();
    } finally {
      tx.finish();
    }

    return accumulator;
  }

  @GET
  @Path("/max_flow/{source}/{sink}")
  public String getMaxFlow(@PathParam("source") Long source_id,
    @PathParam("sink") Long sink_id,
    @Context GraphDatabaseService db) throws IOException {

    return String.valueOf(getFlow(db, source_id, sink_id));
  }
}
