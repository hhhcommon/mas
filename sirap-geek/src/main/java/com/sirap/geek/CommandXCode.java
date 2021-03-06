package com.sirap.geek;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sirap.basic.domain.MexItem;
import com.sirap.basic.domain.MexLocale;
import com.sirap.basic.domain.MexObject;
import com.sirap.basic.search.MexFilter;
import com.sirap.basic.thirdparty.ShiroHelper;
import com.sirap.basic.tool.C;
import com.sirap.basic.util.Colls;
import com.sirap.basic.util.EmptyUtil;
import com.sirap.basic.util.LocaleUtil;
import com.sirap.basic.util.MathUtil;
import com.sirap.basic.util.OptionUtil;
import com.sirap.basic.util.SecurityUtil;
import com.sirap.basic.util.StrUtil;
import com.sirap.basic.util.XCodeUtil;
import com.sirap.common.command.CommandBase;
import com.sirap.geek.domain.AsciiRecord;
import com.sirap.geek.domain.CharsetCode;
import com.sirap.geek.manager.GeekManager;
	
public class CommandXCode extends CommandBase {

	private static final String KEY_ASCII_SHORT = "asc";
	private static final String KEY_ASCII_ALL = "ascii";
	private static final String KEY_TO_BASE64 = "t64";
	private static final String KEY_FROM_BASE64 = "f64";
	private static final String KEY_DIGEST_ALGORITHMS = "(SM3|SHA|SHA1|SHA224|SHA256|SHA512|MD2|MD5)";
	private static final String KEY_URL_ENCODE = "url";
	private static final String KEY_URL_DECODE = "urx";
	private static final String KEY_ENCODE = "cde";
	private static final String KEY_DECODE = "cdx";
	private static final String KEY_ISO = "iso";
	private static final String KEY_CURRENCY = "ccy";
	private static final String KEY_DATE_FORMAT_SYMBOL = "dfs";

