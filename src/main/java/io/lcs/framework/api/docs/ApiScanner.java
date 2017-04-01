package io.lcs.framework.api.docs;


import io.lcs.framework.api.annotation.ApiInfo;
import io.lcs.framework.api.annotation.ApiParam;
import io.lcs.framework.api.annotation.ApiRequest;
import io.lcs.framework.api.annotation.ApiResponse;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Created by lcs on 02/03/2017.
 */
public class ApiScanner {
	private static String API_PACKAGE = System.getProperty("api.package");
	private static String[] API_FILTER = System.getProperty("api.filter","").split(",");
	private static Resource[] resources;
	static {

		Assert.isTrue(!StringUtils.isEmpty(API_PACKAGE),"无效 api.package");
		String packageClass = String.format("classpath*:/%s/**/*.class", API_PACKAGE.replace('.', '/'));
		System.out.printf("scanning package %s\n\n", packageClass);
		System.out.printf("api filter is %s\n\n", API_FILTER);

		PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		try {
			resources = resourcePatternResolver.getResources(packageClass);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 获取所有api
	 * @return
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 */
	public static List<Api> scanRequestMapping() throws IOException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException {
		PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

		List<Api> list = new ArrayList<>();
		for (Resource r : resources) {
			MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(r);
			AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();

			Class clazz = Class.forName(annotationMetadata.getClassName());

			Map<String, Object> classRequestMapping = annotationMetadata.getAnnotationAttributes(RequestMapping.class.getName());

			for (Method method : clazz.getDeclaredMethods()) {
				RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
				ApiResponse apiResponse = method.getAnnotation(ApiResponse.class);
				if( requestMapping == null ) continue;
				if( apiResponse == null ) continue;
				if (method.getAnnotation(ApiRequest.class) == null) continue;

				Api api = new Api();
				api.setInfo(method.getAnnotation(ApiInfo.class));
				api.setApiRequest(method.getAnnotation(ApiRequest.class));
				api.setApiResponse(apiResponse);
				api.setApi(getApis(classRequestMapping, requestMapping.value()));
				if (StringUtils.hasLength(apiResponse.demo())) {
					api.setResponseDemo(apiResponse.demo());
				}

				if( apiResponse.value().length>0 ){
					api.setResponse(Arrays.asList(apiResponse.value()));
				}else{
					api.setResponse(classToApiParam(apiResponse.bean()));
				}
				list.add(api);
			}
		}

		Collections.sort(list,new Comparator<Api>() {
			@Override
			public int compare(Api o1, Api o2) {
				return o1.getApi()[0].compareTo(o2.getApi()[0]);
			}
		});
		return list;
	}


	private static String[] getApis(Map classRequestMapping, String[] methodRequestMapping) {
		List<String> apis = new ArrayList<>();

		for (String path : methodRequestMapping) {
			if (classRequestMapping != null) {
				for (String path2 : (String[]) classRequestMapping.get("value")) {
					apis.add(String.format("%s/%s", path2, path));
				}
			}else{
				apis.add(path);
			}
		}
		return apis.toArray(new String[0]);
	}

	/**
	 * 获取所有枚举方法
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static Map<Class, Object[]> scanEnum() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
		Map<Class, Object[]> map = new HashMap<>();
		for (Resource r : resources) {
			MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(r);
			AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
			Class clazz = Class.forName(annotationMetadata.getClassName());
			for (Class c : clazz.getInterfaces()) {
				if (c.getName().endsWith("IEnum")) {
					Object[] values = (Object[]) clazz.getMethod("values").invoke(null);
					map.put(clazz, values);
				}
			}
		}
		return map;
	}

	private static List<ApiParam> classToApiParam( Class clazz ){

		if (clazz.getSuperclass() == null || clazz.getSuperclass().getName().indexOf("BasePojo") == -1) {
			return Collections.EMPTY_LIST;
		}

		List<ApiParam> apiParamList = new ArrayList<>();
		for (Field field : clazz.getDeclaredFields()) {
			apiParamList.add(new Api.ApiParamImp()
					.value(field.getName())
					.description(getFieldComment(field))
					.type(field.getType())
			);
		}
		return apiParamList;
	}

	private static String getFieldComment(Field field){
		if(field.getAnnotations() == null) return "";
		for (Annotation annotation : field.getAnnotations()) {
			if(annotation.annotationType().getName().endsWith("Comment") ){
				try {
					return (String) annotation.annotationType().getMethod("value").invoke(annotation);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}

	public static PostMan.PostManRequest getPostMan() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
		for (Resource r : resources) {
			MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(r);
			AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
			Class clazz = Class.forName(annotationMetadata.getClassName());

			for( Type type :clazz.getGenericInterfaces()){
				if( PostMan.PostManRequest.class.equals(type) ){
					return (PostMan.PostManRequest)clazz.newInstance();
				}
			}

		}

		return null;
	}
}
