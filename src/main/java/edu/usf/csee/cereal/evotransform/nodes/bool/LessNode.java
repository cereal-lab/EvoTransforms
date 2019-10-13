package edu.usf.csee.cereal.evotransform.nodes.bool;

import edu.usf.csee.cereal.evotransform.nodes.BaseNode;

public class LessNode extends BaseNode {

    private static final long serialVersionUID = 1L;

    public LessNode() {
        super("<");
    }

    @Override
    public String name() { return "ls"; } //name from grammar


}