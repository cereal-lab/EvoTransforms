package edu.usf.csee.cereal.evotransform.nodes;

import java.util.HashMap;
import java.util.Map;

import ec.gp.GPData;

//wrapper around StringBuilder to collect Stratego script
public class StrategoGPData extends GPData {

    private static final long serialVersionUID = 1L;
    private StringBuilder builder; 
    private int indent; 
    // private Map<String, Integer> pools; 
    private int missingMetaVars;
    
    public StrategoGPData() {          
    }
    public void init() {
        builder = new StringBuilder();
        // pools = new HashMap<String, Integer>(); //dynamically allocated meta names for individual
        indent = 0;
    }
    //this could be useful to setup initial template of Stratego script
    // private StrategoGPData(StringBuilder builder) {
    //     this.builder = builder;
    // }
    public StrategoGPData append(String strategoPart) {
        builder.append(strategoPart);
        return this;
    }
    public StrategoGPData line() { 
        append(System.lineSeparator()).append(new String(new char[indent]).replace("\0", "  "));
        return this;
    }
    public StrategoGPData right() {
        indent = indent >= 10 ? indent : (indent+1);
        return this;
    }
    public StrategoGPData left() {
        indent = indent == 0 ? 0 : (indent-1);
        return this;
    }

    @Override
    public void copyTo(GPData data) {
        ((StrategoGPData)data).builder = builder;
    }
    @Override
    public String toString() {
        return builder.toString();
    }

}