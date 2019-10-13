package edu.usf.csee.cereal.evotransform;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.koza.KozaFitness;
import edu.usf.csee.cereal.evotransform.nodes.StrategoGPData;

public class StrategoGPProblem extends StrategoProblemBase {
    private static final long serialVersionUID = 1L;

    private int index;

    public synchronized int newIndex() {
        return index++;
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

            double fitness = 0; // 0 is the best - infinity is the worst
            String prefix = String.format("g%d-i%d", state.generation, newIndex());
            logger.info("-------------------------------------");
            logger.info("Evaluating {}: {}", prefix, modification);
            //2: write it to str file inside spoofax package 
            //3: compile stratego code 
            //4: reload language impl
            //5: run built transformation on input program 
            //6: save transformed result to a file 
            Exception e = stratego.runModification(modification, prefix);
            if (e == null) {
                String outFolder = Paths.get(stratego.getOutFolder(), prefix).toString();
                String javaFile = Paths.get(outFolder, "POC.java").toString();
                //7: compile result with java 
                CmdRunner.Output output = CmdRunner.javac(outFolder, javaFile);
                String outLog = Paths.get(outFolder, "javac.log").toString();
                try {
                    output.preserve(outLog);
                } catch (IOException e1) {
                    logger.warn("cannot write javac.log {}", e); 
                }
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
                    if (output.code == 0) {
                        //8: compare expected output to what we obtained   
                        fitness = fitnessFromOutput(new LinkedList<String>(expectedOutput), output.out, maxLinesOnInfiniteLoop, 3.0, 2.0, 1.0); //TODO: penalties to config
                    } else {
                        logger.error("java failed: {}", e);
                        fitness = JAVA_EXECUTION_FAIL_FITNESS;    
                    }
                } else {
                    logger.error("javac failed: {}", e);
                    fitness = JAVA_COMPILATION_FAIL_FITNESS;
                }

            } else {
                logger.error("strj failed: {}", e);
                fitness = RUN_MODIFICATION_FAIL_FITNESS;
            }                      
            // set the fitness and the evaluated flag
            KozaFitness f = (KozaFitness)(ind.fitness);
            f.setStandardizedFitness(state, fitness);
            f.hits = 0; // don’t bother using this
            ind.evaluated = true;

        }        
    }

}