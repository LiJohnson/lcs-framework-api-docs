package io.lcs.framework.api.docs;

import io.lcs.framework.api.annotation.ApiParam;
import io.lcs.framework.api.annotation.ApiResponse;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lcs on 02/03/2017.
 */
public class MdDoc {

	private static String doc = "/tmp/doc";

	public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, IOException, InvocationTargetException {
		doc = System.getProperty("md.path", doc);

		List<Api> list = ApiScanner.scanRequestMapping();

		Map<Class, Object[]> enumMap = ApiScanner.scanEnum();

		StringBuffer apis = new StringBuffer();
		int index = 0;
		for (Api api : list) {
			if( api.getApi()[0].indexOf("/") > 0 ){
				genFile(api,enumMap);
				apis.append(String.format("| %d | [%s](%s.md) | %s | %s |\n", ++index,
						api.getApi()[0],
						api.getApi()[0],
						api.getInfo().summary(),
						api.getInfo() != null && !api.getInfo().hasImplemented() ? "︎✘" : "✔"
				));
			}
		}

		File readmeFile = new File(doc, "README.md");

		if(!readmeFile.getParentFile().exists()) readmeFile.getParentFile().mkdirs();
		if(readmeFile.exists()) readmeFile.delete();

		readmeFile.createNewFile();
		FileWriter writer = new FileWriter(readmeFile);
		writer.write(TOC.replace("${apis}", apis.toString()));
		writer.flush();
		writer.close();

		File summaryFile = new File(doc, "SUMMARY.md");
		if(summaryFile.exists()) summaryFile.delete();
		summaryFile.createNewFile();
		writer = new FileWriter(summaryFile);
		writer.write(SUMMARY.replace("${SUMMARY}", getSummary(list)));
		writer.flush();
		writer.close();

	}

	private  static void genFile( Api api ,Map<Class, Object[]> enumMap ) throws IOException {
		File file = new File(doc, api.getApi()[0] + ".md");
		if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
		if(file.exists()) file.delete();
		file.createNewFile();
		FileWriter writer = new FileWriter(file);
		writer.write(MD_TEMPLATE.replace("${description}", api.getInfo().summary())
				.replace("${api}", api.getApi()[0])
				.replace("${params}", getParams(api))
				.replace("${responses}", getResponse(api))
				.replace("${paramDemo}", getParamDemo(api))
				.replace("${responseDemo}", getResponseDemo(api))
		);
		writer.append(getEnums(api, enumMap));
		writer.flush();
		writer.close();
		System.out.printf("gen file : %s \n", file.getAbsolutePath());
	}

	private static String getParams( Api api ){
		StringBuffer params = new StringBuffer();
		if(api.getApiRequest().value().length == 0){
			return "| 无      |   |   |  |  - |\n";
		}
		for (ApiParam attributes: api.getApiRequest().value()) {
			String type = attributes.type().getSimpleName();
			if( attributes.type().isEnum() ){
				type = String.format("[enum(%s)](#enum-%s) ", type, type.toLowerCase());
			}
			params.append(String.format("| %s      | %s  | %s | %s| %s |\n",
					attributes.value(),
					type.toString().replaceAll("\\[\\]", "[ ]"),
					attributes.required(),
					attributes.description(),
					StringUtils.hasLength(attributes.demo()) ? attributes.demo() : " - "
			));
		}
		return params.toString();
	}

	private static String getResponse( Api api ){
		StringBuffer params = new StringBuffer();
		if (api.getResponse() == null || api.getResponse().isEmpty()) {
			return "| 无      |     |  |  - |\n";
		}
		for (ApiParam attributes: api.getResponse()) {
			String type = attributes.type().getSimpleName();
			if( attributes.type().isEnum() ){
				type = String.format("[enum(%s)](#enum-%s) ", type, type.toLowerCase());
			}
			params.append(String.format("| %s      | %s | %s| %s |\n",
					attributes.value(),
					type.toString().replaceAll("\\[\\]", "[ ]"),
					attributes.description(),
					StringUtils.hasLength(attributes.demo()) ? attributes.demo() : " - "
			));
		}
		return params.toString();
	}

	private static String getEnums( Api api ,Map<Class, Object[]> enumMap  ){
		StringBuffer enums = new StringBuffer();
		Map<Object[], Class> enumData = new HashMap<>();
		for( ApiParam param : api.getApiRequest().value() ){
			Object[] values = enumMap.get(param.type());
			if( values == null || enumData.get(values) != null) continue;
			enumData.put(values, param.type());
		}
		for( ApiParam param : api.getResponse() ){
			Object[] values = enumMap.get(param.type());
			if( values == null || enumData.get(values) != null) continue;
			enumData.put(values, param.type());
		}
		for (Map.Entry<Object[],Class> entry:enumData.entrySet()) {
			Object[] values = entry.getKey();
			if( values == null ) continue;
			StringBuffer enumValues = new StringBuffer();
			for (Object e : values) {
				try {
					enumValues.append(String.format("| %s | %s |\n", e.getClass().getMethod("name").invoke(e), e.getClass().getMethod("getText").invoke(e)));
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
				}
			}
			enums.append(ENUM_TEMPLETE.replace("${name}", entry.getValue().getSimpleName())
					.replace("${enums}", enumValues))
					.append("\n");
		}

		return enums.toString();
	}

