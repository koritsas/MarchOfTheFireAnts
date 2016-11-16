import edu.koritsas.aco.components.AntSystemAlogrithm;
import edu.koritsas.aco.components.Environment;
import edu.koritsas.aco.components.FireAnt;
import edu.koritsas.aco.components.FireAntColony;
import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ilias on 20/9/2016.
 */
public class ACOMain {
    public static void main(String[] args) {




        Environment environment = new Environment(graph);

        FireAntColony colony = new FireAntColony(10) {
            @Override
            public FireAnt createFireAnt() {
                return new FireAnt(n0) {
                    @Override
                    public boolean isEdgeValid(Edge e) {

                        return !getVisitedNodes().contains(e.getNodeA())&&!getVisitedNodes().contains(e.getNodeB());
                    }

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

                        double L =(double) edge.getObject();
                        return 1/L;
                    }

                    @Override
                    public boolean isSolutionCompleted() {

                       // return environment.getGraph().getNodes().size()==this.getVisitedNodes().size();
                        return this.getVisitedNodes().contains(n3);
                    }

                    @Override
                    public double calculateSolutionCost(GraphBuilder solution) {
                        Collection<Edge> edges=solution.getGraph().getEdges();
                        double cost =0;
                        for (Edge e:edges){

                            double L = (double)e.getObject();
                            cost =cost+L;
                        }
                        return cost;
                    }




                };
            }
        };


        AntSystemAlogrithm solver = new AntSystemAlogrithm(environment,colony,100,5,0.4,0.5,0.5);
        solver.execute();
        System.out.println(solver.getBestSolutionCost()+" "+solver.getBestSolution().getGraph().toString());

    }
}
