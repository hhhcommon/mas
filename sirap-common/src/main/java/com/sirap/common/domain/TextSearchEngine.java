package com.sirap.common.domain;

import java.util.List;

import com.sirap.basic.component.MexedOption;
import com.sirap.basic.domain.MexItem;
import com.sirap.basic.util.OptionUtil;

@SuppressWarnings("serial")
public class TextSearchEngine extends MexItem {
	private String prefix;
	private String folders;
	private String fileCriteria;
	private boolean useCache;
	private boolean useSpace = true;

	private static final String KEY_USECACHE = "usecache";
	private static final String KEY_USESPACE = "usespace";
	
	public TextSearchEngine() {
		
	}
	
	public TextSearchEngine(String prefix, String folders, String fileCriteria) {
		this.prefix = prefix;
		this.folders = folders;
		this.fileCriteria = fileCriteria;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public String getFileCriteria() {
		return fileCriteria;
	}

	public void setFileCriteria(String fileCriteria) {
		this.fileCriteria = fileCriteria;
	}

	public String getFolders() {
		return folders;
	}

	public void setFolders(String folders) {
		this.folders = folders;
	}
	
	public boolean isUseCache() {
		return useCache;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}	
	
	public boolean isUseSpace() {
		return useSpace;
	}

	public void setUseSpace(boolean useSpace) {
		this.useSpace = useSpace;
	}

	@Override
	public boolean parse(String source) {
		String[] info = source.split("#");
		if(info.length < 3) {
			return false;
		}

		setPrefix(info[0].trim());
		setFolders(info[1].trim());
		setFileCriteria(info[2].trim());
		
		String optionsStr = null;
		if(info.length >= 4) {
			optionsStr = info[3].trim();
			List<MexedOption> options = OptionUtil.parseOptions(optionsStr);
			Object value = OptionUtil.readOption(options, KEY_USECACHE);
			if(value instanceof Boolean) {
				setUseCache((Boolean)value);
			}
			value = OptionUtil.readOption(options, KEY_USESPACE);
			if(value instanceof Boolean) {
				setUseSpace((Boolean)value);
			}
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(prefix).append("\t");
		sb.append(folders).append("\t");
		sb.append(fileCriteria);
		
		return sb.toString(); 
	}
}