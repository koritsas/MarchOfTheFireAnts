package edu.koritsas.aco.components;

/**
 * Created by ilias on 25/10/2016.
 */
public class MaxMinAntSystemAlgorithm extends AntSystemAlogrithm{
    private final double minPheromoneValue;
    private final double maxPheromoneValue;
    public MaxMinAntSystemAlgorithm(Environment environment, FireAntColony colony, int numberOfIterations, double initialPheromoneValue, double pheromoneEvaporationRate, double heuristicImportance, double pheromoneImportance,double minPheromoneValue,double maxPheromoneValue) {
        super(environment, colony, numberOfIterations, initialPheromoneValue, pheromoneEvaporationRate, heuristicImportance, pheromoneImportance);
        this.maxPheromoneValue=maxPheromoneValue;
        this.minPheromoneValue=minPheromoneValue;
    }

    @Override
    protected void pheromoneDeposition() {
        //List<FireAnt> ants= colony.getHive();
        //ants.stream().forEach(fireAnt -> fireAnt.depositPheromone(environment));
        colony.getIterationBestAnt().depositPheromone(environment);
        environment.getPheromones().entrySet().stream().forEach(edgeDoubleEntry -> {if (edgeDoubleEntry.getValue()>maxPheromoneValue){
            edgeDoubleEntry.setValue(maxPheromoneValue);
        }else if (edgeDoubleEntry.getValue()<minPheromoneValue){
            edgeDoubleEntry.setValue(minPheromoneValue);
        }});


    }
}
