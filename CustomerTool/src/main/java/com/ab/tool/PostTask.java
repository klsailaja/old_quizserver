package com.ab.tool;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class PostTask<T,R> {
    private final String reqUri;
    private final int requestId;
    private int reqTimeOut = 20 * 1000;
    private T postObject;
    private final Class<R> classType;
    private Object helperObject;

    public PostTask(String reqUri, int reqId, T postObject, Class<R> classType) {
        this.reqUri = reqUri;
        this.requestId = reqId;
        this.postObject = postObject;
        this.classType = classType;
    }

    public int getRequestId() {
        return requestId;
    }

    public String getReqUri() {
        return reqUri;
    }

    public void setPostObject(T postObject) {
        this.postObject = postObject;
    }

    public void setReqTimeOut(int timeOut) {
        this.reqTimeOut = timeOut;
    }

    public void setHelperObject(Object helperObject) {
        this.helperObject = helperObject;
    }
    public Object getHelperObject() {
        return this.helperObject;
    }

    public HttpEntity<?> getHttpEntity(List<MediaType> acceptableMediaTypes) {
        if (acceptableMediaTypes == null) {
            acceptableMediaTypes = new ArrayList<>();
            acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        }
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(acceptableMediaTypes);

        // Populate the headers in an HttpEntity object to use for the request
        return new HttpEntity<>(postObject, requestHeaders);
    }

    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        SimpleClientHttpRequestFactory rf =
        	    (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
        rf.setReadTimeout(reqTimeOut);
        rf.setConnectTimeout(reqTimeOut);
        return restTemplate;
    }
    
    public Object execute() throws Exception {
    	RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<R> responseEntity = restTemplate.exchange(getReqUri(), HttpMethod.POST, getHttpEntity(null),
                classType);
        Object result = responseEntity.getBody();
        return result;
    }
}
