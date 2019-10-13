package edu.usf.csee.cereal.evotransform.nodes.bool;

import edu.usf.csee.cereal.evotransform.nodes.BaseNode;

public class NotNode extends BaseNode {

    public NotNode() {
        super("!");
    }
    private static final long serialVersionUID = 1L;

    @Override
    public String name() { return "not"; } //name from grammar

}