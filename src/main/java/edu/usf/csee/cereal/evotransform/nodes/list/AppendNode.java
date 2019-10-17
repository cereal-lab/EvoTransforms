package edu.usf.csee.cereal.evotransform.nodes.list;

import edu.usf.csee.cereal.evotransform.nodes.SeqNode;

public class AppendNode extends SeqNode {
    private static final long serialVersionUID = 1L;
    public AppendNode() {
        super(true);
    }    
    @Override
    public String name() { return "append"; } //name from grammar
}