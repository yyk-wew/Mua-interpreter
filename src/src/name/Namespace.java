package src.name;

import java.util.HashMap;
import java.util.regex.Pattern;

import src.operation.Operation;
import src.util.Util;
import src.value.MuaNumber;
import src.value.MuaValue;
import src.value.MuaWord;

public class Namespace {

	private static final Pattern pattern = Pattern.compile("^[A-Za-z]+[A-Za-z0-9_]*");
	private HashMap<String, MuaValue> nameMap = new HashMap<>();
	
	public Namespace() {
	}
	
	public static boolean isLegalName(String content) {
		return Util.isType(pattern, content) && !Operation.checkOp(content);
	}
	
	public void createName(MuaWord name, MuaValue value) {
		if(!isLegalName(name.getValue())) {
			// TODO raise exception
		}
		nameMap.put(name.getValue(), value);
	}
	
	public void eraseName(MuaWord name) {
		if(!isLegalName(name.getValue())) {
			// TODO raise exception
		}
		if(!nameMap.containsKey(name.getValue())) {
			// TODO not exist this key
		}
		nameMap.remove(name.getValue());
	}
	
	public boolean existName(MuaWord name) {
		if(!isLegalName(name.getValue())) {
			// TODO raise exception
		}
		return nameMap.containsKey(name.getValue());
	}
	
	public MuaValue getValue(MuaWord name) {
		if(!nameMap.containsKey(name.getValue())) {
			// TODO not exist this key
		}
		return nameMap.get(name.getValue());
	}
	
	// for debug
	public void print() {
		for(String key : nameMap.keySet()) {
			System.out.println(key);
			MuaValue v = nameMap.get(key);
			if(v instanceof MuaNumber) {
				MuaNumber n = (MuaNumber)v;
				n.print();
			} else if(v instanceof MuaWord) {
				MuaWord w = (MuaWord)v;
				w.print();
			}
		}
	}
	
}