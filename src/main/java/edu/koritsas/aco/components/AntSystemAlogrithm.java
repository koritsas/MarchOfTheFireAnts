package edu.koritsas.aco.components;

import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.structure.Edge;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by ilias on 19/9/2016.
 */
public class AntSystemAlogrithm {

    protected Environment environment;
    protected FireAntColony colony;
    private final int numberOfIterations;
    private final double heuristicImpotance;
    private final double pheromoneImportance;
    private FireAnt globalBestAnt;
    private final double pheromoneEvaporationRate;
    private final double initialPheromoneValue;
    private  GraphBuilder bestSolution;
    private int currentIteration;
    private int numberOfIterationBest;

    private XYSeries costSeries = new XYSeries(0);





    private Logger logger = Logger.getLogger(AntSystemAlogrithm.class.getName());
    public AntSystemAlogrithm(Environment environment, FireAntColony colony, int numberOfIterations, double initialPheromoneValue, double pheromoneEvaporationRate, double heuristicImportance, double pheromoneImportance){
        this.environment=environment;
        this.colony=colony;
        this.numberOfIterations=numberOfIterations;
        this.heuristicImpotance=heuristicImportance;
        this.pheromoneImportance=pheromoneImportance;
        this.pheromoneEvaporationRate=pheromoneEvaporationRate;
        this.initialPheromoneValue=initialPheromoneValue;
    }
    public void execute(){
        double startingTime=System.currentTimeMillis();

        logger.info("Initializing environment...");
       environment.initilizePheromones(initialPheromoneValue);

        logger.info("Building ant colony...");
        colony.buildColony(environment);

        logger.info("Starting iterations..");
        for (int i = 0; i < numberOfIterations; i++) {
            logger.info("Iteration: "+i);
            currentIteration =i;

            colony.buildSolutions(environment,heuristicImpotance,pheromoneImportance);



            updateGlobalBestAnt();

            costSeries.add(i,getBestSolutionCost());

            logger.info("Best soution cost:"+getBestSolutionCost());
            performPheromoneEvaporation();

            pheromoneDeposition();

            System.gc();

        }
        double endingTime=System.currentTimeMillis();

        logger.info("Execution Time: "+ ((endingTime-startingTime))/1000);
    }
    protected void pheromoneDeposition(){

       List<FireAnt> ants= colony.getHive();
        ants.stream().forEach(fireAnt -> fireAnt.depositPheromone(environment));

    }
    public void createCostGraph(){
        final XYSeriesCollection dataset = new XYSeriesCollection( );
        dataset.addSeries(costSeries);
        JFreeChart flowChart = ChartFactory.createXYLineChart("Cost", "Iteration", "Cost", dataset, PlotOrientation.VERTICAL,true,true,true);
        ChartPanel chartPanel = new ChartPanel( flowChart);
        chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
        ApplicationFrame frame = new ApplicationFrame(this.getClass().getSimpleName());

        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);

    }

    public void performPheromoneEvaporation(){

        environment.getGraph().getEdges().stream().forEach(edge ->environment.getPheromones().put((Edge) edge,environment.getPheromones().get(edge)*(1-pheromoneEvaporationRate)));


    }


    public int getNumberOfIterationBest() {
        return numberOfIterationBest;
    }

    private void updateGlobalBestAnt() {

       if (bestSolution==null){
           bestSolution=colony.getIterationBestSolution();
           globalBestAnt=colony.getIterationBestAnt();
       }else{
           double iterCost =colony.getIterationBestAnt().calculateSolutionCost(colony.getIterationBestSolution());
           double globalCost =getGlobalBestAnt().calculateSolutionCost(bestSolution);
           if (iterCost<globalCost){
               globalBestAnt=colony.getIterationBestAnt();
               bestSolution=colony.getIterationBestSolution();
               numberOfIterationBest= currentIteration;
           }
       }

    }

    public FireAnt getGlobalBestAnt(){

        return globalBestAnt;
    }

    public GraphBuilder getBestSolution(){

        return bestSolution;
    }
    public double getBestSolutionCost(){
       return getGlobalBestAnt().calculateSolutionCost(bestSolution);
    }

}
