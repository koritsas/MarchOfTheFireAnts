package edu.koritsas.components.test;

import edu.koritsas.aco.components.ACOProblemSolver;
import edu.koritsas.aco.components.Environment;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.basic.BasicEdge;
import org.geotools.graph.structure.basic.BasicNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by ilias on 20/9/2016.
 */
public class ACOProblemSolverTest {
    ACOProblemSolver solver;
    Environment environment;
    @Before
    public void setUp() throws Exception {

        BasicGraphBuilder builder = new BasicGraphBuilder();

        BasicEdge e1 = new BasicEdge(new BasicNode(),new BasicNode());
        BasicEdge e2 = new BasicEdge(new BasicNode(),new BasicNode());

        builder.addEdge(e1);
        builder.addEdge(e2);

        e1.setVisited(true);
        e2.setVisited(true);
        e1.getNodeB().setVisited(true);
        Graph graph =builder.getGraph();

        environment = new Environment(graph);
        environment.initilizePheromones(1);

        solver = new ACOProblemSolver(environment,null,0,0.4,0,0);

    }

    @After
    public void tearDown() throws Exception {
        solver=null;
    }

    @Test
    public void performPheromoneEvaporation() throws Exception {
        solver.performPheromoneEvaporation();
        Collection<Edge> edges =environment.getGraph().getEdges();
        for (Edge e:edges){
            assertTrue(environment.getPheromones().get(e)==0.6);
        }

    }

}