package edu.usf.csee.cereal.evotransform.nodes.bool;

import edu.usf.csee.cereal.evotransform.nodes.BaseNode;

public class EqNode extends BaseNode {

    private static final long serialVersionUID = 1L;

    public EqNode() {
        super("==");
    }

    @Override
    public String name() { return "eq"; } //name from grammar


}