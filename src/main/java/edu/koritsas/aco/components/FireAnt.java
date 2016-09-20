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
    private Environment environment;
    private GraphBuilder solution;
    private Node startNode;


    public FireAnt(Node startNode){
        this.startNode=startNode;
        this.startNode.setVisited(true);

    }
    public void setEnvironment(Environment environment){
        this.environment = environment;
    }

    public GraphBuilder getSolution(){

        return solution;
    }


    /**
     *
     * @return a list of components that are available for solution construction at each step
     */
    private List<Edge> getAvailableEdges(){
        List<Edge> neighbourhood=getNeighbourhood(this.environment);
        List<Edge> availableEdges=  neighbourhood.stream().filter(edge -> !edge.isVisited()).distinct().filter(edge -> !violatesConstraints(edge)).collect(Collectors.toList());

        if (availableEdges.isEmpty()){
            throw new ConfigurationException("There are no available edges for selection. Reconfigure neighbourhood");

        }

        return availableEdges;
    }
    /**
     *
     * @param environment
     * @return a list of components that are not visited
     */
    public abstract List<Edge> getNeighbourhood(Environment environment);

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

    private double calculateDenominator(double heuristicImportance, double pheromoneImportance){
        double denominator =getAvailableEdges().stream().collect(Collectors.summingDouble(new ToDoubleFunction<Edge>() {
            @Override
            public double applyAsDouble(Edge edge) {
                return calculateNumerator(edge,heuristicImportance,pheromoneImportance);
            }
        }));

        return denominator;
    }
    private double calculateNumerator(Edge edge,double heuristicImportance, double pheromoneImportance){
        double numerator =getAvailableEdges().stream().collect(Collectors.summingDouble(new ToDoubleFunction<Edge>() {
            @Override
            public double applyAsDouble(Edge e) {
               double t =environment.getPheromones().get(e);
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
    public HashMap<Edge,Double> calculateProbabilities(List<Edge> availableEdges, double heuristicImportance, double pheromoneImportance){

        HashMap<Edge,Double> probabilities = new HashMap<>(availableEdges.size());
        for (Edge e:availableEdges){
            probabilities.putIfAbsent(e,calculateNumerator(e,heuristicImportance,pheromoneImportance)/calculateDenominator(heuristicImportance,pheromoneImportance));
        }


        return probabilities;
    }

    /**
     *
     *
     * @param edge
     */
    private void addEdgeToSolution(Edge edge){
        this.solution.addEdge(edge);
        edge.setVisited(true);
        edge.getNodeB().setVisited(true);
        edge.getNodeA().setVisited(true);
    }

    /**
     *
     * @param availableEdges
     * @param heuristicImportance
     * @param pheromoneImportance
     * @return chooses an edge from available edges whith roulette wheel selection
     */
    protected Edge chooseRandomEdge(List<Edge> availableEdges,double heuristicImportance,double pheromoneImportance){

        HashMap<Edge,Double> probabilities =calculateProbabilities(availableEdges,heuristicImportance,pheromoneImportance);

        RandomGenerator randomGenerator = new Well512a();
        double p=randomGenerator.nextDouble();

         //Edge edge=  availableEdges.stream().filter(edge1 -> probabilities.get(edge1)>p).findFirst().get();
        Edge edge=null;
            double probability=0;
        for (Edge e:getAvailableEdges()){
                probability=probability+probabilities.get(e);
            if (probability>p){
                edge=e;
                break;
            }


        }

       return edge;
    }

    public abstract boolean isSolutionCompleted();

    public void constructSolution(double heuristicImportance, double pheromoneImportance) {

        solution = new BasicGraphBuilder();


        solution.addNode(startNode);

        while (!isSolutionCompleted()) {

            Edge edge = chooseRandomEdge(getAvailableEdges(), heuristicImportance, pheromoneImportance);
            this.addEdgeToSolution(edge);
        }


    }

    public abstract double calculateSolutionCost(GraphBuilder solution);


    public void depositPheromone(Environment environment){

        environment.getGraph().getEdges().stream().filter(o -> this.solution.getGraph().getEdges().contains(o)).forEach(o -> environment.getPheromones().put((Edge)o,1/calculateSolutionCost(this.getSolution())));


    }

    public Environment getEnvironment() {
        return environment;
    }
}
