package com.sirap.extractor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import com.sirap.basic.domain.MexObject;
import com.sirap.basic.util.Colls;
import com.sirap.basic.util.IOUtil;
import com.sirap.basic.util.OptionUtil;
import com.sirap.basic.util.StrUtil;
import com.sirap.common.command.CommandBase;
import com.sirap.common.component.FileOpener;
import com.sirap.extractor.domain.MeituLassItem;
import com.sirap.extractor.domain.MeituOrgItem;
import com.sirap.extractor.impl.ExtractorFancy;
import com.sirap.extractor.impl.MeituImageLinksExtractor;
import com.sirap.extractor.manager.MeituManager;

public class CommandFancy extends CommandBase {
	
	public static final String KEY_MEITU = "mei";

	public boolean handle() {
		String types = StrUtil.connect(new ArrayList<String>(ExtractorFancy.TYPE_METHOD.keySet()), "|");
		solo = parseParam("lady(" + types+ ")");
		if(solo != null) {
			String type = solo.toLowerCase();
			String method = ExtractorFancy.TYPE_METHOD.get(type);
			List<String> links = ExtractorUtil.photos(method, ExtractorFancy.class);

			export(links);
			return true;
		}
		
		//fetch lass image links from library path that indicates one or more albums
		solo = parseParam(KEY_MEITU + " (.+)");
		if(solo != null && StrUtil.isRegexMatched("[a-z\\d/]+", solo)) {
			List<MexObject> items = new ArrayList<>();
			MeituImageLinksExtractor justin = new MeituImageLinksExtractor(solo);
			justin.process();
			items.addAll(justin.getItems());
			
			boolean showAll = OptionUtil.readBooleanPRI(options, "all", false);
			if(showAll) {
				List<MexObject> morePages = MeituManager.g().explode(solo, justin.getCountOfAlbum());
				items.addAll(MeituManager.g().getImageLinks(morePages));
			} else {
				int sample = g().getUserNumberValueOf("mei.some", 7);
				items = Colls.top(items, sample);
			}
			
			export(items);

			return true;
		}
		
		//fetch all organizations in library
		if(is(KEY_MEITU + "..") || is(KEY_MEITU + ".orgs")) {
			List<MeituOrgItem> items = MeituManager.g().getAllOrgItems(false);
			Collections.sort(items);
			export(items);
		}
		
		//fetch all lass paths
		if(is(KEY_MEITU + "....") || is(KEY_MEITU + ".lass")) {
			List<MeituLassItem> items = MeituManager.g().getAllLassItems();
			items = new ArrayList<>(new LinkedHashSet<>(items));
			Collections.sort(items);
			export(items);
		}
		
		//fetch lass intros from lass path which locates in txt file
		solo = parseParam(KEY_MEITU + ".intro (.+?)");
		if(solo != null) {
			File file = parseFile(solo);
			if(file != null) {
				String filePath = file.getAbsolutePath();
				if(FileOpener.isTextFile(filePath)) {
					List<String> records = IOUtil.readFileIntoList(filePath);
					List<MexObject> items = MeituManager.g().readLassPath(records);
					List<MeituLassItem> intros = MeituManager.g().getAllLassIntros(items);
					Collections.sort(intros);
					export(intros);
				}
			}
			
			return true;
		}
		
		return false;
	}
}
