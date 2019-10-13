package edu.usf.csee.cereal.evotransform.nodes;

import java.util.Arrays;
import java.util.stream.Collectors;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class SeqNode extends GPNode {
    private static final long serialVersionUID = 1L;

    @Override
    public void eval(EvolutionState arg0, int arg1, GPData data, ADFStack arg3, GPIndividual arg4, Problem arg5) {
        //check if we are unary or binary 
        StrategoGPData strategoScript = ((StrategoGPData)data);
        if (this.children.length > 0)
        {
            for (int i = 0; i < this.children.length - 1; i++) {
                this.children[i].eval(arg0, arg1, data, arg3, arg4, arg5);
                strategoScript.line();
            }
            this.children[this.children.length - 1].eval(arg0, arg1, data, arg3, arg4, arg5);
        }
    }

    @Override
    public String toString() {     
        return Arrays.stream(this.children).map(child -> child.toString())
                    .collect(Collectors.joining(System.lineSeparator()));
    }

}