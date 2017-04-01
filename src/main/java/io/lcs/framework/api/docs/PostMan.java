package io.lcs.framework.api.docs;

import com.alibaba.fastjson.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyright (c) 2017 Hengte Technology Co.,Ltd.
 * All Rights Reserved.<br />
 *
 * @author lcs
 * @version 1.0
 * @date 2017-04-01
 */
public class PostMan {
	private static String doc = "/tmp/postman";

	public Info info = new Info();

	public List<Item> item = new ArrayList<>();

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, NoSuchFieldException, NoSuchMethodException {
		if (StringUtils.hasLength(System.getProperty("postman.path"))) {
			doc = System.getProperty("postman.path");
		}

		PostMan script = new PostMan();
		script.info.name = "postman-" + new Date().getTime();
		PostManRequest postMan = ApiScanner.getPostMan();
		List<Api> apiList = ApiScanner.scanRequestMapping();
		List<Item> itemList = script.item;

		for( Api api : apiList ){
			if(StringUtils.isEmpty(api.getApi()[0]))continue;
			String[] path = api.getApi()[0].split("/");
			int len = path.length;
			if (len < 2) continue;
			List<Item> currentList = itemList;
			if (len > 1) {
				String name = path[0];
				Item item = getItem(currentList, name);

				if (item == null) {
					item = new Item();
					item.name = name;
					item.description = name;
					currentList.add(item);
				}
				currentList = item.item;
			}
			Item apiItem = new Item();
			apiItem.name = api.getApi()[0];
			apiItem.description =api.getInfo().summary();
			apiItem.request = JSONObject.parseObject(postMan.request(api));
			apiItem.request.put("description","### " + api.getInfo().summary() + "\n" +
					"| 参数      | 类型   | 是否必填 | 描述 | 示例值 |\n" +
					"|:----------|:-------|:---------|:-----|:-------|\n" +
					api.getParamList());
			apiItem.item = null;
			currentList.add(apiItem);
		}

		File readmeFile = new File(doc, "postman.json");

		if(!readmeFile.getParentFile().exists()) readmeFile.getParentFile().mkdirs();
		if(readmeFile.exists()) readmeFile.delete();
		readmeFile.createNewFile();
		FileWriter writer = new FileWriter(readmeFile);
		writer.write(JSONObject.toJSONString(script, true));
		writer.flush();
		writer.close();
		System.out.println("postman file : " + readmeFile.getAbsolutePath());

	}


	private static Item getItem( List<Item> list , String name){
		for( Item item:list ){
			if(name.equals(item.name))return item;
		}
		return null;
	}

	public static class Item{
		public String name;
		public String description;
		public JSONObject request;
		public List<Item> item = new ArrayList<>();

	}

	public class Info{
		public String name;
		public String description;
		public final String schema = "https://schema.getpostman.com/json/collection/v2.0.0/collection.json";
	}

	public static interface PostManRequest {
		/**
		 * PostMan request
		 * <code>
		 "request": {
		 "url": "http://api",
		 "method": "POST",
		 "header": [
		 {
		 "key": "Content-Type",
		 "value": "application/x-www-form-urlencoded",
		 "description": ""
		 }
		 ],
		 "body": {
		 "mode": "urlencoded",
		 "urlencoded": [
		 {
		 "key": "abs",
		 "value": "lcs",
		 "type": "text",
		 "enabled": true
		 },
		 {
		 "key": "sin",
		 "value": "360",
		 "type": "text",
		 "enabled": true
		 }
		 ]
		 },
		 "description": ""
		 },
		 * </code>
		 * @param api
		 * @return
		 */
		String request(Api api);
	}

}
