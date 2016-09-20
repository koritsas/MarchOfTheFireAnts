import edu.koritsas.aco.components.ACOProblemSolver;
import edu.koritsas.aco.components.Environment;
import edu.koritsas.aco.components.FireAnt;
import edu.koritsas.aco.components.FireAntColony;
import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.basic.BasicEdge;
import org.geotools.graph.structure.basic.BasicNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ilias on 20/9/2016.
 */
public class ACOMain {
    public static void main(String[] args) {
        GraphBuilder builder = new BasicGraphBuilder();

        BasicNode n0 = new BasicNode();
        n0.setID(0);
        BasicNode n1 = new BasicNode();
        n1.setID(1);
        BasicNode n2 = new BasicNode();
        n2.setID(2);
        BasicNode n3 = new BasicNode();
        n3.setID(3);

        BasicEdge e1 = new BasicEdge(n0,n1);
        e1.setID(01);
        e1.setObject(3.0);

        BasicEdge e2 = new BasicEdge(n1,n3);
        e2.setID(13);
        e2.setObject(4.0);


        BasicEdge e3 = new BasicEdge(n0,n2);
        e3.setID(02);
        e3.setObject(10.0);


        BasicEdge e4 = new BasicEdge(n2,n3);
        e4.setID(24);
        e4.setObject(15.0);

        builder.addNode(n0);
        builder.addNode(n1);
        builder.addNode(n2);
        builder.addNode(n3);

        builder.addEdge(e1);
        builder.addEdge(e2);
        builder.addEdge(e3);
        builder.addEdge(e4);



        Graph graph = builder.getGraph();

        Environment environment = new Environment(graph);

        FireAntColony colony = new FireAntColony(10) {
            @Override
            public FireAnt createFireAnt() {
                return new FireAnt(n0) {


                    @Override
                    public List<Edge> getNeighbourhood(List<Edge> visitedEdges, List<Node> visitedNodes) {
                        List<Edge> neighbourhood = new ArrayList<>();
                        for (Node n:visitedNodes){
                            neighbourhood.addAll(n.getEdges());
                        }

                        return neighbourhood;
                    }

                    @Override
                    public boolean violatesConstraints(Edge edge) {
                        return false;
                    }

                    @Override
                    public double getEdgeHeuristicValue(Edge edge) {
                        double L = (double) edge.getObject();
                        return 1/L;
                    }

                    @Override
                    public boolean isSolutionCompleted() {

                        return environment.getGraph().getNodes().size()==this.getVisitedNodes().size();
                    }

                    @Override
                    public double calculateSolutionCost(GraphBuilder solution) {
                        Collection<Edge> edges=solution.getGraph().getEdges();
                        double cost =0;
                        for (Edge e:edges){
                            cost =cost+(double)e.getObject();
                        }
                        return cost;
                    }




                };
            }
        };


        ACOProblemSolver solver = new ACOProblemSolver(environment,colony,100,5,0.4,0.5,0.5);
        solver.execute();
        System.out.println(solver.getBestSolutionCost());

    }
}
