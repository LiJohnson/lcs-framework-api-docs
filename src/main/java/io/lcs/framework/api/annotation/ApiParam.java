package io.lcs.framework.api.annotation;

import java.lang.annotation.*;

/**
 * Created by lcs on 02/03/2017.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiParam {
	/**
	 * 数据类型
	 * @return
	 */
	Class type() default Object.class;

	/**
	 * 参数名
	 * @return
	 */
	String value() default "";

	/**
	 * 说明
	 * @return
	 */
	String description() default "";

	/**
	 * 示例
	 * @return
	 */
	String demo() default "";

	/**
	 * 必填
	 * @return
	 */
	boolean required() default false;

}
