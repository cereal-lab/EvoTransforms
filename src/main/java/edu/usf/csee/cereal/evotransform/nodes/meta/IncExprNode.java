package edu.usf.csee.cereal.evotransform.nodes.meta;

import edu.usf.csee.cereal.evotransform.nodes.PoolNode;

public class IncExprNode extends PoolNode<String> {
    private static final long serialVersionUID = 1L;

    public IncExprNode() {
        super("~inc"); //add expr here
    }
    @Override
    public String name() { return "incExpr"; } //name from grammar

}