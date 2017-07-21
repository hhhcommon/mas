package com.sirap.basic.thread.business;

import com.sirap.basic.domain.MexObject;
import com.sirap.basic.thread.WorkerGeneralItemOriented;
import com.sirap.basic.util.DateUtil;
import com.sirap.basic.util.FileUtil;
import com.sirap.basic.util.IOUtil;
import com.sirap.basic.util.RandomUtil;

public class InternetFileFetcher extends WorkerGeneralItemOriented<MexObject> {
	private String storage;
	private String suffixWhenObscure;
	private boolean useUniqueFilename;
	
	public InternetFileFetcher(String storage) {
		this.storage = storage;
	}
	
	public InternetFileFetcher(String storage, String suffixWhenObscure) {
		this.storage = storage;
		this.suffixWhenObscure = suffixWhenObscure;
	}
	
	public boolean isUseUniqueFilename() {
		return useUniqueFilename;
	}

	public void setUseUniqueFilename(boolean useUniqueFilename) {
		this.useUniqueFilename = useUniqueFilename;
	}

	@Override
	public Object process(MexObject link) {
		Object tempObj = link.getObj();
		if(tempObj == null) {
			return null;
		}
		
		String url = tempObj.toString();
		int count = countOfTasks - tasks.size();
		String unique = "";
		if(useUniqueFilename) {
			unique = DateUtil.timestamp() + "_" + RandomUtil.letters(4) + "_";
		}
		String filePath = storage + unique + FileUtil.generateFilenameByUrl(url, suffixWhenObscure);
		if(FileUtil.exists(filePath)) {
			status(STATUS_TEMPLATE_SIMPLE, count, countOfTasks, "Existed =>", filePath);
		} else {
			status(STATUS_TEMPLATE_SIMPLE, count, countOfTasks, "Fetching...", url);
			FileUtil.makeDirectoriesIfNonExist(storage);
			boolean flag = IOUtil.downloadNormalFile(url, filePath, true);
			if(flag) {
				status(STATUS_TEMPLATE_SIMPLE, count, countOfTasks, "Saved =>", filePath);
			} else {
				status(STATUS_TEMPLATE_SIMPLE, count, countOfTasks, "Error =>", url);
				return null;
			}
		}
		
		return filePath;
	}
}