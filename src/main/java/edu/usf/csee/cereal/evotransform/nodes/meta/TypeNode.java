package edu.usf.csee.cereal.evotransform.nodes.meta;

import edu.usf.csee.cereal.evotransform.nodes.PoolNode;

public class TypeNode extends PoolNode<String> {
    private static final long serialVersionUID = 1L;

    public TypeNode() {
        super("~type");
    }
    @Override
    public String name() { return "metaType"; } //name from grammar

}