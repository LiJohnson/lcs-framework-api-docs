package io.lcs.framework.api.annotation;

import java.lang.annotation.*;

/**
 * Created by lcs on 02/03/2017.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiResponse {
	/**
	 * 数据类型
	 * @return
	 */
	EType type();

	/**
	 * 返回实体
	 * @return
	 */
	Class bean() default Object.class;

	/**
	 * 返回参数
	 * @return
	 */
	ApiParam[] value() default {};

	/**
	 * @return示例
	 */
	String demo() default "";

	enum EType{
		OBJECT, ARRAY,PAGE
	}
}
