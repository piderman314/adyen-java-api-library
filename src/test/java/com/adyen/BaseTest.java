package com.adyen;

import com.adyen.enums.VatCategory;
import com.adyen.httpclient.HTTPClientException;
import com.adyen.httpclient.HttpURLConnectionClient;
import com.adyen.model.*;
import com.adyen.model.additionalData.InvoiceLine;
import com.adyen.model.modification.AbstractModificationRequest;
import com.adyen.model.modification.CaptureRequest;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class BaseTest {
    /**
     * Returns a Client object that has a mocked response
     *
     * @param response
     * @return
     */
    protected Client createMockClientFromResponse(String response) {
        HttpURLConnectionClient httpURLConnectionClient = mock(HttpURLConnectionClient.class);
        try {
            when(httpURLConnectionClient.
                    request(any(String.class), any(String.class), any(Config.class))).
                    thenReturn(response);

            when(httpURLConnectionClient.
                    post(any(String.class), any(Map.class), any(Config.class))).
                    thenReturn(response);
        } catch (IOException | HTTPClientException e) {
            e.printStackTrace();
        }
        Client client = new Client();
        client.setHttpClient(httpURLConnectionClient);

        Config config = new Config();
        config.setHmacKey("DFB1EB5485895CFA84146406857104ABB4CBCABDC8AAF103A624C8F6A3EAAB00");
        client.setConfig(config);

        return client;
    }

    /**
     * Returns a Client object that has a mocked response from fileName
     *
     * @param fileName
     * @return
     */
    protected Client createMockClientFromFile(String fileName) {
        String response = getFileContents(fileName);

        return createMockClientFromResponse(response);
    }

    /**
     * Helper for file reading
     *
     * @param fileName
     * @return
     */
    public String getFileContents(String fileName) {
        String result = "";

        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Populates the basic parameters (browser data, merchant account, shopper IP)
     *
     * @param abstractPaymentRequest
     * @param <T>
     * @return
     */
    protected <T extends AbstractPaymentRequest> T createBasePaymentRequest(T abstractPaymentRequest) {
        abstractPaymentRequest
                .merchantAccount("AMerchant")
                .setBrowserInfoData(
                        "User-Agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36",
                        "*/*"
                )
                .setShopperIP("1.2.3.4");

        return abstractPaymentRequest;
    }

    /**
     * Returns a sample PaymentRequest opbject with full card data
     *
     * @return
     */
    protected PaymentRequest createFullCardPaymentRequest() {
        PaymentRequest paymentRequest = createBasePaymentRequest(new PaymentRequest())
                .reference("123456")
                .setAmountData(
                        "1000",
                        "EUR"
                )
                .setCardData(
                        "5136333333333335",
                        "John Doe",
                        "08",
                        "2018",
                        "737"
                );

        return paymentRequest;
    }

    /**
     *  Returns a sample PaymentRequest opbject with full OpenInvoice request
     *
     * @return
     */
    protected PaymentRequest createOpenInvoicePaymentRequest() {

        Date dateOfBirth = null;
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            dateOfBirth = fmt.parse("1970-07-10");
        } catch (Exception e) {
        }

        PaymentRequest paymentRequest = createBasePaymentRequest(new PaymentRequest())
                .reference("123456")
                .setAmountData(
                        "200",
                        "EUR"
                );

        // Set Shopper Data
        paymentRequest.setShopperEmail("youremail@email.com");
        paymentRequest.setDateOfBirth(dateOfBirth);
        paymentRequest.setTelephoneNumber("0612345678");
        paymentRequest.setShopperReference("4");

        // Set Shopper Info
        Name shopperName = new Name();
        shopperName.setFirstName("Testperson-nl");
        shopperName.setLastName("Approved");
        shopperName.gender(Name.GenderEnum.MALE);
        paymentRequest.setShopperName(shopperName);

        // Set Billing and Delivery address
        Address address = new Address();
        address.setCity("Gravenhage");
        address.setCountry("NL");
        address.setHouseNumberOrName("1");
        address.setPostalCode("2521VA");
        address.setStateOrProvince("Zuid-Holland");
        address.setStreet("Neherkade");
        paymentRequest.setDeliveryAddress(address);
        paymentRequest.setBillingAddress(address);

        // Use OpenInvoice Provider (klarna, ratepay)
        paymentRequest.selectedBrand("klarna");

        Long itemAmount = new Long("9000");
        Long itemVatAmount = new Long("1000");
        Long itemVatPercentage = new Long("1000");

        List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();

        // invoiceLine1
        InvoiceLine invoiceLine = new InvoiceLine();
        invoiceLine.setCurrencyCode("EUR");
        invoiceLine.setDescription("Test product");
        invoiceLine.setItemAmount(itemAmount);
        invoiceLine.setItemVATAmount(itemVatAmount);
        invoiceLine.setItemVatPercentage(itemVatPercentage);
        invoiceLine.setVatCategory(VatCategory.NONE);
        invoiceLine.setNumberOfItems(1);
        invoiceLine.setItemId("1234");

        // invoiceLine2
        InvoiceLine invoiceLine2 = new InvoiceLine();
        invoiceLine2.setCurrencyCode("EUR");
        invoiceLine2.setDescription("Test product 2");
        invoiceLine2.setItemAmount(itemAmount);
        invoiceLine2.setItemVATAmount(itemVatAmount);
        invoiceLine2.setItemVatPercentage(itemVatPercentage);
        invoiceLine2.setVatCategory(VatCategory.NONE);
        invoiceLine2.setNumberOfItems(1);
        invoiceLine2.setItemId("4567");

        invoiceLines.add(invoiceLine);
        invoiceLines.add(invoiceLine2);

        paymentRequest.setInvoiceLines(invoiceLines);

        return paymentRequest;
    }

    /**
     * Returns a sample PaymentRequest object with CSE data
     *
     * @return
     */
    protected PaymentRequest createCSEPaymentRequest() {
        PaymentRequest paymentRequest = createBasePaymentRequest(new PaymentRequest())
                .reference("123456")
                .setAmountData("1000", "EUR")
                .setCSEToken("adyenjs_0_1_4p1$...");

        return paymentRequest;
    }

    /**
     * Returns a PaymentRequest3d object for 3D secure authorisation
     *
     * @return
     */
    protected PaymentRequest3d create3DPaymentRequest() {
        PaymentRequest3d paymentRequest3d = createBasePaymentRequest(new PaymentRequest3d())
                .set3DRequestData("mdString", "paResString");

        return paymentRequest3d;
    }

    /**
     * Returns a Client that has a mocked error response from fileName
     *
     * @param status
     * @param fileName
     * @return
     */
    protected Client createMockClientForErrors(int status, String fileName) {
        String response = getFileContents(fileName);

        HttpURLConnectionClient httpURLConnectionClient = mock(HttpURLConnectionClient.class);
        HTTPClientException httpClientException = new HTTPClientException(
                status,
                "An error occured",
                new HashMap<String, List<String>>(),
                response);
        try {
            when(httpURLConnectionClient.
                    request(any(String.class), any(String.class), any(Config.class))).
                    thenThrow(httpClientException);
        } catch (IOException | HTTPClientException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        Client client = new Client();
        client.setHttpClient(httpURLConnectionClient);
        return client;
    }

    protected <T extends AbstractModificationRequest> T createBaseModificationRequest(T modificationRequest) {
        modificationRequest
                .merchantAccount("AMerchant")
                .originalReference("originalReference")
                .reference("merchantReference");

        return modificationRequest;
    }

    protected CaptureRequest createCaptureRequest() {
        CaptureRequest captureRequest = createBaseModificationRequest(new CaptureRequest());

        captureRequest
                .fillAmount(
                        "15.00",
                        "EUR"
                );

        return captureRequest;
    }
}
