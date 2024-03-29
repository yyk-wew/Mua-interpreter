package src.mua;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import src.name.Namespace;
import src.operation.Operation;
import src.util.Util;
import src.value.MuaList;
import src.value.MuaNumber;
import src.value.MuaValue;
import src.value.MuaWord;

public class Parser {
	public static Scanner sc = new Scanner(System.in);
	public static List<String> paraList = new ArrayList<>();
	private static Stack<MuaValue> runStack = new Stack<>();
	private static Stack<String> parseListStack = new Stack<>();
	private static Stack<Double> operandStack = new Stack<>();
	private static Stack<String> operatorStack = new Stack<>();

	public Parser() {
		super();
	}
	
	public static boolean parse(Namespace namespace) {
		
		// return false means can not get parameters, error
		
		// execute a single parameter
		String para = getNextPara();
		if(para == null)
			return false;
//		System.out.println(para.substring(0, 1));
		// basic operation
		if(Operation.checkOp(para)) {
			Operation op = Operation.getOperation(para);
			int numOfPara = op.getParaNum();
			for(int i = 0; i < numOfPara; i++) parse(namespace);
			op.execute(runStack, namespace);
		}
		else if(namespace.existFunc(para)) {
			MuaList func = namespace.getFunc(para);
			Namespace funcNamespace = new Namespace();
			funcNamespace.createName(para, func);
			
			MuaList argName = (MuaList)func.get(0);
			
			// create parameters
			for(int i = 0; i < argName.size(); i++) {
				parse(namespace);
				MuaValue v = runStack.pop();
				funcNamespace.createName((String)argName.get(i), v);
			}
			
			parseFunc(funcNamespace, func);
		}
		else if(para.substring(0, 1).equals(":")){
			Operation op = Operation.getOperation("thing");
			MuaValue value = new MuaWord(para.substring(1, para.length()), true);
			runStack.push(value);
			op.execute(runStack, namespace);
		}
		else if(para.substring(0, 1).equals("(")) {
			parseExp(namespace, para);
		}
		else if(para.substring(0,1).equals("[")) {
			parseListStack.clear();
			parseListStack.push(para.substring(1, para.length()));
			MuaList list;
			list = parseList();
			runStack.push(list);
		}
		else {
			// must be a value
			MuaValue value;
			char start = para.charAt(0);
			if(Character.isDigit(start)) {
				para = Util.addSpace(para);
				String[] tempList = para.split(" ");
				String firstPara = "";
				for(int i = 0; i < tempList.length; i++) {
					firstPara = tempList[i];
					if(!firstPara.isEmpty()) {
						for(int j = tempList.length - 1; j > i; j--) paraList.add(0, tempList[j]);
						break;
					}
				}
				value = MuaValue.getValue(firstPara);
			}else if(start == '-') {
				para = para.substring(1, para.length());
				para = Util.addSpace(para);
				String[] tempList = para.split(" ");
				String firstPara = "";
				for(int i = 0; i < tempList.length; i++) {
					firstPara = tempList[i];
					if(!firstPara.isEmpty()) {
						for(int j = tempList.length - 1; j > i; j--) paraList.add(0, tempList[j]);
						break;
					}
				}
				value = MuaValue.getValue("-"+firstPara);
			}
			else {
				value = MuaValue.getValue(para);
			}
			
			runStack.push(value);
		}
		return true;
	}
	
	private static void parseExp(Namespace namespace, String para) {
		
		// clone the operator and operand Stack
		Stack<String> tempOperatorStack = new Stack<>();
		int size = operatorStack.size();
//		System.out.println(size);
		for(int i = 0; i < size; i++)
			tempOperatorStack.push(operatorStack.pop());
			
		Stack<Double> tempOperandStack = new Stack<>();
		size = operandStack.size();
		for(int i = 0; i < size; i++)
			tempOperandStack.push(operandStack.pop());
		
		// start parse
		String operInMem = "";
		int flag = 1;
		
		while(true) {
			para = Util.addSpace(para);
			
			String[] expList = para.split(" ");
			
//			System.out.println("Parse Exp");
			
			for(int i = 0; i < expList.length; i++) {
				
//				System.out.println(expList[i]);
				
				if(expList[i].isEmpty())
					continue;
				
//				System.out.println(expList[i]);
				if(Util.isExpOperator(expList[i]) != 0) {
					// operator
					if(expList[i].equals("-") && (operInMem.equals("(") || Util.isExpOperator(operInMem) >= 2)) {
						flag *= -1;
					}
					else {
						int calRes;
						calRes = pushExpOperator(expList[i]);
						if(calRes == 1) {
							for(int j = i + 1; j < expList.length; j++)
								paraList.add(expList[j]);
							break;
						}
					}
					operInMem = expList[i];
				}
				else if(namespace.existFunc(expList[i])) {
					// function
					
					MuaList func = namespace.getFunc(expList[i]);
					Namespace funcNamespace = new Namespace();
					funcNamespace.createName(expList[i], func);
					
					MuaList argName = (MuaList)func.get(0);
					
					// create parameters
					for(int j = 0; j < argName.size(); j++) {
						parse(namespace);
						MuaValue v = runStack.pop();
						funcNamespace.createName((String)argName.get(j), v);
					}
					
					parseFunc(funcNamespace, func);
					
					MuaValue tempV = runStack.pop();
					// if not number, then error
					double tempNum = ((MuaNumber)tempV).getValue();
					
					operandStack.push(tempNum);
					
					operInMem = "";
				}
				else if(expList[i].substring(0, 1).equals(":")){
					Operation op = Operation.getOperation("thing");
					MuaValue value = new MuaWord(expList[i].substring(1, expList[i].length()), true);
					runStack.push(value);
					op.execute(runStack, namespace);
					
					MuaValue tempV = runStack.pop();
					// if not number, then error
					double tempNum = ((MuaNumber)tempV).getValue();
					
					operandStack.push(tempNum);
					
					operInMem = "";
				}
				else {
					// number
//					System.out.println(expList[i]);
					double num = Double.valueOf(expList[i]) * flag;
					flag = 1;
					operandStack.push(num);
					operInMem = "";
				}
			}
			if(operatorStack.isEmpty())
				break;
			para = getNextPara();
			// error for para == null
		}
		
		
		double res = operandStack.pop();
		MuaNumber value = new MuaNumber(res);
		runStack.push(value);
		
		// resume stack
		size = tempOperatorStack.size();
		for(int i = 0; i < size; i++)
			operatorStack.push(tempOperatorStack.pop());
		
		size = tempOperandStack.size();
		for(int i = 0; i < size; i++)
			operandStack.push(tempOperandStack.pop());
		
		return;
	}
	
