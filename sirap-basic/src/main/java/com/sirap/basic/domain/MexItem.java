package com.sirap.basic.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sirap.basic.tool.C;
import com.sirap.basic.util.StrUtil;

@SuppressWarnings("serial")
public abstract class MexItem implements Serializable {

	public static final String SYMBOL_CARET = "^";
	public static final String SYMBOL_USD = "$";
	public static final String SYMBOL_ASTERISK = "*";
	public static final String CHARS_REGEX_ESCAPE = "\\$()*+.[]?^{}|";

	protected boolean caseSensitive;
	protected int pseudoOrder;
	
	public boolean parse(String record) {
		return false;
	}
	
	public int getPseudoOrder() {
		return pseudoOrder;
	}

	public void setPseudoOrder(int pseudoOrder) {
		this.pseudoOrder = pseudoOrder;
	}
	
	public boolean isMatched(String keyWord, boolean caseSensitive) {
		return isMatched(keyWord);
	}
	
	public boolean isMatched(String keyWord) {
		return false;
	}
	
	protected String parseWholeMatched(String keyWord) {
		if(keyWord != null && keyWord.length() > 2 && keyWord.startsWith(SYMBOL_CARET) && keyWord.endsWith(SYMBOL_USD)) {
			String temp = keyWord.substring(1, keyWord.length() - 1);
			return temp;
		}
		
		return null;
	}

	protected String parseLeftMatched(String keyWord) {
		if(keyWord != null && keyWord.length() > 1 && keyWord.startsWith(SYMBOL_CARET) && !keyWord.endsWith(SYMBOL_USD)) {
			return keyWord.substring(1);
		}
		
		return null;
	}
	
	protected String parseRightMatched(String keyWord) {
		if(keyWord != null && keyWord.length() > 1 && keyWord.endsWith(SYMBOL_USD) && !keyWord.startsWith(SYMBOL_CARET)) {
			return keyWord.substring(0, keyWord.length() - 1);
		}
		
		return null;
	}
	
	protected boolean isRegexMatched(String source, String keyWord) {
		
		String wholeMatchedStr = parseWholeMatched(keyWord);
		if(wholeMatchedStr != null && StrUtil.equals(wholeMatchedStr, source)) {
			return true;
		}
		
		String leftMatchedStr = parseLeftMatched(keyWord);
		if(leftMatchedStr != null && StrUtil.startsWith(source, leftMatchedStr)) {
			return true;
		}
		
		String rightMatchedStr = parseRightMatched(keyWord);
		if(rightMatchedStr != null && StrUtil.endsWith(source, rightMatchedStr)) {
			return true;
		}
		
		String genuineRegex = StrUtil.parseParam("rx:(.+)", keyWord);
		if(genuineRegex != null && StrUtil.isRegexFound(genuineRegex, source)) {
			return true;
		}
		
		return false;
	}
	
	public String toPrint() {
		return toString();
	}
	
	public String toPrint(Map<String, Object> params) {
		return toPrint();
	}
	
	public String toPrint(String options) {
		return toPrint();
	}
	
	public List<String> toPDF() {
		List<String> list = new ArrayList<String>();
		list.add(toPrint());
		
		return list;
	}
	
	public void print() {
		C.pl(toPrint());
	}
	
	public void print(Map<String, Object> params) {
		C.pl(toPrint(params));
	}
	
	public void print(String options) {
		C.pl(toPrint(options));
	}
	
	public String printAll(Object... values) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < values.length; i++) {
			Object obj = values[i];
			
			sb.append(obj);
			if(i != values.length - 1) {
				sb.append(" ");
			}
		}
		
		return sb.toString();
	}
	
	public String printAllButNull(Object... values) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < values.length; i++) {
			Object obj = values[i];
			if(obj == null) {
				continue;
			}
			
			sb.append(obj);
			if(i != values.length - 1) {
				sb.append(" ");
			}
		}
		
		return sb.toString();
	}
}
