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

public class PoolNode<T> extends ERC {    
    private static final long serialVersionUID = 1L;
    // TODO: here we should create a pool of names from which we select the name
    int poolIndex = 0; //index from pool 
    T[] pool;
    public PoolNode(T... pool) {
        this.pool = pool;
    }
    // String[] metaNames =
    //     new String[] {
    //         "~mn1", "~mn2", "~mn3", //meta name vars
    //         "~mnl1*", "~mnl2*", "~mnl3*" //meta name list
    //         //TODO: think to split or not
    //     };

    // @Override
    // public String name() {
    //     return "metaName";
    // } // name from grammar

    @Override
    public String encode() {
        return Code.encode(poolIndex);
    }

    @Override
    public boolean decode(DecodeReturn dret) {
        Code.decode(dret);
        poolIndex = (int)dret.l;
        return dret.type == DecodeReturn.T_INT;
    }

    @Override
    public boolean nodeEquals(GPNode arg0) {
        if (arg0.getClass().equals(this.getClass())) {
            return ((PoolNode<T>) arg0).poolIndex == this.poolIndex;
        }
        return false;
    }

    @Override
    public void resetNode(EvolutionState arg0, int arg1) {
        poolIndex = arg0.random[arg1].nextInt(pool.length);
    }

    @Override
    public void eval(EvolutionState arg0, int arg1, GPData arg2, ADFStack arg3, GPIndividual arg4, Problem arg5) {
        StrategoGPData strategoScript = ((StrategoGPData)arg2);
        strategoScript.append(this.toString());                
    }

    @Override
    public String toString() {
        return pool[poolIndex].toString();
    }

}