package io.lcs.framework.api.annotation;


import java.lang.annotation.*;

/**
 * Created by lcs on 02/03/2017.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiInfo {
	/**
	 * 始于发布当前api的版本
	 */
	String since() default "";

	/**
	 * 说明
	 * @return
	 */
	String summary() default "";

	/**
	 * 当前api将于此版本移弃
	 */
	String deprecated() default "";

	/**
	 * 是否已经实现
	 */
	boolean hasImplemented() default true;

	/**
	 * 参数列表
	 * @return
	 */
	ApiParam[] value() default {};


	String VERSION_1 = "1.0.0";

}
