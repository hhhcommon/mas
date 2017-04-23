package com.sirap.bible;

import java.util.List;

import com.sirap.basic.domain.MexedObject;
import com.sirap.basic.tool.C;
import com.sirap.basic.util.StrUtil;
import com.sirap.common.command.CommandBase;

public class CommandBible extends CommandBase {

	private static final String KEY_BIBLE = "bible";

	public boolean handle() {
		if(is(KEY_BIBLE)) {
			List<BibleBook> items = BibleManager.g().listAllBooks();
			export(items);
			
			return true;
		}
		
		singleParam = parseParam(KEY_BIBLE + "\\s(.*?)");
		if(singleParam != null) {
			List<BibleBook> items = BibleManager.g().listBooksByName(singleParam);
			export(items);
			
			return true;
		}
		
		String regex = "([123][a-z]{2,}|[a-z]{3,})(\\d{1,3})";
		params = parseParams(regex);
		if(params != null) {
			String bookName = params[0];
			int chapter = Integer.parseInt(params[1]);
			BibleBook book = BibleManager.g().searchByName(bookName, chapter);
			if(book != null) {
				if(!StrUtil.equals(book.getName(), bookName)) {
					C.pl("Looking for book " + book.getName() + " chapter " + chapter);
				}
				List<MexedObject> items = BibleManager.g().getChapter(book.getName(), chapter);
				export(items);
				
				return true;
			}
		}
		
		return false;
	}
}