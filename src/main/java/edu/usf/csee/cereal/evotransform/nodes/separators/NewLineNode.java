package edu.usf.csee.cereal.evotransform.nodes.separators;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import edu.usf.csee.cereal.evotransform.nodes.StrategoGPData;

public class NewLineNode extends GPNode {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void eval(EvolutionState arg0, int arg1, GPData arg2, ADFStack arg3, GPIndividual arg4, Problem arg5) {
        StrategoGPData data = (StrategoGPData)arg2; 
        data.line();
    }

    @Override
    public String toString() {
        return System.lineSeparator();
    }
    
    @Override
    public String name() { return "nl"; } //name from grammar      
}
