package io.lcs.framework.api.docs;


import io.lcs.framework.api.annotation.ApiInfo;
import io.lcs.framework.api.annotation.ApiResponse;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by lcs on 02/03/2017.
 */
public class ApiScanner {
	private static final String methodMetadataClass = "classpath*:/io/lcs/**/*.class";
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

		Resource[] e = resourcePatternResolver.getResources(methodMetadataClass);
		List<Api> list = new ArrayList<>();
		for (Resource r : e) {
			MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(r);
			AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();

			Class clazz = Class.forName(annotationMetadata.getClassName());

			Map<String, Object> classRequestMapping = annotationMetadata.getAnnotationAttributes(RequestMapping.class.getName());

			for (Method method : clazz.getDeclaredMethods()) {
				RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
				if( requestMapping == null ) continue;

				ApiResponse apiResponse = method.getAnnotation(ApiResponse.class);

				Api api = new Api();
				api.setInfo(method.getAnnotation(ApiInfo.class));
				api.setApi(getApis(classRequestMapping, requestMapping.value()));
				if( apiResponse != null && StringUtils.hasLength(apiResponse.demo())){
					api.setResponseDemo(apiResponse.demo());
				}

				list.add(api);
			}
		}

		Collections.sort(list,new Comparator<Api>() {
			@Override
			public int compare(Api o1, Api o2) {
				return 0;
				//return o1.getApi()[0].compareTo(o2.getApi()[0]);
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
		Resource[] e = resourcePatternResolver.getResources(methodMetadataClass);
		Map<Class, Object[]> map = new HashMap<>();
		for (Resource r : e) {
			MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(r);
			AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
			Class clazz = Class.forName(annotationMetadata.getClassName());
			for (Class c : clazz.getInterfaces()) {
				if (c.getName().endsWith("Constants$Constant")) {
					Object[] values = (Object[]) clazz.getMethod("values").invoke(null);
					map.put(clazz, values);
				}
			}
		}
		return map;
	}
}
