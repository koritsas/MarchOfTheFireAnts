package edu.koritsas.components.test;

import edu.koritsas.aco.components.Environment;
import edu.koritsas.aco.components.FireAnt;
import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.basic.BasicEdge;
import org.geotools.graph.structure.basic.BasicNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by ilias on 19/9/2016.
 */
public class FireAntTest {


    FireAnt ant;
    Environment environment;
    Graph graph;
    @Before
    public void setUp() throws Exception {
        GraphBuilder graphBuilder = new BasicGraphBuilder();
        final BasicEdge e1 = new BasicEdge(new BasicNode(),new BasicNode());
        BasicEdge e2 = new BasicEdge(new BasicNode(),new BasicNode());

        graphBuilder.addEdge(e1);
        graphBuilder.addEdge(e2);
         graph =graphBuilder.getGraph();

        environment = new Environment(graph);
        environment.initilizePheromones(5);


        ant=new FireAnt(e1.getNodeA()) {

            @Override
            public List<Edge> getNeighbourhood(Environment environment) {
                return new ArrayList<Edge>(graph.getEdges());
            }

            @Override
            public boolean violatesConstraints(Edge edge) {
                return false;
            }

            @Override
            public double getEdgeHeuristicValue(Edge edge) {
                return 5;
            }

            public boolean isSolutionCompleted() {
                return false;
            }

            public double calculateSolutionCost(GraphBuilder solution) {
                return 5;
            }
        };
        ant.setEnvironment(environment);

    }

    @After
    public void tearDown() throws Exception {
            ant=null;
    }

    @Test
    public void calculateProbabilities() throws Exception {

        HashMap<Edge,Double> map =ant.calculateProbabilities(ant.getNeighbourhood(environment),1,1);
        for (Object e:ant.getNeighbourhood(environment)){
            assertTrue(map.get(e)==0.5);
        }
    }
    @Test
    public void depositPheromone() throws Exception {

    }

}