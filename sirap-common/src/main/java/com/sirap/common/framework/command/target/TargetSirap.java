package com.sirap.common.framework.command.target;

import java.util.List;

import com.google.common.collect.Lists;
import com.sirap.basic.thirdparty.TrumpHelper;
import com.sirap.basic.tool.C;
import com.sirap.basic.util.FileUtil;
import com.sirap.basic.util.IOUtil;
import com.sirap.basic.util.StrUtil;
import com.sirap.common.component.FileOpener;
import com.sirap.common.framework.SimpleKonfig;

public class TargetSirap extends TargetFile {
	
	public TargetSirap() {}
	
	public TargetSirap(String folderpath, String filename) {
		this.folderpath = folderpath;
		this.filename = filename;
	}
	
	@Override
	public void export(List records, String options, boolean withTimestamp) {
		String charset = SimpleKonfig.g().getCharsetInUse();
		String filePath = withTimestamp ? getTimestampPath() : getFilePath();
		String content = StrUtil.connectWithLineSeparator(records);
		String passcode = SimpleKonfig.g().getSecurityPasscode();
		String encoded = TrumpHelper.encodeBySIRAP(content, passcode);
		
		List<String> list = Lists.newArrayList(encoded);
		IOUtil.saveAsTxtWithCharset(list, filePath, charset);

		C.pl2("Exported => " + FileUtil.canonicalPathOf(filePath));
		if(SimpleKonfig.g().isGeneratedFileAutoOpen()) {
			FileOpener.open(filePath);
		}
	}
	
	@Override
	public String toString() {
		return folderpath + " *** " + filename;
	}
}
