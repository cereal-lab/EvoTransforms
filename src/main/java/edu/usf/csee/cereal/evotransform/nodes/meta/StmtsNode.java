package edu.usf.csee.cereal.evotransform.nodes.meta;

import edu.usf.csee.cereal.evotransform.nodes.PoolNode;

public class StmtsNode extends PoolNode<String> {
    private static final long serialVersionUID = 1L;
    public StmtsNode() {
        super("~stms*"); //add here other captured statements
    }
    @Override
    public String name() { return "metaStmts"; } //name from grammar    
}