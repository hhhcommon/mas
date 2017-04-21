package com.sirap.common.command.explicit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.sirap.basic.tool.C;
import com.sirap.basic.util.EmptyUtil;
import com.sirap.basic.util.PanaceaBox;
import com.sirap.basic.util.StrUtil;
import com.sirap.common.command.CommandBase;
import com.sirap.common.framework.Janitor;
import com.sirap.common.framework.SimpleKonfig;

public class CommandShortcut extends CommandBase {
	
	private static final String KEY_WINDOWS_COMMAND = "c\\.";
	private static final String KEY_WINDOWS_LOCK = "l";
	private static final String KEY_WINDOWS_LOGOUT = "out";
	private static final String KEY_WINDOWS_TURNOFF = "off";
	private static final String KEY_WINDOWS_RESTART = "rest";
	private static final String KEY_DYNAMIC = "(\\S+)\\s(.+)";
	
	public boolean handle() {
		
		singleParam = parseParam(KEY_WINDOWS_COMMAND + "(.{3,}?)");
		if(singleParam != null) {
			executeInternalCmd(singleParam);
			return true;
		}
		
		String dosPrefix = g().getUserValueOf("dos.prefix");
		if(!EmptyUtil.isNullOrEmpty(dosPrefix)) {
			List<String> items = StrUtil.split(dosPrefix);
			for(String item : items) {
				if(StrUtil.equalsCaseSensitive(command, item) || command.startsWith(item + " ")) {
					executeInternalCmd(command);
					return true;
				}
			}
		}
		
		if(is(KEY_WINDOWS_LOCK)) {
			PanaceaBox.execute("rundll32 user32.dll,LockWorkStation");
			C.pl2("lock computer.");
			
			return true;
		}
		
		if(is(KEY_WINDOWS_TURNOFF)) {
			PanaceaBox.execute("shutdown -s -t 1");
			C.pl2("turn off computer immediately.");
			
			return true;
		}
		
		if(is(KEY_WINDOWS_LOGOUT)) {
			PanaceaBox.execute("shutdown -l");
			C.pl2("log out computer immediately.");
			
			return true;
		}
		
		if(is(KEY_WINDOWS_RESTART)) {
			PanaceaBox.execute("shutdown -r");
			C.pl2("restart computer.");
			
			return true;
		}
		
		params = parseParams(KEY_DYNAMIC);
		if(params != null) {
			String dynamite = getDynamicCommand(params[0]);
			if(dynamite != null) {
				String temp = StrUtil.occupy(dynamite, params[1]);
				C.pl(command + "=" + temp);
				Janitor.g().process(temp);
				
				return true;
			}
		}
		
		String app = SimpleKonfig.g().getUserValueOf(command);
		if(app != null) {
			C.pl(command + "=" + app);
			Janitor.g().process(app);
			
			return true;
		}

		return false;
	}
	
	//nin {0} = E:/KDB/statics/wx 3 {0}
	private String getDynamicCommand(String prefix) {
		HashMap<String, String> map = SimpleKonfig.g().getUserProps().getKeyValuesByPartialKeyword(prefix);
		Iterator<String> it = map.keySet().iterator();
		
		while(it.hasNext()) {
			String key = it.next();
			String temp = key.replace(prefix, "").trim();
			String regex = "\\{\\d{1,2}\\}";
			if(StrUtil.isRegexMatched(regex, temp)) {
				String value = map.get(key);
				
				return value;
			}
		}
		
		return null;
	}
}
