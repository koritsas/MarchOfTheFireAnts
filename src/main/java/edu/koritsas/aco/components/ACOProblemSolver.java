package edu.koritsas.aco.components;

import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;

import java.util.logging.Logger;

/**
 * Created by ilias on 19/9/2016.
 */
public class ACOProblemSolver {

    private Environment environment;
    private FireAntColony colony;
    private final int numberOfIterations;
    private final double heuristicImpotance;
    private final double pheromoneImportance;
    private FireAnt globalBestAnt;
    private final double pheromoneEvaporationRate;
    private final double initialPheromoneValue;
    private Logger logger = Logger.getLogger(ACOProblemSolver.class.getName());
    public ACOProblemSolver(Environment environment, FireAntColony colony,int numberOfIterations,double initialPheromoneValue,double pheromoneEvaporationRate,double heuristicImportance,double pheromoneImportance){
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

            performPheromoneEvaporation();

            pheromoneDeposition();


        }

    }
    protected void pheromoneDeposition(){

        colony.getIterationBestAnt().depositPheromone(environment);
    }

    public void performPheromoneEvaporation(){

        environment.getGraph().getEdges().stream().forEach(edge ->environment.getPheromones().put((Edge) edge,environment.getPheromones().get(edge)*(1-pheromoneEvaporationRate)));


    }

    private void updateGlobalBestAnt() {
        if (globalBestAnt==null){
            globalBestAnt=colony.getIterationBestAnt();
        }else{
            if (colony.getIterationBestAnt().calculateSolutionCost((BasicGraphBuilder) colony.getIterationBestAnt().getSolution())<globalBestAnt.calculateSolutionCost((BasicGraphBuilder) globalBestAnt.getSolution())){
                globalBestAnt=colony.getIterationBestAnt();
            }
        }
    }

    public FireAnt getGlobalBestAnt(){

        return globalBestAnt;
    }

    public GraphBuilder getBestSolution(){

        return globalBestAnt.getSolution();
    }
    public double getBestSolutionCost(){
       return globalBestAnt.calculateSolutionCost((BasicGraphBuilder) getBestSolution());
    }

}
