import com.vividsolutions.jts.geom.Geometry;
import edu.koritsas.aco.components.*;
import edu.koritsas.aco.components.shapefileutils.GraphUtils;
import edu.koritsas.aco.components.shapefileutils.IrrigationNetwork;
import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.resources.GraphicsUtilities;
import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Created by ilias on 20/9/2016.
 */
public class ACOMain {
    public static void main(String[] args) {

//δφγ
        IrrigationNetwork irrigationNetwork = new IrrigationNetwork("C:/Users/ilias/Desktop/ParametrizedTests/Hbenchmark11.shp", "C:/Users/ilias/Desktop/ParametrizedTests/Wbenchmark11.shp", "C:/Users/ilias/Desktop/ParametrizedTests/Pbenchmark11.shp");

        Graph graph = null;
        try {
            graph = irrigationNetwork.getBasicGraph();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Node> sources = irrigationNetwork.getWaterSource();
        Node source =sources.get(0);

        List<Node> sinks =irrigationNetwork.getHydrants();


        Environment environment = new Environment(graph);

        FireAntColony colony = new FireAntColony(15) {
            @Override
            public FireAnt createFireAnt() {
                return new FireAnt(source) {
                    @Override
                    public List<Edge> getNeighbourhood(List<Edge> visitedEdges, List<Node> visitedNodes) {
                        List<Edge> neighbourhood = new ArrayList<>();

                        for (Node n:visitedNodes){
                            neighbourhood.addAll(n.getEdges());
                        }

                        return neighbourhood;
                    }

                    @Override
                    public boolean solutionViolatesConstraints(GraphBuilder solution) {

                        Node sink =sinks.get(0);

                        SimpleFeature sourcef = (SimpleFeature) source.getObject();
                        SimpleFeature sinkf = (SimpleFeature) sink.getObject();

                        double Ho= (double) sourcef.getAttribute("hdemand");
                        double He= (double) sinkf.getAttribute("hdemand");

                        double dh = (double) solution.getGraph().getEdges().stream().collect(Collectors.summingDouble(new ToDoubleFunction<Edge>() {
                            @Override
                            public double applyAsDouble(Edge edge) {
                                SimpleFeature f = (SimpleFeature) edge.getObject();
                                Geometry g = (Geometry) f.getDefaultGeometry();
                                double L =g.getLength();
                                double c = (double) f.getAttribute("Cost");
                                double dh = (double) f.getAttribute("Dh");
                                return dh;
                            }
                        }));
                        boolean violates=false;

                        if ((Ho-He)<dh){
                            violates=true;
                        }

                        return violates;
                    }

                    @Override
                    public boolean violatesConstraints(Edge edge) {
                        return false;
                    }

                    @Override
                    public double getEdgeHeuristicValue(Edge edge) {
                        SimpleFeature f = (SimpleFeature) edge.getObject();
                        Geometry g = (Geometry) f.getDefaultGeometry();
                        double L =g.getLength();
                        double c = (double) f.getAttribute("Cost");

                        double h=1/(L*c);
                        return h;
                    }

                    @Override
                    public boolean isSolutionCompleted() {
                        boolean completed =getSolution().getGraph().getNodes().containsAll(sinks);
                        return completed;
                    }

                    @Override
                    public double calculateSolutionCost(GraphBuilder solution) {
                        List<Edge> edges =new ArrayList<>(solution.getGraph().getEdges());

                        double cost =edges.stream().collect(Collectors.summingDouble(new ToDoubleFunction<Edge>() {
                            @Override
                            public double applyAsDouble(Edge edge) {
                                SimpleFeature f = (SimpleFeature) edge.getObject();
                                Geometry g = (Geometry) f.getDefaultGeometry();
                                double L =g.getLength();
                                double c = (double) f.getAttribute("Cost");
                                return c*L;
                            }
                        }));

                        return cost;
                    }

                    @Override
                    public boolean isEdgeValid(Edge e) {
                        if (getVisitedNodes().contains(e.getNodeA())&&getVisitedNodes().contains(e.getNodeB())){
                            return false;
                        }else {

                            return true;
                        }
                    }
                };
            }
        };

       // AntSystemAlogrithm AS = new AntSystemAlogrithm(environment,colony,200,5,0.5,0.5,0.5);
        MaxMinAntSystemAlgorithm AS = new MaxMinAntSystemAlgorithm(environment,colony,200,5,0.5,0.5,0.5,2,15);
        AS.execute();
        AS.createCostGraph();
        GraphUtils.visualizeGraph(AS.getBestSolution().getGraph());

        System.out.println(AS.getNumberOfIterationBest());


    }
}
