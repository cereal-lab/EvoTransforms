package edu.usf.csee.cereal.evotransform.nodes.list;

import edu.usf.csee.cereal.evotransform.nodes.SeqNode;

public class PrependNode extends SeqNode {
    private static final long serialVersionUID = 1L;
    public PrependNode() {
        super(false);
    }
    @Override
    public String name() { return "prepend"; } //name from grammar
}