package io.lcs.framework.api.docs;

import io.lcs.framework.api.annotation.ApiParam;
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
		//doc = System.getProperty("md.path", doc);

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
				.replace("${paramDemo}", getParamDemo(api))
				.replace("${responseDemo}", getResponeDemo(api))
		);
		writer.append(getEnums(api, enumMap));
		writer.flush();
		writer.close();
		System.out.printf("gen file : %s \n", file.getAbsolutePath());
	}

	private static String getParams( Api api ){
		StringBuffer params = new StringBuffer();
		if(api.getInfo().value().length == 0){
			return "| 无      |   |   |  |  - |\n";
		}
		for (ApiParam attributes: api.getInfo().value()) {
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

	private static String getEnums( Api api ,Map<Class, Object[]> enumMap  ){
		StringBuffer enums = new StringBuffer();
		for (ApiParam attributes: api.getInfo().value()) {
			Object[] values = enumMap.get(attributes.type());
			if( values == null ) continue;
			StringBuffer enumValues = new StringBuffer();
			for (Object e : values) {
				try {
					enumValues.append(String.format("| %s | %s |\n", attributes.type().getMethod("val").invoke(e), attributes.type().getMethod("desc").invoke(e)));
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
				}
			}
			enums.append(ENUM_TEMPLETE.replace("${name}", attributes.type().getSimpleName())
					.replace("${enums}", enumValues))
					.append("\n");

		}

		return enums.toString();
	}

	private static String getParamDemo(Api api){
		if( StringUtils.hasLength(api.getParamDemo()) ) return api.getParamDemo();
		StringBuffer params = new StringBuffer("{\n");

		for (ApiParam attributes : api.getInfo().value()) {
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

	private static String getResponeDemo(Api api){
		if( StringUtils.hasLength(api.getResponseDemo()) ) return api.getResponseDemo();
		StringBuffer params = new StringBuffer("{\n");
		params.append("}\n");
		return params.toString().replace(",\n}", "\n}");
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
			"|                 |        |      |        |\n" +
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
