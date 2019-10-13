package edu.usf.csee.cereal.evotransform.nodes.arithm;

import edu.usf.csee.cereal.evotransform.nodes.BaseNode;

public class AssignNode extends BaseNode {
    private static final long serialVersionUID = 1L;
    public AssignNode() { super("="); }
    @Override
    public String name() { return "assign"; } //name from grammar
}