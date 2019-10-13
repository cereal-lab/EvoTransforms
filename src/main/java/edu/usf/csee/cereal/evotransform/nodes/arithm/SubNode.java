package edu.usf.csee.cereal.evotransform.nodes.arithm;

import edu.usf.csee.cereal.evotransform.nodes.BaseNode;

public class SubNode extends BaseNode {
    private static final long serialVersionUID = 1L;
    public SubNode() {
        super("-");
    }
    @Override
    public String name() { return "sub"; } //name from grammar
}