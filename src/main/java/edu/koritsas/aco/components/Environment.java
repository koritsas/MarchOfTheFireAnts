package edu.koritsas.aco.components;

import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.basic.BasicEdge;
import org.geotools.graph.structure.basic.BasicNode;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by ilias on 19/9/2016.
 */
public class Environment {


    private Graph graph;
    private HashMap<Edge,Double> pheromones;

    public Environment(Graph graph){
        this.graph =graph;
        pheromones=new HashMap<>(graph.getEdges().size());
    }
    public Graph getGraph() {
        return graph;
    }
    public void initialize(){

        Collection<Edge> edges =this.graph.getEdges();
        Collection<Node> nodes =this.getGraph().getNodes();

        edges.stream().filter(e->e.isVisited()).forEach(edge -> edge.setVisited(false));
        nodes.stream().filter(n->n.isVisited()).forEach(n -> n.setVisited(false));

    }

    public void initilizePheromones(double pheromoneValue){

        Collection<Edge> edges =this.graph.getEdges();
        edges.stream().forEach(edge -> pheromones.put(edge,pheromoneValue));
    }
    public HashMap<Edge,Double> getPheromones(){
        return pheromones;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
