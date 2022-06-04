package edu.usf.csee.cereal.evotransform.nodes.stmts;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import edu.usf.csee.cereal.evotransform.nodes.StrategoGPData;

public class ForNode extends GPNode {

    private static final long serialVersionUID = 1L;

    @Override
    public void eval(EvolutionState arg0, int arg1, GPData data, ADFStack arg3, GPIndividual arg4, Problem arg5) {
        StrategoGPData strategoScript = ((StrategoGPData)data);
        strategoScript.append("for(");
        this.children[0].eval(arg0, arg1, data, arg3, arg4, arg5);
        strategoScript.append(";");
        this.children[1].eval(arg0, arg1, data, arg3, arg4, arg5);
        strategoScript.append(";");
        this.children[2].eval(arg0, arg1, data, arg3, arg4, arg5);
        strategoScript.append("){").line().right();
        this.children[3].eval(arg0, arg1, data, arg3, arg4, arg5);
        strategoScript.line().left().append("}");
    }

    @Override
    public String toString() {
        return String.format("for(%s;%s;%s){ %n%s%n }", 
            this.children[0].toString(), 
            this.children[1].toString(), 
            this.children[2].toString(), 
            this.children[3].toString());
    }

    @Override
    public String name() { return "for"; } //name from grammar      
    
}
