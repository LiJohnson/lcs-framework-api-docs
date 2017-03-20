package io.lcs.framework.api.docs;

import io.lcs.framework.api.annotation.ApiInfo;
import io.lcs.framework.api.annotation.ApiParam;
import io.lcs.framework.api.annotation.ApiRequest;
import io.lcs.framework.api.annotation.ApiResponse;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lcs on 02/03/2017.
 */
public class Api {
	private String name;
	private String[] api;
	private ApiInfo info;
	private ApiRequest apiRequest;
	private ApiResponse apiResponse;
	private String paramDemo;
	private List<ApiParam> response;
	private String responseDemo;


	public Api(){
		this.response = new ArrayList();
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getApi() {
		return api;
	}

	public void setApi(String[] api) {
		this.api = api;
	}

	public String getParamDemo() {
		return paramDemo;
	}

	public void setParamDemo(String paramDemo) {
		this.paramDemo = paramDemo;
	}

	public List<ApiParam> getResponse() {
		return response;
	}

	public void setResponse(List<ApiParam> response) {
		this.response = response;
	}

	public String getResponseDemo() {
		return responseDemo;
	}

	public void setResponseDemo(String responseDemo) {
		this.responseDemo = responseDemo;
	}

	public ApiInfo getInfo() {
		return info;
	}

	public void setInfo(ApiInfo info) {
		this.info = info;
	}

	public ApiRequest getApiRequest() {
		return apiRequest;
	}

	public void setApiRequest(ApiRequest apiRequest) {
		this.apiRequest = apiRequest;
	}

	public ApiResponse getApiResponse() {
		return apiResponse;
	}

	public void setApiResponse(ApiResponse apiResponse) {
		this.apiResponse = apiResponse;
	}

	public static class ApiParamImp implements ApiParam {
		private Class typeImp = Object.class;
		private String valueImp = "";
		private String descriptionImp = "";
		private String demoImp = "";
		private boolean requiredImp = false;

		@Override
		public Class type() {
			return this.typeImp;
		}

		@Override
		public String value() {
			return this.valueImp;
		}

		@Override
		public String description() {
			return this.descriptionImp;
		}

		@Override
		public String demo() {
			return this.demoImp;
		}

		@Override
		public boolean required() {
			return this.requiredImp;
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return ApiParam.class;
		}

		public ApiParamImp type(Class type) {
			this.typeImp = type;
			return this;
		}

		public ApiParamImp value(String value) {
			this.valueImp = value;
			return this;
		}


		public ApiParamImp description(String descriptionImp) {
			this.descriptionImp = descriptionImp;
			return this;
		}


		public ApiParamImp demo( String demoImp) {
			this.demoImp = demoImp;
			return this;
		}

		public ApiParamImp required(boolean requiredImp) {
			this.requiredImp = requiredImp;
			return this;
		}
	}
}
