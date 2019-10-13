package edu.usf.csee.cereal.evotransform.nodes.arithm;

import edu.usf.csee.cereal.evotransform.nodes.BaseNode;

public class AddNode extends BaseNode {
    private static final long serialVersionUID = 1L;
    public AddNode() { super("+"); }
    @Override
    public String name() { return "add"; } //name from grammar
}