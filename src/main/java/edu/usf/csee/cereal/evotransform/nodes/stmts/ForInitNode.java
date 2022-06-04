package edu.usf.csee.cereal.evotransform.nodes.stmts;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import edu.usf.csee.cereal.evotransform.nodes.StrategoGPData;

public class ForInitNode extends GPNode {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void eval(EvolutionState arg0, int arg1, GPData arg2, ADFStack arg3, GPIndividual arg4, Problem arg5) {
        StrategoGPData data = (StrategoGPData)arg2; 
        this.children[0].eval(arg0, arg1, arg2, arg3, arg4, arg5);
        data.append(" ");
        this.children[1].eval(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    @Override
    public String toString() {
        return String.format("%s %s", this.children[0].toString(), this.children[1].toString());
    }

    @Override
    public String name() { return "forInit"; } //name from grammar      
    
}
