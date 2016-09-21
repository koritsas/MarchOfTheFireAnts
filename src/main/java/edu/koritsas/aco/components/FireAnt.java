package edu.koritsas.aco.components;

import edu.koritsas.aco.components.edu.koritsas.aco.exceptions.ConfigurationException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.apache.commons.math3.util.FastMath;
import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.basic.BasicDirectedEdge;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Created by ilias on 19/9/2016.
 */
public abstract class FireAnt {

    private GraphBuilder solution;
    private Node startNode;



    private List<Edge> visitedEdges = new ArrayList<>();
    private List<Node> visitedNodes = new ArrayList<>();


    public FireAnt(Node startNode){
        this.startNode=startNode;
        visitedNodes.add(startNode);

    }
    public List<Edge> getVisitedEdges() {
        return visitedEdges;
    }

    public List<Node> getVisitedNodes() {
        return visitedNodes;
    }



    public GraphBuilder getSolution(){

        return solution;
    }


    /**
     *
     * @return a list of components that are available for solution construction at each step
     */
    private List<Edge> getAvailableEdges(){
        List<Edge> neighbourhood=getNeighbourhood(visitedEdges,visitedNodes);
        List<Edge> availableEdges=  neighbourhood.stream().filter(edge -> !visitedEdges.contains(edge)).distinct().filter(edge -> !violatesConstraints(edge)).collect(Collectors.toList());

        if (availableEdges.isEmpty()){
            throw new ConfigurationException("There are no available edges for selection. Reconfigure neighbourhood");

        }

        return availableEdges;
    }
    /**
     *
     * @param visitedEdges
     * @return a list of components that are not visited
     */
    public abstract List<Edge> getNeighbourhood(List<Edge> visitedEdges, List<Node> visitedNodes);

    /**
     *
     * @param edge
     * @return true if the component violates some constraints when added to solution;
     */
    public abstract boolean violatesConstraints(Edge edge);

    /**
     *
     * @param edge
     * @return the heuristic value of the edge
     */
    public abstract double getEdgeHeuristicValue(Edge edge);

    private double calculateDenominator(Environment environment,double heuristicImportance, double pheromoneImportance){
        double denominator =getAvailableEdges().stream().collect(Collectors.summingDouble(new ToDoubleFunction<Edge>() {
            @Override
            public double applyAsDouble(Edge edge) {
                return calculateNumerator(environment,edge,heuristicImportance,pheromoneImportance);
            }
        }));

        return denominator;
    }
    private double calculateNumerator(Environment environment,Edge edge,double heuristicImportance, double pheromoneImportance){
        double numerator =getAvailableEdges().stream().filter(edge1 -> edge1!=null).collect(Collectors.summingDouble(new ToDoubleFunction<Edge>() {
            @Override
            public double applyAsDouble(Edge e) {
                double t;

                     t = environment.getPheromones().get(e);

               double h=getEdgeHeuristicValue(e);

                double n= FastMath.pow(t,pheromoneImportance)*FastMath.pow(h,heuristicImportance);

                return n;
            }
        }));

                return numerator;
    }

    /**
     *
     * @param availableEdges
     * @param heuristicImportance
     * @param pheromoneImportance
     * @return a map of the probabilities of choosing an edge
     */
    public HashMap<Edge,Double> calculateProbabilities(Environment environment,List<Edge> availableEdges, double heuristicImportance, double pheromoneImportance){

        HashMap<Edge,Double> probabilities = new HashMap<>(availableEdges.size());
        for (Edge e:availableEdges){
            probabilities.putIfAbsent(e,calculateNumerator(environment,e,heuristicImportance,pheromoneImportance)/calculateDenominator(environment,heuristicImportance,pheromoneImportance));
        }


        return probabilities;
    }

    /**
     *
     *
     * @param edge
     */
    private synchronized void addEdgeToSolution(Edge edge){
        this.solution.addEdge(edge);
        visitedEdges.add(edge);
        if (!visitedNodes.contains(edge.getNodeA())){
            visitedNodes.add(edge.getNodeA());
        }
        if (!visitedNodes.contains(edge.getNodeB())){
            visitedNodes.add(edge.getNodeB());
        }


    }

    /**
     *
     * @param availableEdges
     * @param heuristicImportance
     * @param pheromoneImportance
     * @return chooses an edge from available edges whith roulette wheel selection
     */
    protected Edge chooseRandomEdge(Environment environment,List<Edge> availableEdges,double heuristicImportance,double pheromoneImportance){

        HashMap<Edge,Double> probabilities =calculateProbabilities(environment,availableEdges,heuristicImportance,pheromoneImportance);

        RandomGenerator randomGenerator = new Well512a();
        double p=randomGenerator.nextDouble();

         //Edge edge=  availableEdges.stream().filter(edge1 -> probabilities.get(edge1)>p).findFirst().get();
        Edge edge=null;
            double probability=0;
        for (Edge e:getAvailableEdges()){
                probability=probability+probabilities.get(e);
            if (probability>=p){
                edge=e;
                break;
            }


        }

       return edge;
    }

    public abstract boolean isSolutionCompleted();

    public void constructSolution(Environment environment, double heuristicImportance, double pheromoneImportance) {

        visitedEdges=new ArrayList<>();
        visitedNodes = new ArrayList<>();
        visitedNodes.add(startNode);
        solution = new BasicGraphBuilder();


        solution.addNode(startNode);

        while (!isSolutionCompleted()) {

            Edge edge = chooseRandomEdge(environment,getAvailableEdges(), heuristicImportance, pheromoneImportance);
            this.addEdgeToSolution(edge);
        }


    }

    public abstract double calculateSolutionCost(GraphBuilder solution);


    public void depositPheromone(Environment environment){

        environment.getGraph().getEdges().stream().filter(o -> this.solution.getGraph().getEdges().contains(o)).forEach(o -> environment.getPheromones().put((Edge)o,environment.getPheromones().get(o)+1/calculateSolutionCost(this.getSolution())));


    }


}
