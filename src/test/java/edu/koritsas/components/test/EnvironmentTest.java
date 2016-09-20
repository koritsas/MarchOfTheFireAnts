package edu.koritsas.components.test;

import edu.koritsas.aco.components.Environment;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.basic.BasicEdge;
import org.geotools.graph.structure.basic.BasicNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by ilias on 19/9/2016.
 */
public class EnvironmentTest {


    private Environment environment;
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
        environment=new Environment(graph);
    }

    @After
    public void tearDown() throws Exception {

        environment=null;

    }
    @Test
    public void initilizePheromones() throws Exception {
        environment.initilizePheromones(5);
        Collection<Edge> edges =environment.getGraph().getEdges();
        HashMap<Edge,Double> pheromones =environment.getPheromones();
        for (Edge e:edges){
            assertTrue(pheromones.get(e)==5);
        }

    }

    @Test
    public void initialize() throws Exception {
        environment.initialize();

        Collection<Edge> edges =environment.getGraph().getEdges();
        for (Edge e:edges){
            assertTrue(!e.isVisited());

        }
        Collection<Node> nodes =environment.getGraph().getNodes();
        for(Node n:nodes){
            assertTrue(!n.isVisited());
        }

    }

}