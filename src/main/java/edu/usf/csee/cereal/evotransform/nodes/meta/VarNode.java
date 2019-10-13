package edu.usf.csee.cereal.evotransform.nodes.meta;

import edu.usf.csee.cereal.evotransform.nodes.PoolNode;

public class VarNode extends PoolNode<String> {
    private static final long serialVersionUID = 1L;
    public VarNode() {
        super("~x"); //add here other metas
    }
    @Override
    public String name() { return "metaVar"; } //name from grammar    
}