package edu.usf.csee.cereal.evotransform.nodes.meta;

import edu.usf.csee.cereal.evotransform.nodes.PoolNode;

public class InitExprNode extends PoolNode<String> {
    private static final long serialVersionUID = 1L;

    public InitExprNode() {
        super("~initExpr"); //add expr here
    }
    @Override
    public String name() { return "initExpr"; } //name from grammar

}