	private static String getParamDemo(Api api){
		if( StringUtils.hasLength(api.getParamDemo()) ) return api.getParamDemo();
		StringBuffer params = new StringBuffer("{\n");

		for (ApiParam attributes : api.getApiRequest().value()) {
			String val = attributes.type().equals(String.class) ? "\"" + attributes.demo() + "\"" : attributes.demo();
			if( !StringUtils.hasLength(val) ) val = "\"\"";
			params.append(String.format("\t\"%s\":%s,\n",
					attributes.value(),
					val
			));
		}
		params.append("}\n");
		return params.toString().replace(",\n}", "\n}");
	}

	private static String getResponseDemo(Api api){
		if( StringUtils.hasLength(api.getResponseDemo()) ) return api.getResponseDemo();
		StringBuffer params = new StringBuffer();
		String tab = "";

		if (api.getApiResponse().type().equals(ApiResponse.EType.ARRAY)) {
			params = new StringBuffer("[\n");
			tab += "\t";
		}
		if (api.getApiResponse().type().equals(ApiResponse.EType.PAGE)) {
			params = new StringBuffer("{\n" +
					"    \"last\": true,\n" +
					"    \"totalPages\": 0,\n" +
					"    \"totalElements\": 0,\n" +
					"    \"sort\": null,\n" +
					"    \"first\": true,\n" +
					"    \"numberOfElements\": 0,\n" +
					"    \"size\": 10,\n" +
					"    \"number\": 0,\n" +
					"    \"content\": [\n" +
					"");
			tab += "\t\t";
		}

		params.append(tab + "{\n");
		tab += "\t";
		String split = "";
		for (ApiParam apiParam:api.getResponse()) {
			params.append(String.format("%s%s\"%s\":\"\"", split, tab, apiParam.value()));
			if (split.length() == 0) {
				split = ",\n";
			}
		}
		params.append("\n");

		tab = tab.replaceFirst("\t", "");
		params.append(tab+"}\n");

		if (api.getApiResponse().type().equals(ApiResponse.EType.ARRAY)) {
			tab = tab.replaceFirst("\t", "");
			params.append("]\n");
		}
		if (api.getApiResponse().type().equals(ApiResponse.EType.PAGE)) {
			tab = tab.replaceFirst("\t", "");
			params.append(tab);
			params.append("]\n");
			tab = tab.replaceFirst("\t", "");
			params.append(tab);
			params.append("}");
		}

		return params.toString();
	}

	private static String getSummary(List<Api> list) {
		List<String> parent = new ArrayList<>();
		Map<String, List<Api>> map = new HashMap<>();
		List<Api> sub = null;
		StringBuffer summary = new StringBuffer();
		for (Api api : list) {
			String path = api.getApi()[0].split("/")[0];
			sub = map.get(path);
			if(sub == null){
				sub = new ArrayList<>();
				map.put(path, sub);
				parent.add(path);
			}
			sub.add(api);
		}
		for (String path : parent) {
			sub = map.get(path);
			summary.append(String.format("* %s\n", path));
			for (Api api : sub) {
				if( !StringUtils.isEmpty(api.getInfo().summary()) ){
					summary.append(String.format(" * [%s](%s.md)\n", api.getInfo().summary(), api.getApi()[0]));
				}
			}
		}
		return summary.toString();

	}


	private static String MD_TEMPLATE = "\n" +
			//"[返回](../index.md)\n\n" +
			"## ${description}\n" +
			"\n" +
			"### api\n" +
			"\n" +
			"${api}\n" +
			"\n" +
			"### 请求参数\n" +
			"| 参数      | 类型   | 是否必填 | 描述 | 示例值 |\n" +
			"|:----------|:-------|:---------|:-----|:-------|\n" +
			"${params}\n" +

			"\n" +
			"\n" +
			"### 示例\n" +
			"```json\n" +
			"${paramDemo}" +
			"```\n" +
			"### 返回结果\n" +
			"\n" +
			"| 参数            | 类型   | 描述 | 示例值 |\n" +
			"|:----------------|:-------|:-----|:-------|\n" +
			"${responses}\n" +
			"\n" +
			"### 示例\n" +
			"```json\n" +
			"\n" +
			"${responseDemo}"+
			"\n" +
			"```\n";

	private static String TOC = "" +
			"# Introduction\n" +
			"| index      | api   | 说明 | 实现|\n" +
			"|:----------|:-------|:-----|:-----\n" +
			"${apis}\n";

	private static String ENUM_TEMPLETE = "" +
			"### ENUM-${name} \n" +
			"| val   | desc |\n" +
			"|:-------|:-----|\n" +
			"${enums}\n" +
			"\n";

	private static String SUMMARY = "" +
			"# Summary\n" +
			"\n" +
			"* [Introduction](README.md)\n" +
			"${SUMMARY}";

}