	private static int pushExpOperator(String content) {
		// judge priority
		int priority = Util.isExpOperator(content);
		
		// if empty
		if(operatorStack.isEmpty()) {
			operatorStack.push(content);
			return 0;
		}
		
		// calculate all
		if(content.equals(")")) {
			while(true) {
				String lastOper = operatorStack.pop();
				if(lastOper.equals("(")) {
					break;
				}else {
					double operand2 = operandStack.pop();
					double operand1 = operandStack.pop();
					double tempRes = Util.doExp(lastOper, operand1, operand2);
					operandStack.push(tempRes);
				}
			}
			return 1;
		}
		
		// calculate temporary
		while(true) {
			String lastOper = operatorStack.peek();
			int lastPriority = Util.isExpOperator(lastOper);
			if(lastPriority > priority || lastPriority == 1) {
				operatorStack.push(content);
				break;
			}else {
				operatorStack.pop();
				double operand2 = operandStack.pop();
				double operand1 = operandStack.pop();
				double tempRes = Util.doExp(lastOper, operand1, operand2);
				operandStack.push(tempRes);
			}
		}
		
		return 0;
	}
	
	private static void parseFunc(Namespace funcNamespace, MuaList func) {
		MuaList program = (MuaList)func.get(1);
		
		// clone the paraList
		List<String> tempParaList = new ArrayList<>();
		for(int i = 0; i < paraList.size(); i++)
			tempParaList.add(paraList.get(i));
		paraList.clear();
		
		// execute program
		String inst = program.getOriginList();
		String[] elements = inst.split("\\s+");
		// check empty
		if(!inst.isEmpty()) {
			for(int j = elements.length - 1; j >= 0; j--) paraList.add(0, elements[j]);
		}
		while(!paraList.isEmpty())
			parse(funcNamespace);
//		System.out.println("out!");
		MuaValue output = funcNamespace.getResult();
		if(output != null) {
//			System.out.println("Func result");
//			output.print();
			runStack.push(output);
		}			
		
		for(int i = 0; i < tempParaList.size(); i++)
			paraList.add(tempParaList.get(i));
		return;
	}
	
	private static MuaList parseList() {
		MuaList l = new MuaList();
		String para = parseListStack.pop();
		while(true) {
//			String frontChar = para.substring(0, 1);
//			String endChar = para.substring(para.length() - 1, para.length());
			int frontParenthese = para.indexOf("[");
			int endParenthese = para.indexOf("]");
			if(frontParenthese != -1 && ((frontParenthese < endParenthese) ^ (endParenthese == -1))) {
				if(!para.substring(0, frontParenthese).isEmpty())
					l.add(para.substring(0,  frontParenthese));
				String rest = para.substring(frontParenthese + 1, para.length());
				parseListStack.push(rest);
				MuaList l_temp = parseList();
				l.add(l_temp);
			}
			else if(endParenthese != -1) {
				if(!para.substring(0, endParenthese).isEmpty())
					l.add(para.substring(0, endParenthese));
				String rest = para.substring(endParenthese + 1, para.length());
				if(!rest.isEmpty())
					parseListStack.push(rest);
				break;
			}
			else {
				if(!para.isEmpty())
					l.add(para);
			}
			
			if(parseListStack.isEmpty()) {
				String newPara = getNextPara();
//				if(newPara == null)
					//TODO error for read ending
				parseListStack.push(newPara);
			}
			para = parseListStack.pop();
		}
		return l;
	}

	private static String getNextPara() {
		// return parameter if exists, otherwise return NULL
		String para = null;
		boolean read = true;
		while(true) {
			if(paraList.isEmpty()) read = readNext();
			if(!read) return null;
			if(paraList.isEmpty()) continue;
			para = paraList.get(0);
			paraList.remove(0);
			if(!para.isEmpty()) break;
		}
		return para;
	}
	
	private static boolean readNext() {
		// read an instruction
		if(!sc.hasNext()) {
			// TODO: error for read nothing
			return false;
		}
		String inst = sc.nextLine();
		
		// delete comment
		int commentIndex = inst.indexOf("//");
		if(commentIndex != -1) {
			inst = inst.substring(0, commentIndex);
		}
		
		// divide into parameters
		String[] paras = inst.split("\\s+");
		// check empty
		if(!inst.isEmpty()) {
			for(int i = 0; i < paras.length; i++) paraList.add(paras[i]);
		}
		
		return true;
	}
}
