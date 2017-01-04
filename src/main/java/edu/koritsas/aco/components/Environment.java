package edu.koritsas.aco.components;

import edu.koritsas.aco.components.edu.koritsas.aco.exceptions.ConfigurationException;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ilias on 19/9/2016.
 */
public class Environment {


    private Graph graph;

    public void setPheromones(HashMap<Edge, Double> pheromones) {
        this.pheromones = pheromones;
    }
    public void setPheromonesToEdges(List<Edge> edges,double pheromoneValue){
        for (Edge e:edges){
                pheromones.replace(e,pheromoneValue);
        }


    }

    private HashMap<Edge, Double> pheromones;

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

    public void createPheromonesBarChart(){

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<Edge> edges = new ArrayList<>(this.getGraph().getEdges());
        for (Edge e:edges){
            String id=Integer.toString(e.getID());
            dataset.addValue((double)pheromones.get(e),id,"Pheromone Value");
        }

        JFreeChart barChart= ChartFactory.createBarChart("Edge pheromone quantity","Edge","Pheromones",dataset, PlotOrientation.VERTICAL,true,false,false);

        ChartPanel panel = new ChartPanel(barChart);
        panel.setChart(barChart);
        ApplicationFrame frame = new ApplicationFrame("Pheromone Bar Chart");
        frame.setContentPane(panel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }
}
