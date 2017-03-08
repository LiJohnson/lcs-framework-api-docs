package io.lcs.framework.api.docs;

import io.lcs.framework.api.annotation.ApiInfo;
import io.lcs.framework.api.annotation.ApiParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lcs on 02/03/2017.
 */
public class Api {
	private String name;
	private String[] api;
	private ApiInfo info;
	private String paramDemo;
	private List response;
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

	public List getResponse() {
		return response;
	}

	public void setResponse(List response) {
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
}
