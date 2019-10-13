package edu.usf.csee.cereal.evotransform.nodes;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class BaseNode extends GPNode {


    private String symbol;

    public BaseNode(String symbol) {
        this.symbol = symbol;

    }
    private static final long serialVersionUID = 1L;

    @Override
    public void eval(EvolutionState arg0, int arg1, GPData data, ADFStack arg3, GPIndividual arg4, Problem arg5) {
        //check if we are unary or binary 
        StrategoGPData strategoScript = ((StrategoGPData)data);
        if (this.children.length == 1) {
            strategoScript.append(symbol);
            boolean complexChild = this.children[0].children != null && this.children[0].children.length > 0;
            if (complexChild) {
                strategoScript.append("(");
            }
            this.children[0].eval(arg0, arg1, data, arg3, arg4, arg5);
            if (complexChild) {
                strategoScript.append(")");
            }
        } else if (this.children.length == 2) {            
            boolean complexChild1 = this.children[0].children != null && this.children[0].children.length > 0;
            if (complexChild1) {
                strategoScript.append("(");
            }
            this.children[0].eval(arg0, arg1, data, arg3, arg4, arg5);
            if (complexChild1) {
                strategoScript.append(")");
            }
            strategoScript.append(" ");
            strategoScript.append(symbol);
            strategoScript.append(" ");
            boolean complexChild2 = this.children[1].children != null && this.children[1].children.length > 0;
            if (complexChild2) {
                strategoScript.append("(");
            }
            this.children[1].eval(arg0, arg1, data, arg3, arg4, arg5);
            if (complexChild2) {
                strategoScript.append(")");
            }
        }
    }

    @Override
    public String toString() {     
        String result = "";
        if (this.children.length == 1) {
            result = symbol;
            boolean complexChild = this.children[0].children != null && this.children[0].children.length > 0;
            if (complexChild) result += "(";
            result += this.children[0].toString();
            if (complexChild) result += ")";
        } else if (this.children.length == 2) {            
            boolean complexChild1 = this.children[0].children != null && this.children[0].children.length > 0;
            if (complexChild1)  result += "(";
            result += this.children[0].toString();
            if (complexChild1) result += ")";
            result += " " + symbol + " ";
            boolean complexChild2 = this.children[1].children != null && this.children[1].children.length > 0;
            if (complexChild2) result += "(";
            result += this.children[1].toString();
            if (complexChild2) result += ")";
        }
        return result;
    }

}