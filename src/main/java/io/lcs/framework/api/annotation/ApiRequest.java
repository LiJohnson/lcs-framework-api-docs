package io.lcs.framework.api.annotation;

import java.lang.annotation.*;

/**
 * Created by lcs on 20/03/2017.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiRequest {

	/**
	 * 参数列表
	 * @return
	 */
	ApiParam[] value() default {};
}
