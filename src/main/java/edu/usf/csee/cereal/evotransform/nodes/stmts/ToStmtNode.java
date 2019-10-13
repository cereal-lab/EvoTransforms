package edu.usf.csee.cereal.evotransform.nodes.stmts;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import edu.usf.csee.cereal.evotransform.nodes.StrategoGPData;

public class ToStmtNode extends GPNode {
    private static final long serialVersionUID = 1L;
    @Override
    public void eval(EvolutionState arg0, int arg1, GPData data, ADFStack arg3, GPIndividual arg4, Problem arg5) {
        StrategoGPData strategoScript = ((StrategoGPData)data);
        this.children[0].eval(arg0, arg1, data, arg3, arg4, arg5);
        strategoScript.append(";");
    }

    @Override
    public String toString() {
        return String.format("%s;", this.children[0].toString());
    }
    @Override
    public String name() { return "toStmt"; } //name from grammar    

}