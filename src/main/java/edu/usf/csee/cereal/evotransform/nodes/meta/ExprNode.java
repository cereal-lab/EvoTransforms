package edu.usf.csee.cereal.evotransform.nodes.meta;

import edu.usf.csee.cereal.evotransform.nodes.PoolNode;

public class ExprNode extends PoolNode<String> {
    private static final long serialVersionUID = 1L;

    public ExprNode() {
        super("~inc", "~initExpr"); //add expr here
    }
    @Override
    public String name() { return "metaExpr"; } //name from grammar

}