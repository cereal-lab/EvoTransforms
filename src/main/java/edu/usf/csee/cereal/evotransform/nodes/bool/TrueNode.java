package edu.usf.csee.cereal.evotransform.nodes.bool;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import edu.usf.csee.cereal.evotransform.nodes.StrategoGPData;

public class TrueNode extends GPNode {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void eval(EvolutionState arg0, int arg1, GPData data, ADFStack arg3, GPIndividual arg4, Problem arg5) {
        ((StrategoGPData)data).append(this.toString());
    }

    @Override
    public String toString() {
        return "true";
    }

    @Override
    public String name() { return "true"; } //name from grammar


}