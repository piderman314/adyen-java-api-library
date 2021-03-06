package com.adyen.service;

import com.adyen.Client;
import com.adyen.Config;
import com.adyen.Service;
import com.adyen.Util.HMACValidator;
import com.adyen.httpclient.ClientInterface;
import com.adyen.httpclient.HTTPClientException;
import com.adyen.model.hpp.DirectoryLookupRequest;
import com.adyen.model.hpp.DirectoryLookupResult;
import com.adyen.model.hpp.PaymentMethod;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.security.SignatureException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.adyen.constants.HPPConstants.Fields.*;

public class HostedPaymentPages extends Service {
    public HostedPaymentPages(Client client) {
        super(client);
    }

    public String directoryLookup(Map<String, String> postParameters) throws HTTPClientException, IOException {
        String endpoint = this.getClient().getConfig().getHppEndpoint() + "/directory.shtml";
        ClientInterface httpClient = (ClientInterface) this.getClient().getHttpClient();
        Config config = this.getClient().getConfig();

        return httpClient.post(endpoint, postParameters, config);
    }

    public SortedMap<String, String> getPostParametersFromDLRequest(DirectoryLookupRequest request) throws SignatureException {
        Config config = this.getClient().getConfig();

        // Set HTTP Post variables
        final SortedMap<String, String> postParameters = new TreeMap<>();
        postParameters.put(CURRENCY_CODE, request.getCurrencyCode());
        postParameters.put(MERCHANT_ACCOUNT, config.getMerchantAccount());
        postParameters.put(PAYMENT_AMOUNT, request.getPaymentAmount());
        postParameters.put(SKIN_CODE, config.getSkinCode());
        postParameters.put(MERCHANT_REFERENCE, request.getMerchantReference());
        postParameters.put(SESSION_VALIDITY, request.getSessionValidity());
        postParameters.put(COUNTRY_CODE, request.getCountryCode());

        HMACValidator hmacValidator = new HMACValidator();

        String dataToSign = hmacValidator.getDataToSign(postParameters);

        String merchantSig = hmacValidator.calculateHMAC(dataToSign, config.getHmacKey());
        postParameters.put(MERCHANT_SIG, merchantSig);

        return postParameters;
    }

    public List<PaymentMethod> getPaymentMethods(DirectoryLookupRequest request)
            throws SignatureException, IOException, HTTPClientException {
        final SortedMap<String, String> postParameters = getPostParametersFromDLRequest(request);

        String jsonResult = directoryLookup(postParameters);

        DirectoryLookupResult directoryLookupResult = GSON.fromJson(jsonResult, new TypeToken<DirectoryLookupResult>() {
        }.getType());

        return directoryLookupResult.getPaymentMethods();
    }
}