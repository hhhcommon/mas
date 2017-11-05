package com.sirap.basic.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sirap.basic.exception.MexException;

public class WebReader {
	
	private String url;
	private String charset;
	private String serverCharset;
	private boolean isMethodPost;
	private String requestParams;

	public WebReader(String url) {
		this.url = url;
	}
	
	public WebReader(String url, String charset) {
		this.url = url;
		this.charset = charset;
		requestParams = StrUtil.findFirstMatchedItem("\\?(.+)", url);
	}
	
	public boolean isMethodPost() {
		return isMethodPost;
	}

	public void setMethodPost(boolean isMethodPost) {
		this.isMethodPost = isMethodPost;
	}
	
	public String getCharset() {
		return charset;
	}

	public String getServerCharset() {
		return serverCharset;
	}

	public String readIntoString() {
		String content = null;
		try {
			BufferedReader br = createReader();

			String record;
			StringBuffer sb = new StringBuffer();
			while ((record = br.readLine()) != null) {
				if(serverCharset == null) {
					serverCharset = parseWebCharsetByMeta(record);
				}
				sb.append(record);
			}

			content = sb.toString();
			sb.setLength(0);
			br.close();
		} catch (Exception ex) {
			String msg = ex + "\nURL=>" + url + "\nLocation=>" + getClass().getName();
			throw new MexException(msg);
		}

		return content;
	}
	
	public List<String> readIntoList() {
		List<String> records = new ArrayList<>();
		try {
			BufferedReader br = createReader();

			String record;
			while ((record = br.readLine()) != null) {
				if(serverCharset == null) {
					serverCharset = parseWebCharsetByMeta(record);
				}
				records.add(record);
			}

			br.close();
		} catch (Exception ex) {
			String msg = ex + "\nURL=>" + url + "\nLocation=>" + getClass().getName();
			throw new MexException(msg);
		}

		return records;
	}
	
	private BufferedReader createReader() throws Exception {
		URLConnection conn = new URL(url).openConnection();
		equip(conn);
		String charsetInHeader = parseWebCharsetByHeader(conn);
		if(charsetInHeader != null) {
			charset = charsetInHeader;
			serverCharset = charsetInHeader;
		}
		InputStreamReader isr = new InputStreamReader(conn.getInputStream(), charset);
		BufferedReader br = new BufferedReader(isr);
		
		return br;
	}
	
	public static String parseWebCharsetByHeader(URLConnection conn) {
		Map<String, List<String>> map = conn.getHeaderFields();  
        Set<String> keys = map.keySet();  
        Iterator<String> iterator = keys.iterator();  
  
        while (iterator.hasNext()) {  
            String key = iterator.next();
            if(!StrUtil.equals("Content-Type", key)) {
            	continue;
            }
            
            List<String> items = map.get(key);
            for(String item : items) {
            	String charset = StrUtil.findFirstMatchedItem("charset=([^\\s;,]+)", item);
            	if(charset != null) {
            		return charset;
            	}
            }
        }
        
		return null;
	}
	
	public static String parseWebCharsetByMeta(String content) {
		String regex = "<meta http-equiv=\"Content-Type\" content=\"[^>]+charset=([^\\s;,\"]+)\">";
		String charset = StrUtil.findFirstMatchedItem(regex, content);
		
		return charset;
	}
	
	private void equip(URLConnection urlConn) throws Exception {
		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1WOW64rv:25.0) Gecko/20100101 Firefox/25.0");
		if(isMethodPost) {
			urlConn.setDoOutput(true);
			urlConn.setDoInput(true);
			PrintWriter out = new PrintWriter(urlConn.getOutputStream());
	        out.print(requestParams);
	        out.flush();
		}
	}

	public void setRequestParams(String requestParams) {
		this.requestParams = requestParams;
	}	
}
