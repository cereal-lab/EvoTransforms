package edu.usf.csee.cereal.evotransform.nodes.arithm;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.ERC;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Code;
import ec.util.DecodeReturn;
import edu.usf.csee.cereal.evotransform.nodes.StrategoGPData;

//integer number but could it be generalized to any arbitrary class? with generics
public class NumberNode extends ERC {
    private static final long serialVersionUID = 1L;
    int value = 0;

    @Override
    public String name() { return "number"; } //name from grammar

    @Override
    public String encode() {
        //return String.format("%s:%d", name(), value);
        return Code.encode(value);
    }

    @Override 
    public boolean decode(DecodeReturn dret) {
        Code.decode(dret);
        value = (int)dret.l;
        return dret.type == DecodeReturn.T_INT;
    }

    @Override
    public boolean nodeEquals(GPNode arg0) {
        if (arg0 instanceof NumberNode) {
            return ((NumberNode)arg0).value == this.value;
        }
        return false;
    }

    //TODO: to be more generic - we should use config - obtain config from arg0 and read the range from there - add custom parameters for range                    
    private static final int TOP_LIMIT = 10; 
    private static final int BOTTOM_LIMIT = -10;
    private static final int RANGE = 5;

    @Override
    public void resetNode(EvolutionState arg0, int arg1) {
        value = arg0.random[arg1].nextInt(TOP_LIMIT - BOTTOM_LIMIT + 1) + BOTTOM_LIMIT; //from -100 to 100 -             
    }

    @Override
    public void mutateERC(EvolutionState state, int thread) {
        value = value + (state.random[thread].nextInt(2*RANGE + 1) - RANGE);
        value = (value > TOP_LIMIT) ? TOP_LIMIT : ((value < BOTTOM_LIMIT) ? BOTTOM_LIMIT : value);
    }

    @Override
    public void eval(EvolutionState arg0, int arg1, GPData arg2, ADFStack arg3, GPIndividual arg4, Problem arg5) {
        StrategoGPData strategoScript = ((StrategoGPData)arg2);
        strategoScript.append(this.toString());        
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }


}