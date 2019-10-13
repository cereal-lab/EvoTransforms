package edu.usf.csee.cereal.evotransform.nodes.bool;

import edu.usf.csee.cereal.evotransform.nodes.BaseNode;

public class AndNode extends BaseNode {

    private static final long serialVersionUID = 1L;

    public AndNode() {
        super("&&");
    }

    @Override
    public String name() { return "and"; } //name from grammar


}