package com.sirap.basic.domain;

import java.util.List;

import com.google.common.collect.Lists;
import com.sirap.basic.component.map.AlinkMap;
import com.sirap.basic.json.JsonUtil;
import com.sirap.basic.util.OptionUtil;
import com.sirap.basic.util.StrUtil;

@SuppressWarnings("serial")
public class KeyValuesItem extends MexItem {
	
	protected AlinkMap<String, Object> box = new AlinkMap<String, Object>();
	
	public KeyValuesItem() {

	}
	
	public KeyValuesItem(String key, Object value) {
		box.put(key, value);
	}
	
	public void add(String key, Object value) {
		box.put(key, value);
	}
	
	public boolean isMatched(String keyWord) {
		List<Object> values = Lists.newArrayList(box.values());
		for(Object item : values) {
			if(StrUtil.contains(item + "", keyWord)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String toJson() {
		return JsonUtil.toJson(box);
	}
	
	@Override
	public String toPrettyJson(int depth) {
		return JsonUtil.toPrettyJson(box, depth);
	}
	
	public String toPrint() {
		String options = "";
		return toPrint(options);
	}
	
	public String toPrint(String options) {
		String keys = OptionUtil.readString(options, "keys", "");
		String connector = OptionUtil.readString(options, "c", ", ");
		List<String> list;
		if(keys.isEmpty()) {
			list = Lists.newArrayList(box.keySet());
		} else {
			list = StrUtil.split(keys, '|');
		}
		StringBuffer sb = sb();
		boolean isTheFirstOne = true;
		for(String key : list) {
			if(!isTheFirstOne) {
				sb.append(connector);
			}
			sb.append(box.get(key));
			isTheFirstOne = false;
		}
		
		return sb.toString();
	}

	public String toString() {
		List<String> list = Lists.newArrayList(box.keySet());
		StringBuffer sb = sb();
		boolean isTheFirstOne = true;
		for(String key : list) {
			if(!isTheFirstOne) {
				sb.append(", ");
			}
			sb.append(box.get(key));
			isTheFirstOne = false;
		}
		
		return sb.toString();
	}
}