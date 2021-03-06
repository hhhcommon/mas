package com.sirap.common.command;

import java.io.File;
import java.util.List;

import com.sirap.basic.tool.C;
import com.sirap.basic.util.EmptyUtil;
import com.sirap.basic.util.IOUtil;
import com.sirap.basic.util.OptionUtil;
import com.sirap.basic.util.PanaceaBox;
import com.sirap.basic.util.StrUtil;
import com.sirap.common.component.Alarm;
import com.sirap.common.component.FileOpener;
import com.sirap.common.manager.AlarmManager;

public class CommandTask extends CommandBase {

	private static final String KEY_ALARM_SHOW = "al";
	private static final String KEY_ALARM_CANCEL = "ac(|[\\d|,]+)";
	private static final String KEY_TASK = "tk";
	
	@Override
	public boolean handle() {
		
		if(g().isFromWeb()) {
			return false;
		}
		
		String regex = "al([\\+@#\\d:hms]+)(|\\s.*?)";
		params = StrUtil.parseParams(regex, input);
		if(params != null) {
			String actionInfo = params[1];
			if(EmptyUtil.isNullOrEmpty(actionInfo)) {
				actionInfo = g().getUserValueOf("action.def");
			}
			if(EmptyUtil.isNullOrEmpty(actionInfo)) {
				C.pl2("There should be some action you like to perform.");
				return true;
			}
			
			String actionInfo2 = actionInfo;
			Alarm al = new Alarm(params[0], actionInfo2) {
				@Override
				public void execute() {
					String action = actionInfo2;
					C.pl("==================== START of alarm task => " + action + " ====================");
					List<String> actions = StrUtil.split(action);
					executeActions(actions);
					C.pl("==================== END of alarm task => " + action + " ====================");
				}
			};
			
			if(al.isActive()) {
				C.pl(al.display(g().getLocale()));
				C.pl2(Alarm.alarmNowInfo(g().getLocale()));
				
				return true;
			}
		}
		
		if(is(KEY_ALARM_SHOW)) {
			List<Alarm> alarms = AlarmManager.g().getAlarms();
			for(Alarm al:alarms) {
				C.pl(al.display(g().getLocale()));
			}
			
			if(alarms.size() > 0) {
				C.pl2(Alarm.alarmNowInfo(g().getLocale()));
			} else {
				C.pl2("Currently no alarm.");
			}
			
			return true;
		}
		
		solo = parseParam(KEY_ALARM_CANCEL);
		if(solo != null) {
			List<String> orderIds = solo.isEmpty() ? null : StrUtil.split(solo);			
			int count = AlarmManager.g().cancelAlarms(true, orderIds);
			if(count > 0) {
				C.pl2(Alarm.alarmNowInfo(g().getLocale()));
			} else {
				if(count != -1) {
					C.pl2("No alarm has been cancelled.");
				}
			}
			
			return true;
		}
		
		solo = StrUtil.parseParam(KEY_TASK + "\\s(.*?)", input);
		if(solo != null) {
			File file = parseFile(solo);
			if(file != null && FileOpener.isTextFile(file.getAbsolutePath())) {
				String cat = IOUtil.charsetOfTextFile(file.getAbsolutePath());
				if(OptionUtil.readBooleanPRI(options, "x", false)) {
					cat = switchChartset(cat);
				}
				List<String> tasks = FileOpener.readTextContent(file.getAbsolutePath(), false, cat);
				executeActions(tasks);
			} else {
				List<String> actions = StrUtil.split(solo);
				executeActions(actions);
			}

			return true;
		}

		return false;
	}
}