	public boolean handle() {
		
		if(is(KEY_ASCII_SHORT)) {
			List<MexItem> items = new ArrayList<>();
			MexObject header = new MexObject((AsciiRecord.getHeader()));
			items.add(header);
			
			int[][] ranges= {{'0', '9'}, {'A', 'Z'}, {'a', 'z'}};
			for(int i = 0; i < ranges.length; i++) {
				int[] range = ranges[i];
				items.addAll(GeekManager.g().ascii(range));
			}
			
			items.add(header);
			
			export(items);
			
			return true;
		}
		
		if(isIn(KEY_ASCII_ALL + "," + KEY_ASCII_SHORT + KEY_2DOTS)) {
			List<MexItem> items = GeekManager.g().asciiAll();
			MexObject header = new MexObject((AsciiRecord.getHeader()));
			items.add(0, header);
			items.add(header);
			
			export(items);
			
			return true;
		}
		
		solo = parseParam(KEY_ASCII_SHORT + "\\s(.+?)");
		if(solo != null) {
			List<MexItem> items = Colls.filter(GeekManager.g().asciiAll(), solo, isCaseSensitive(), isStayCriteria());
			
			if(!EmptyUtil.isNullOrEmpty(items)) {
				items.add(0, new MexObject((AsciiRecord.getHeader())));
			}

			export(items);
			
			return true;
		}

		solo = parseParam(KEY_TO_BASE64 + "\\s(.+?)");
		if(solo != null) {
			String result = XCodeUtil.toBase64(solo);
			export(result);
			
			return true;
		}
		
		solo = parseParam(KEY_FROM_BASE64 + "\\s(.+?)");
		if(solo != null) {
			String result = XCodeUtil.fromBase64(solo);
			export(result);
			
			return true;
		}

		params = parseParams(KEY_DIGEST_ALGORITHMS + "\\s(.+?)");
		if(params != null) {
			String temp = params[0];
			String what = params[1];
			String algo = temp;

			if(StrUtil.isRegexMatched("SHA\\d{3}", algo)) {
				StringBuffer sb = new StringBuffer(algo);
				sb.insert(3, "-");
				algo = sb.toString();
			}
			
			String salt = OptionUtil.readString(options, "s");
			int count = OptionUtil.readIntegerPRI(options, "i", 1);
			
			List<String> whatsaltcount = StrUtil.splitByRegex(what, "\\s+");
			if(!whatsaltcount.isEmpty()) {
				what = whatsaltcount.get(0);
				if(whatsaltcount.size() > 1) {
					salt = whatsaltcount.get(1);
					if(whatsaltcount.size() > 2) {
						String tempCount = whatsaltcount.get(2);
						count = MathUtil.toInteger(tempCount, count);
					}
				}
			}
			
			File normal = parseFile(what);
			boolean isFile = normal != null;
			String result = null;
			
			
			if(salt != null) {
				Object source = isFile ? normal : what;
				if(StrUtil.equals(salt, ".")) {
					salt = null;
				}
				result = ShiroHelper.hash(algo, source, salt, count);
			} else {
				if(isFile) {
					result = SecurityUtil.digestFile(normal.getAbsolutePath(), algo);
				} else {
					result = SecurityUtil.digest(what, algo);
				}
			}
			
			String prefix = "";
			if(isFile) {
				prefix = "File, ";
			}
			
			List<String> items = new ArrayList<>();
			if(salt != null) {
				items.add("salt: " + salt);
			}
			if(count > 1) {
				items.add("times: " + count);
			}
			items.add(result);
			items.add(prefix + algo.toUpperCase() + " generates " + result.length() + " chars.");
			
			export(items);
			return true;
		}
		
		solo = parseParam(KEY_URL_ENCODE + "\\s(.+?)");
		if(solo != null) {
			String charset = g().getCharsetInUse();
			String value = XCodeUtil.urlEncode(solo, charset);
			C.pl("Encode with charset: " + charset);
			export(value);
			
			return true;
		}
		
		solo = parseParam(KEY_URL_DECODE + "\\s(.+?)");
		if(solo != null) {
			String charset = g().getCharsetInUse();
			String value = XCodeUtil.urlDecode(solo, charset);
			C.pl("Decode with charset: " + charset);
			export(value);
			
			return true;
		}
		
		if(is(KEY_ENCODE + KEY_2DOTS)) {
			List<CharsetCode> items = GeekManager.g().allCodingNames();
			export(items);
			
			return true;
		}
		
		solo = parseParam(KEY_ENCODE + "-([\\S]+)");
		if(solo != null) {
			String criteria = solo;
			List<CharsetCode> codes = GeekManager.g().searchCharsetNames(criteria);
			
			List<String> charsets = new ArrayList<>();
			for(CharsetCode mexCode : codes) {
				String codeName = mexCode.getName();
				charsets.add(codeName);
			}
			
			export(codes);
			
			return true;
		}
		
		solo = parseParam(KEY_ENCODE + "\\s(.+?)");
		if(solo != null) {
			List<String> items = GeekManager.g().encodeStringByUnicodeUTF8GBK(solo);
			export(items);
			return true;
		}
		
		params = parseParams(KEY_ENCODE + "-([\\S]+)\\s(.+?)");
		if(params != null) {
			String criteria = params[0];
			String content = params[1];
			List<CharsetCode> codes = GeekManager.g().searchCharsetNames(criteria);
			
			List<String> charsets = new ArrayList<>();
			for(CharsetCode mexCode : codes) {
				String codeName = mexCode.getName();
				charsets.add(codeName);
			}
			List<String> items = GeekManager.g().encodeStringByCharset(content, charsets);
			export(items);
			
			return true;
		}
		
		solo = parseParam(KEY_DECODE + "\\s(.+?)");
		if(solo != null) {
			List<String> items = GeekManager.g().decodeStringByUnicodeUTF8GBK(solo);
			export(items);
			return true;
		}
		
		params = parseParams("([a-z0-9\\-_]{3,30})\\s(.+?)");
		if(params != null) {
			String accurateCharset = "^" + params[0] + "$";
			List<CharsetCode> mexCodes = GeekManager.g().searchCharsetNames(accurateCharset);
			if(mexCodes.size() == 1) {
				String code = mexCodes.get(0).getName();
				String content = params[1];
				String value = XCodeUtil.replaceHexChars(content, code);
				export(value);
				
				return true;
			}
		}
		
		if(isIn(KEY_CURRENCY + KEY_2DOTS)) {
			String extraLocales = g().getUserValueOf("iso.locales");
			List<MexObject> records = LocaleUtil.getAllCurrencies(extraLocales);

			export(records);
			
			return true;
		}
		
		solo = parseParam(KEY_CURRENCY + "\\s(.+?)");
		if(isSingleParamNotnull()) { 
			String extraLocales = g().getUserValueOf("iso.locales");
			List<MexObject> records = LocaleUtil.getAllCurrencies(extraLocales);
			
			MexFilter<MexObject> filter = new MexFilter<MexObject>(solo, records);
			List<MexObject> items = filter.process();

			export(items);
			
			return true;
		}
		
		if(isIn(KEY_DATE_FORMAT_SYMBOL + KEY_2DOTS)) {
			List<String> records = LocaleUtil.getAllMonthWeekdays();
			export(records);
			
			return true;
		}
		
		solo = parseParam(KEY_DATE_FORMAT_SYMBOL + "\\s(.+?)");
		if(isSingleParamNotnull()) { 
			List<String> records = LocaleUtil.getAllMonthWeekdays();
			export2(records, solo);
			
			return true;
		}
		
		if(isIn(KEY_ISO + KEY_2DOTS, KEY_ISO + KEY_EQUALS)) {
			String extraLocales = g().getUserValueOf("iso.locales");
			List<MexLocale> records = LocaleUtil.AAM_LOCALES;
			C.pl(LocaleUtil.getIso3Header(extraLocales));
			
			export(Colls.items2PrintRecords(records, creteaLocaleParams()));
			
			return true;
		}
		
		solo = parseParam(KEY_ISO + "\\s(.+?)");
		if(isSingleParamNotnull()) { 
			String extraLocales = g().getUserValueOf("iso.locales");
			MexFilter<MexLocale> filter = new MexFilter<MexLocale>(solo, LocaleUtil.AAM_LOCALES);
			List<MexLocale> items = filter.process();

			if(!EmptyUtil.isNullOrEmpty(items)) {
				C.pl(LocaleUtil.getIso3Header(extraLocales));
			}

			export(Colls.items2PrintRecords(items, creteaLocaleParams()));
			
			return true;
		}
	
		solo = parseParam(KEY_ISO + "=\\s*([^|&]*?)");
		if(solo != null) {
			Locale locale = null;
			List<MexLocale> items = null;
			String criteria = "^" + solo + "$";
			List<MexLocale> accurateItems = LocaleUtil.searchSimilars(criteria);
			if(accurateItems.size() == 1) {
				locale = accurateItems.get(0).getLocale();
			} else {
				criteria = solo.replace('_', '|');
				items = LocaleUtil.searchSimilars(criteria);
				if(items.size() == 1) {
					locale = items.get(0).getLocale();
				}
			}
			
			Map mexItemParams = creteaLocaleParams();
			if(locale != null) {
				g().setLocale(locale);
				MexLocale ml = new MexLocale(locale);
				C.pl2("Locale set as " + ml.toPrint(mexItemParams));
			} else {
				C.pl("[" + solo + "] is not a valid locale, did you mean one of these?");
				if(EmptyUtil.isNullOrEmpty(items)) {
					C.listSome(Colls.items2PrintRecords(LocaleUtil.AAM_LOCALES, mexItemParams), 10);
				} else {
					C.list(Colls.items2PrintRecords(items, mexItemParams));
					C.pl();
				}
			}
			
			return true;
		}
				
		return false;
	}
	
	protected Map<String, Object> creteaLocaleParams() {
		String multipleLocalesString = g().getUserValueOf("iso.locales");
		List<Locale> localesInDisplay = LocaleUtil.ofs(multipleLocalesString);
		
		return createMexItemParams("localesInDisplay", localesInDisplay);
	}
}
