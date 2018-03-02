package com.sirap.geek;

import java.util.List;

import com.google.common.collect.Lists;
import com.sirap.basic.component.Konstants;
import com.sirap.basic.domain.MexItem;
import com.sirap.basic.domain.MexObject;
import com.sirap.basic.domain.ValuesItem;
import com.sirap.basic.tool.C;
import com.sirap.basic.util.CollUtil;
import com.sirap.basic.util.DateUtil;
import com.sirap.basic.util.EmptyUtil;
import com.sirap.basic.util.IOUtil;
import com.sirap.basic.util.OptionUtil;
import com.sirap.basic.util.StrUtil;
import com.sirap.basic.util.XXXUtil;
import com.sirap.common.command.CommandBase;
import com.sirap.common.component.FileOpener;
import com.sirap.common.framework.SimpleKonfig;
import com.sirap.geek.domain.DistrictItem;
import com.sirap.geek.manager.GaodeManager;
import com.sirap.geek.manager.GaodeUtils;

public class CommandGaode extends CommandBase {
	private static final String KEY_GAODE = "gao";
	private static final String KEY_GAODE_POI = "gap";
	private static final String KEY_GAODE_SEARCH = "gas";
	private static final String KEY_GAODE_GEO = "geo";

	public boolean handle() {
		
		boolean toRefresh = OptionUtil.readBooleanPRI(options, "r", false);

		if(is(KEY_GAODE + KEY_2DOTS)) {
			boolean fromAmap = OptionUtil.readBooleanPRI(options, "amap", false);
			if(fromAmap) {
				export(GaodeUtils.fetchAllDistricts());
			} else {
				export(GaodeManager.g().getAllDistricts(toRefresh));
			}
			
			return true;
		}
		
		solo = parseParam(KEY_GAODE + "\\s+(.+?)");
		if(solo != null) {
			boolean showUpperLevels = OptionUtil.readBooleanPRI(options, "p", false);
			Integer nextKLevel = OptionUtil.readInteger(options, "n");
			if(nextKLevel == null && OptionUtil.readBooleanPRI(options, "n", false)) {
				nextKLevel = 1;
			}
			List<DistrictItem> items = GaodeManager.g().getAllDistricts(toRefresh);
			List<DistrictItem> list2 = CollUtil.filter(items, solo, isCaseSensitive());
			List<MexItem> finals = Lists.newArrayList();
			if(showUpperLevels | nextKLevel != null) {
				boolean theFirstItem = true;
				for(DistrictItem item : list2) {
					if(!theFirstItem) {
						finals.add(new MexObject("===="));
					}
					theFirstItem = false;
					if(showUpperLevels) {
						finals.addAll(GaodeManager.g().getUpperDistrictsOf(item));
					}
					if(nextKLevel != null) {
						if(nextKLevel <= 0) {
							nextKLevel = 1;
						}
						finals.addAll(GaodeManager.g().getLowerDistrictsOf(item, nextKLevel));
					}
				}
				export(finals);
			} else {
				export(list2);
			}
			
			return true;
		}

		solo = parseParam(KEY_GAODE_POI + "\\s+(.+?)");
		if(solo != null) {
			List<ValuesItem> items = GaodeUtils.tipsOf(solo);
			export(items);
			
			return true;
		}
		
		params = parseParams(KEY_GAODE_SEARCH + "\\s+(\\S+)(|\\s+[0-3]?)");
		if(params != null) {
			String keyword = params[0];
			String level = "0";
			if(!params[1].isEmpty()) {
				level = params[1];
			}
			
			String result = GaodeUtils.districtsOf(keyword, level);
			export(result);
			return true;
		}
		
		params = parseParams(KEY_GAODE_GEO + "\\s+(.+?)");
		if(params != null) {
			String ack = params[0];
			String bye = OptionUtil.readString(ack, "g", "");
			boolean showJson = OptionUtil.readBooleanPRI(options, "j", false);
			List<String> lines = Lists.newArrayList();
			if(GaodeUtils.isCoordination(ack)) {
				if(OptionUtil.readBooleanPRI(options, "r", false)) {
					export(KEY_GAODE_GEO + " " + GaodeUtils.reverseLongAndLat(ack));
					
					return true;
				}
				String radius = "1000";
				if(!EmptyUtil.isNullOrEmpty(bye)) {
					radius = bye;
				}
				
				lines = GaodeUtils.regeocodeOf(ack, radius);
			} else {
				lines = GaodeUtils.geocodeOf(ack, bye);
			}

			if(!showJson) {
				lines = prettyFormatOf(lines);
			}
			if(OptionUtil.readBooleanPRI(options, "v", false)) {
				String variables = generateVariables(lines);
				generatePicker(variables);
			} else {
				export(lines);
			}
			
			return true;
		}
		
		return false;
	}
	
	private String generateVariables(List<String> lines) {
		String regexTemp = "\"{0}\"\\s*:\\s*\"([^\"]+)\"";
		String regexA = StrUtil.occupy(regexTemp, "formatted_address");
		String regexB = StrUtil.occupy(regexTemp, "location");
		String oneline = StrUtil.connect(lines);
		String address = StrUtil.findFirstMatchedItem(regexA, oneline);
		String location = StrUtil.findFirstMatchedItem(regexB, oneline);
		String temp = "location = \"{0}\"; address = \"{1}\";";
		
		return StrUtil.occupy(temp, location, address);
	}
	
	private void generatePicker(String addressAndLocationVariables) {
		String filePath = "data/pickerT.html";
		List<String> lines = IOUtil.readResourceIntoList(filePath, Konstants.CODE_UTF8);
		String newLine = null;
		int index = 0;
		String key = "//PLACEHOLDER";
		for(String line : lines) {
			if(line.trim().startsWith(key)) {
				newLine = line.replaceAll(key + ".+", addressAndLocationVariables);
				break;
			}
			index++;
		}
		if(newLine == null) {
			XXXUtil.alert("Uncanny, not found placeholder: " + key);
		}
		lines.set(index, newLine);
		String fileName = "picker.html";
		String dir = getExportLocation();
		String pcikerFilepath = dir + fileName;
		if(SimpleKonfig.g().isExportWithTimestampEnabled(options)) {
			pcikerFilepath = dir + DateUtil.timestamp() + "_" + fileName;
		}

		IOUtil.saveAsTxtWithCharset(lines, pcikerFilepath, Konstants.CODE_UTF8);
		C.pl2("Exported => " + pcikerFilepath);
		FileOpener.open(pcikerFilepath);
	}
	
	private List<String> prettyFormatOf(List<String> lines) {
		List<String> items = Lists.newArrayList();
		String regex = "\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"";
		for(String line : lines) {
			if(StrUtil.isRegexFound(regex, line)) {
				items.add(line.trim());
			}
		}
		
		return items;
	}
}