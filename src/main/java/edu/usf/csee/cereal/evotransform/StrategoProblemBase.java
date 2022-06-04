package edu.usf.csee.cereal.evotransform;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.io.Files;
import com.google.common.io.Resources;

import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import ec.EvolutionState;
import ec.gp.GPProblem;
import ec.util.Parameter;
import edu.usf.csee.cereal.evotransform.StrategoProxy.Modification;
import edu.usf.csee.cereal.evotransform.nodes.StrategoGPData;

public abstract class StrategoProblemBase extends GPProblem {
    private static final long serialVersionUID = 1L;

    StrategoProxy stratego;
    ILogger logger = LoggerUtils.logger(StrategoGPProblem.class);
    List<String> expectedOutput;
    String replaceWhat;
    // possible fintesses:
    // 1. output comparison (or tests) - this is current
    // 2. source code comparison (tree or string)
    // 3. metrics of code
    // 4. combined approach - check markers in source code + run it
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        // verify that our GPData is of the right class (or subclasses from it)
        if (!(input instanceof StrategoGPData))
            state.output.fatal("GPData class must subclass from " + StrategoGPData.class, base.push(P_DATA), null);

        try {       
            Parameter strategoParam = base.push("stratego");
            Parameter strategoLangParam = strategoParam.push("language");
            String strategoLanguageDir = state.parameters.getString(strategoLangParam, null);
            Parameter replaceWhatParam = base.push("replaceWhat");
            replaceWhat = state.parameters.getString(replaceWhatParam, null);
            Parameter strategoFileTimeplateParam = strategoParam.push("fileTemplate");
            String strategoFileTimeplate = state.parameters.getString(strategoFileTimeplateParam, null);
            Parameter strategoStrategyTemplateParam = strategoParam.push("strategyTemplate");
            String strategyTemplate = state.parameters.getString(strategoStrategyTemplateParam, null);
            Parameter outDirParam = base.push("out");
            String outDir = state.parameters.getString(outDirParam, null);

            Parameter preModificationParam = base.push("preModification");
            Parameter preModificationReplaceWhatParam = preModificationParam.push("replaceWhat");
            String preModificationReplaceWhat = state.parameters.getString(preModificationReplaceWhatParam, null);
            Modification preModification = null;
            if (preModificationReplaceWhat != null) {
                Parameter preModificationModificationParam = preModificationParam.push("modification");
                String preModificationModificationResource = state.parameters.getString(preModificationModificationParam, null);
                String preModificationModification = Resources.toString(Resources.getResource(preModificationModificationResource), Charset.forName("UTF-8"));
                preModification = new Modification(-1, preModificationReplaceWhat, preModificationModification);
            }
            stratego = StrategoProxy.create(strategoLanguageDir, outDir, strategoFileTimeplate, strategyTemplate, preModification);// can fail
            Parameter inDirParam = base.push("in");
            String inDir = state.parameters.getString(inDirParam, null);
            Parameter inDirFileParam = inDirParam.push("file"); //TODO: allow several input files and agregate fitness
            String inFile = state.parameters.getString(inDirFileParam, null);
            //String inFolder = Paths.get("data", "in").toString();
            //String inFolderPOC = Paths.get(inFolder, "POC.java").toString();
            CmdRunner.Output output = CmdRunner.javac(inDir, inFile);
            if (output.code != 0) {
                logger.error("Provided input Java file is incorrect: {}", 
                    output.err.stream().collect(Collectors.joining(System.lineSeparator())));
                state.output.fatal("Provided input Java file is incorrect");
                System.exit(1);
            }
            String className = Files.getNameWithoutExtension(inFile);
            output = CmdRunner.java(inDir, className, 0);       
            if (output.code != 0)  {
                logger.error("Provided input file has runtime error: {}", 
                    output.err.stream().collect(Collectors.joining(System.lineSeparator())));
                state.output.fatal("Provided input file has runtime error");
                System.exit(1);
            }
            expectedOutput = output.out;
            String inLog = Paths.get(inDir, "java.log").toString();
            output.preserve(inLog);
        } catch (Exception e) {
            logger.error("Creation of StrategoProxy failed: {}", e);
            state.output.fatal("Cannot create StrategoProxy: " + e.getMessage(), base.push(P_DATA), null);
            System.exit(1);
        }
    }

    final static double RUN_MODIFICATION_FAIL_FITNESS = 100000;
    final static double JAVA_COMPILATION_FAIL_FITNESS = 10000;
    final static double JAVA_EXECUTION_FAIL_FITNESS = 1000;

    //1. first we add to misses all lines that are not in the expected
    //2. then we assign to each of rest of lines indexes corresponding to array expected 
    //3. lastly, we use minInsertionStepToSortArray to sort array of indexes and add value to misses
    public static double fitnessFromOutput(List<String> expected, List<String> obtained, 
            int maxLinesOnInfiniteLoop, double countMissPenalty,
            double missPenalty, double orderPenalty) {
        //primitive - fitness is number of missed matches 

        //analysis of count of lines in output
        //having more is less penalty - encourages loops  
        double fitness = 0;                  
        double rightMissPenalty = (expected.size() - 2) * countMissPenalty / maxLinesOnInfiniteLoop;
        if (obtained.size() <= expected.size()) {
            fitness = fitness + (expected.size() - obtained.size()) * countMissPenalty;
        } else {
            fitness = fitness + (obtained.size() - expected.size()) * rightMissPenalty;
        }
        int[] presentIndexes = new int[obtained.size()];
        for (int j = 0; j < obtained.size(); j++) {
            presentIndexes[j] = -1;
        }
        for (int i = 0; i < expected.size(); i++) {
            int foundIndex = -1;
            for (int j = 0; j < obtained.size(); j++) {
                if (expected.get(i).equals(obtained.get(j)) && (presentIndexes[j] == -1)) 
                {
                    foundIndex = j;                    
                    presentIndexes[j] = i;
                    break;
                }
            }
            if (foundIndex == -1) {
                fitness = fitness + missPenalty;
            }
        }
        int[] presentIndexesSeq = Arrays.stream(presentIndexes).filter(i -> i != -1).toArray();
        int orderSteps = presentIndexesSeq.length > 1 ? minInsertionStepToSortArray(presentIndexesSeq, presentIndexesSeq.length) : 0;
        fitness = fitness + orderSteps * orderPenalty;
        return fitness;
    }

    public static class FitnessTest {
        public static void main(String[] args) {
            List<String> expected = new ArrayList<>();
            expected.add("1");
            expected.add("2");
            expected.add("3");
            expected.add("4");     
            List<String> obtained = new ArrayList<>(expected);     
            System.out.println("fitness: " + fitnessFromOutput(expected, obtained, expected.size()*2, 10, 5, 1));
        }
    }

    //taken from https://www.geeksforgeeks.org/minimum-insertions-sort-array/
    //  method returns min steps of insertion we need 
    // to perform to sort array 'arr' 
    static private int minInsertionStepToSortArray(int arr[], int N) 
    { 
        // lis[i] is going to store length of lis 
        // that ends with i. 
        int[] lis = new int[N]; 
       
        /* Initialize lis values for all indexes */
        for (int i = 0; i < N; i++) 
            lis[i] = 1; 
       
        /* Compute optimized lis values in bottom up manner */
        for (int i = 1; i < N; i++) 
            for (int j = 0; j < i; j++) 
                if (arr[i] >= arr[j] && lis[i] < lis[j] + 1) 
                    lis[i] = lis[j] + 1; 
       
        /* The overall LIS must end with of the array 
           elements. Pick maximum of all lis values */
        int max = 0; 
        for (int i = 0; i < N; i++) 
            if (max < lis[i]) 
                max = lis[i]; 
       
        // return size of array minus length of LIS 
        // as final result 
        return (N - max); 
    } 
    
}