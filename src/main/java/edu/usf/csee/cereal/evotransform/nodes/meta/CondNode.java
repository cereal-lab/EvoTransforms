package edu.usf.csee.cereal.evotransform.nodes.meta;

import edu.usf.csee.cereal.evotransform.nodes.PoolNode;

public class CondNode extends PoolNode<String> {
    private static final long serialVersionUID = 1L;

    public CondNode() {
        super("~cond"); //add meta coditions here
    }
    @Override
    public String name() { return "metaCondExpr"; } //name from grammar
}