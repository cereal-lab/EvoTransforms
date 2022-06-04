package edu.usf.csee.cereal.evotransform.nodes;
//This node is an ERC and still under experimentation 

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.ERC;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Code;
import ec.util.DecodeReturn;

public class BindNode<T> extends ERC {    
    private static final long serialVersionUID = 1L;
    static int poolIndex = 0; //dyn allocation 
    static String prefix; 
    int currentIndex = 0;
    public BindNode() {        
    }

    @Override
    public String encode() {
        return Code.encode(currentIndex);
    }

    @Override
    public boolean decode(DecodeReturn dret) {
        Code.decode(dret);
        currentIndex = (int)dret.l;
        return dret.type == DecodeReturn.T_INT;
    }

    @Override
    public boolean nodeEquals(GPNode arg0) {
        if (arg0.getClass().equals(this.getClass())) {
            return ((BindNode<T>) arg0).currentIndex == this.currentIndex;
        }
        return false;
    }

    @Override
    public void resetNode(EvolutionState arg0, int arg1) {        
        currentIndex = poolIndex++; //TODO: check 
    }

    @Override
    public void eval(EvolutionState arg0, int arg1, GPData arg2, ADFStack arg3, GPIndividual arg4, Problem arg5) {
        StrategoGPData strategoScript = ((StrategoGPData)arg2);
        strategoScript.append(this.toString());                
    }

    @Override
    public String toString() {
        return String.format("~%s%d", prefix, currentIndex);
    }

}