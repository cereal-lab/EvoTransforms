package edu.usf.csee.cereal.evotransform.nodes;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class MetaNode extends GPNode {


    private String name;

    public MetaNode(String name) {
        this.name = name;

    }
    private static final long serialVersionUID = 1L;

    @Override
    public void eval(EvolutionState arg0, int arg1, GPData data, ADFStack arg3, GPIndividual arg4, Problem arg5) {
        //check if we are unary or binary 
        StrategoGPData strategoScript = ((StrategoGPData)data);
        strategoScript.append(name);
    }

    @Override
    public String toString() {     
        return name;
    }

}