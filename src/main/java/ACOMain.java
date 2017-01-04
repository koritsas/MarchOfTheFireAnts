import com.vividsolutions.jts.geom.Geometry;
import edu.koritsas.aco.components.*;

import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.path.DijkstraShortestPathFinder;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.graph.traverse.standard.DijkstraIterator;
import org.geotools.resources.GraphicsUtilities;
import org.opengis.feature.simple.SimpleFeature;
import shapefile.GraphUtils;
import shapefile.IrrigationNetwork;

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
        IrrigationNetwork irrigationNetwork = new IrrigationNetwork("ParametrizedTests/Hbenchmark22.shp", "ParametrizedTests/Wbenchmark22.shp", "ParametrizedTests/Pbenchmark22.shp");

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

        Graph finalGraph = graph;
        FireAntColony colony = new FireAntColony(10) {
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
                        DijkstraIterator.EdgeWeighter weigter = new DijkstraIterator.EdgeWeighter() {
                        @Override
                        public double getWeight(Edge e) {

                            return 1;
                        }
                    };
                        DijkstraShortestPathFinder pf = new DijkstraShortestPathFinder(finalGraph, source, weigter);
                         synchronized (solution) {

                             pf.calculate();
                         }





                        SimpleFeature sourcef = (SimpleFeature) source.getObject();


                        double Ho= (double) sourcef.getAttribute("hdemand");

                        boolean violates =false;

                        List<Edge> edgeList = new ArrayList<>(solution.getGraph().getEdges());

                        List<Node> hydr= irrigationNetwork.getHydrants();

                        for (Node n:hydr){
                            Path path = pf.getPath(n);

                            List<Edge> actual = new ArrayList<>();

                            for (Edge ed:edgeList){
                                if (path.contains(ed.getNodeA())&&path.contains(ed.getNodeB())){
                                    actual.add(ed);
                                }
                            }


                            double dh = actual.stream().collect(Collectors.summingDouble(new ToDoubleFunction<Edge>() {
                                @Override
                                public double applyAsDouble(Edge edge) {
                                    SimpleFeature f= (SimpleFeature) edge.getObject();
                                      double dh= (double) f.getAttribute("Dh");
                                    return dh;
                                }
                            }));



                            SimpleFeature sinkf = (SimpleFeature) n.getObject();
                            double He= (double) sinkf.getAttribute("hdemand");

                            double DH= Ho-He;
                           // System.out.println("Διαθέσιμο: "+DH+"Πραγματικό: "+dh+" Διαφορά: "+(DH-dh));

                            if ((DH-dh)<0){

                                violates=true;
                                break;
                            }
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

        environment.initilizePheromones(5);
       //AntSystemAlogrithm AS = new AntSystemAlogrithm(environment,colony,100,0.000001,0.5,0.2,0.8);
        MaxMinAntSystemAlgorithm AS = new MaxMinAntSystemAlgorithm(environment,colony,500,0.000001,0.2,1,1,0,5);
        AS.execute();
        AS.createCostGraph();
        GraphUtils.visualizeGraph(AS.getBestSolution().getGraph());

        System.out.println(AS.getNumberOfIterationBest());
        environment.createPheromonesBarChart();



    }
}
