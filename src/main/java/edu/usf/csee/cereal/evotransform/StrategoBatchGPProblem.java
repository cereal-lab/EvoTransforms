package edu.usf.csee.cereal.evotransform;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.koza.KozaFitness;
import edu.usf.csee.cereal.evotransform.nodes.StrategoGPData;

public class StrategoBatchGPProblem extends StrategoProblemBase {
    private static final long serialVersionUID = 1L;

    public static class GenotypeWithPhenotype {
        public final GPIndividual ind;
        public final String strategoScript;

        public GenotypeWithPhenotype(GPIndividual ind, String strategoScript) {
            this.ind = ind;
            this.strategoScript = strategoScript;

        }
    }

    public List<GenotypeWithPhenotype> genomesAndPhenomes;

    @Override
    public void prepareToEvaluate(EvolutionState state, int threadnum) {
        genomesAndPhenomes = new ArrayList<>();
    }
    @Override
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) {
        if (!ind.evaluated) {
            StrategoGPData strategoScript = (StrategoGPData) input;
            strategoScript.init();
            strategoScript.append("!"); // evolution of transforms only
            GPIndividual gpInd = (GPIndividual) ind;
            gpInd.trees[0].child.eval(state, threadnum, strategoScript, stack, gpInd, this);
            // here we have our collected code
            //1: form stratego script 
            String modification = strategoScript.toString(); // should be list according to s.grammar from resources

            //NOTE: not thread safe!!!!!
            genomesAndPhenomes.add(new GenotypeWithPhenotype(gpInd, modification));
        }        
    }

    @Override
    public void finishEvaluating(EvolutionState state, int threadnum) {
        
        String prefix = String.format("g%d", state.generation);
        logger.info("-------------------------------------");
        //logger.info("Evaluating {}: {}", prefix, modification);
        //2: write it to str file inside spoofax package 
        //3: compile stratego code 
        //4: reload language impl
        //5: run built transformation on input program 
        //6: save transformed result to a file 
        Exception e = stratego.runModifications(genomesAndPhenomes
            .stream().map(gp -> gp.strategoScript)
            .collect(Collectors.toList()), prefix);
        if (e == null) {
            for (int i = 0; i < genomesAndPhenomes.size(); i++)
            {
                double fitness = 0; // 0 is the best - infinity is the worst
                String outFolder = Paths.get(stratego.getOutFolder(), prefix, String.valueOf(i)).toString();
                String javaFile = Paths.get(outFolder, "POC.java").toString();
                //7: compile result with java 
                CmdRunner.Output output = CmdRunner.javac(outFolder, javaFile);
                String outLog = Paths.get(outFolder, "javac.log").toString();
                try {
                    output.preserve(outLog);
                } catch (IOException e1) {
                    logger.warn("cannot write javac.log {}", e);                }
                int maxLinesOnInfiniteLoop = expectedOutput.size() * 2;
                if (output.code == 0) {
                    //8: run obtained java code and collect output 
                    output = CmdRunner.java(outFolder, "POC", maxLinesOnInfiniteLoop);        
                    outLog = Paths.get(outFolder, "java.log").toString();
                    try {
                        output.preserve(outLog);
                    } catch (IOException e1) {
                        logger.warn("cannot write java.log {}", e);
                    }                    
                    //code == -1 - infinite program - could be not so bad 
                    if (output.code <= 0) {
                        //8: compare expected output to what we obtained   
                        // penalties are 10 for count miss, 5 - for content miss, 1 for order miss
                        // count miss is assymetric - 10 is for case when # is not enough in obtained
                        // in case when it is more - one additional miss is (#exp - 2) * 10 / (2 * #exp) == 4.1 for POC (#exp == 11)  
                        // for content miss - two misses are equal to 1 count miss 
                        // for order miss - 5 misses i equivalent to 1 content miss                      
                        fitness = fitnessFromOutput(new LinkedList<String>(expectedOutput), output.out, maxLinesOnInfiniteLoop, 10.0, 5.0, 1.0); //TODO: penalties to config
                    } else {
                        logger.error("java failed: {}", e);
                        fitness = JAVA_EXECUTION_FAIL_FITNESS;    
                    }
                } else {
                    logger.error("javac failed: {}", e);
                    fitness = JAVA_COMPILATION_FAIL_FITNESS;
                }
                GPIndividual ind = genomesAndPhenomes.get(i).ind;
                // set the fitness and the evaluated flag
                KozaFitness f = (KozaFitness)(ind.fitness);
                f.setStandardizedFitness(state, fitness);
                f.hits = 0; // don’t bother using this
                ind.evaluated = true;
            }
        } else {
            logger.error("Unexpected strj failed: {}", e);
            state.output.fatal("Unexpected stratego failure " + e.getMessage());
            System.exit(1);
        }                      
    }

}