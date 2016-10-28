package edu.koritsas.aco.components;

import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.structure.Edge;

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
        logger.info("Initializing environment...");
       environment.initilizePheromones(initialPheromoneValue);

        logger.info("Building ant colony...");
        colony.buildColony(environment);

        logger.info("Starting iterations..");
        for (int i = 0; i < numberOfIterations; i++) {
            logger.info("Iteration: "+i);

            colony.buildSolutions(environment,heuristicImpotance,pheromoneImportance);

            updateGlobalBestAnt();

            logger.info("Best soution cost:"+getBestSolutionCost());
            performPheromoneEvaporation();

            pheromoneDeposition();

            System.gc();

        }

    }
    protected void pheromoneDeposition(){

       List<FireAnt> ants= colony.getHive();
        ants.stream().forEach(fireAnt -> fireAnt.depositPheromone(environment));

    }

    public void performPheromoneEvaporation(){

        environment.getGraph().getEdges().stream().forEach(edge ->environment.getPheromones().put((Edge) edge,environment.getPheromones().get(edge)*(1-pheromoneEvaporationRate)));


    }

    private void updateGlobalBestAnt() {
       /* if (globalBestAnt==null){
            globalBestAnt=colony.getIterationBestAnt();
        }else{
            double globalBestCost =globalBestAnt.calculateSolutionCost(globalBestAnt.getSolution());
            double iterationBestCost = colony.getIterationBestAnt().calculateSolutionCost(colony.getIterationBestAnt().getSolution());
           // if (colony.getIterationBestAnt().calculateSolutionCost((BasicGraphBuilder) colony.getIterationBestAnt().getSolution())<globalBestAnt.calculateSolutionCost((BasicGraphBuilder) globalBestAnt.getSolution())){
            if(globalBestCost<iterationBestCost){
                globalBestAnt=colony.getIterationBestAnt();
            }
        }*/
       if (bestSolution==null){
           bestSolution=colony.getIterationBestSolution();
           globalBestAnt=colony.getIterationBestAnt();
       }else{
           double iterCost =colony.getIterationBestAnt().calculateSolutionCost(colony.getIterationBestSolution());
           double globalCost =getGlobalBestAnt().calculateSolutionCost(bestSolution);
           if (iterCost<globalCost){
               globalBestAnt=colony.getIterationBestAnt();
               bestSolution=colony.getIterationBestSolution();
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
