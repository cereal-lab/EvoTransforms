package edu.usf.csee.cereal.evotransform.nodes.java;

import edu.usf.csee.cereal.evotransform.nodes.PoolNode;

public class TypeNode extends PoolNode<String> {
    private static final long serialVersionUID = 1L;

    public TypeNode() {
        super("int", "double"); //TODO, add here type
    }
    @Override
    public String name() { return "javaType"; } //name from grammar

}