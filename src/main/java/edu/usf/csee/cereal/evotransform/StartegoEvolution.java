package edu.usf.csee.cereal.evotransform;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.Output;
import ec.util.ParameterDatabase;

public class StartegoEvolution {

    public static void main(String[] args) throws URISyntaxException {
        URL ecjParamsUrl = ClassLoader.getSystemResource("ec.params");
        File ecjConfigFile = new File(ecjParamsUrl.toURI());
        ParameterDatabase params = null;		
        try {
            params = new ParameterDatabase(ecjConfigFile);
            //String r = params.getString(new Parameter("test"), null);
            //System.out.format("Found %s%n", r);
            
                        //new String[] { "-file", ecjConfigFile.getCanonicalPath() });
            // params.set(new Parameter("pop.subpop.0.species.min-gene"), "0");
            // params.set(new Parameter("pop.subpop.0.species.max-gene"), String.valueOf(broker.getLib().getTransformCount() - 1));
            // params.set(new Parameter("pop.subpop.0.species.min-gene.0"), "0");
            // params.set(new Parameter("pop.subpop.0.species.max-gene.0"), String.valueOf(broker.getLib().getProgramCount() - 1));
        } catch (IOException e) {
            System.err.format("[ECJ.runFresh] Error starting ECJ (check param file): %s", e.getMessage());
            System.exit(1);
        }
        Output out = Evolve.buildOutput();
        EvolutionState evolState = Evolve.initialize(params, 0, out);
        evolState.run(EvolutionState.C_STARTED_FRESH);
        System.out.println("End of Stratego evolution");
    }
}