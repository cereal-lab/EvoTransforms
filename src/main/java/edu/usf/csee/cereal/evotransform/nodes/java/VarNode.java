package edu.usf.csee.cereal.evotransform.nodes.java;

import edu.usf.csee.cereal.evotransform.nodes.PoolNode;

//declared vars in context
public class VarNode extends PoolNode<String> {
    private static final long serialVersionUID = 1L;
    public VarNode() {
        super("sum"); //add here other metas
    }
    @Override
    public String name() { return "contextVar"; } //name from grammar    
}