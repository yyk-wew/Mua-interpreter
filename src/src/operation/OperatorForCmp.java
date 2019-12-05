package src.operation;

import java.util.Stack;

import src.name.Namespace;
import src.value.MuaNumber;
import src.value.MuaValue;
import src.value.MuaWord;

public abstract class OperatorForCmp extends Operation {

	protected MuaValue formerOperand;
	protected MuaValue latterOperand;
	
	public OperatorForCmp() {
	}

	protected void preCompare(Stack<MuaValue> paras) {
		if(paras.size() < 2)
			// TODO raise exception for wrong number of parameters
			return;
		
		MuaValue formerPara = paras.pop();
		MuaValue latterPara = paras.pop();
		
		// judge the type
		if(formerPara instanceof MuaWord && latterPara instanceof MuaWord) {
			// both word
			this.formerOperand = formerPara;
			this.latterOperand = latterPara;
		} else if(formerPara instanceof MuaNumber && latterPara instanceof MuaWord) {
			// one word to number
			this.formerOperand = formerPara;
			this.latterOperand = ((MuaWord)latterPara).getMuaNumber();
		} else if(formerPara instanceof MuaWord && latterPara instanceof MuaNumber) {
			// one word to number
			this.formerOperand = ((MuaWord)formerPara).getMuaNumber();
			this.latterOperand = latterPara;
		} else {
			// both number
			this.formerOperand = formerPara;
			this.latterOperand = latterPara;
		}
		
		
	}
	
	@Override
	public abstract void execute(Stack<MuaValue> paras, Namespace namespace);

}
