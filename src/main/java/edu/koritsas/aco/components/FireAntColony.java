package edu.koritsas.aco.components;

import com.rits.cloning.Cloner;
import edu.koritsas.aco.components.FireAnt;
import org.geotools.graph.build.GraphBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by ilias on 19/9/2016.
 */
public abstract class FireAntColony<T extends FireAnt> {
    private final int numberOfAnts;
    private List<FireAnt> hive;
    private GraphBuilder bestSolution;
    private FireAnt iterationBestAnt;
    private FireAnt globalBestAnt;
    public FireAntColony(int numberOfAnts){
        this.numberOfAnts=numberOfAnts;
    }
    public FireAnt getIterationBestAnt(){
        return iterationBestAnt;
    }


    public List<FireAnt> getHive() {
        return hive;
    }


    protected void buildColony(Environment environment){


        hive = Stream.generate(this::createFireAnt).limit(numberOfAnts).collect(Collectors.toList());

    }



    protected void buildSolutions(Environment environment,double heuristicImportance,double pheromoneImportance){


        hive.parallelStream().forEach(fireAnt -> fireAnt.constructSolution(environment,heuristicImportance,pheromoneImportance));


        iterationBestAnt = hive.parallelStream().min(new Comparator<FireAnt>() {
           @Override
           public int compare(FireAnt o1, FireAnt o2) {
               if (o1.calculateSolutionCost(o1.getSolution())<o2.calculateSolutionCost(o2.getSolution())){
                   return -1;
               }else{
                   return 1;
               }


           }
        }).get();
        }

    public abstract FireAnt createFireAnt();

}
