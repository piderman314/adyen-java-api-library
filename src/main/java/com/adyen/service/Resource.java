package com.adyen.service;

import com.adyen.Config;
import com.adyen.Service;
import com.adyen.Util.Util;
import com.adyen.httpclient.ClientInterface;
import com.adyen.httpclient.HTTPClientException;
import com.adyen.model.ApiError;
import com.adyen.service.exception.ApiException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.*;

public class Resource {

    private Service service;
    protected String endpoint;
    protected List<String> requiredFields;

    protected static final Gson GSON = new Gson();

    public Resource(Service service, String endpoint, List<String> requiredFields) {
        this.service = service;
        this.endpoint = endpoint;
        this.requiredFields = requiredFields;
    }

    public Map<String, Object> request(Map<String, Object> params) throws Exception {
        // validate parameters
        // TODO: build valdiation for dots (reflection used for Gson)
//    	this.validate(params);
        ClientInterface clientInterface = (ClientInterface) this.service.getClient().getHttpClient();
        Map<String, Object> result = clientInterface.requestJson(this.service, this.endpoint, params);
        return result;
    }

    /**
     * Request using json String
     *
     * @param json
     * @return
     * @throws ApiException
     * @throws IOException
     */
    public String request(String json) throws ApiException, IOException {
        ClientInterface clientInterface = (ClientInterface) this.service.getClient().getHttpClient();
        Config config = this.service.getClient().getConfig();
        String result = null;
        try {
            result = clientInterface.request(this.endpoint, json, config);
        } catch (HTTPClientException e) {
            String responseBody = e.getResponseBody();

            ApiError apiError = GSON.fromJson(responseBody, new TypeToken<ApiError>() {
            }.getType());
            ApiException apiException = new ApiException(e.getMessage(), e.getCode());
            apiException.setError(apiError);

            throw apiException;
        }
        return result;
    }

    // Validate needs to be done by gson the json to an object. Object only contains valdiate fields to validate and use the json to put to the server to adyen
    protected void validate(Map<String, Object> params) throws Exception {
        List<String> missingFields = new ArrayList<String>();
        List<String> missingValues = new ArrayList<String>();

        if (!this.requiredFields.isEmpty()) {
            for (String requiredField : this.requiredFields) {
                // if validation is two levels validate if parent and child is in the request
                if (requiredField.contains(".")) {

                    List<String> items = Arrays.asList(requiredField.split("\\."));
                    String parent = items.get(0);
                    String child = items.get(1);

                    if (!params.containsKey(parent)) {
                        missingFields.add(requiredField);
                        continue;
                    }

                    // cast to hashmap
                    HashMap<String, Object> result = (HashMap<String, Object>) params.get(parent);

                    if (!result.containsKey(child)) {
                        missingFields.add(requiredField);
                        continue;
                    }

                    // check if param has value toString() could lead to exception if there is no value set
                    try {
                        if (result.get(child).toString().isEmpty()) {
                            missingValues.add(requiredField);
                        }
                    } catch (Exception e) {
                        missingValues.add(requiredField);
                    }
                } else {
                    if (!params.containsKey(requiredField)) {
                        missingFields.add(requiredField);
                    } else {
                        // check if field has value
                        if (params.get(requiredField).toString().isEmpty()) {
                            missingValues.add(requiredField);
                        }
                    }
                }
            }
        }

        if (!missingFields.isEmpty()) {
            String msg = "Missing the following fields: " + Util.implode(",", missingFields);
            throw new Exception(msg);
        }

        if (!missingValues.isEmpty()) {
            String msg = "Missing the following values: " + Util.implode(",", missingValues);
            throw new Exception(msg);
        }
    }
}
