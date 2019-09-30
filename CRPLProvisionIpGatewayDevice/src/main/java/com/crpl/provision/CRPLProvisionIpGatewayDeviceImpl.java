/**
 *
 */
package com.crpl.provision;

import com.crpl.provision.errors.ProvisionStatus;
import com.crpl.support.PropertiesUtil;
import com.crpl.support.Util;
import com.crpl.support.attributes.ServiceAttributes;
import com.crpl.support.aws.AwsUtil;
import com.crpl.support.aws.lambda.LambdaUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.crpl.support.attributes.ServiceAttributes.SAT_TOKEN;

/**
 * @author radari200
 */
public class CRPLProvisionIpGatewayDeviceImpl {
    static final Logger log = Logger.getLogger(CRPLProvisionIpGatewayDeviceImpl.class);
    private static PropertiesUtil propertiesUtil;
    private static final String DELETE = "Delete";
    private Gson gson = new GsonBuilder().create();
    private String billingAccountId = null;
    private JsonObject inputJson;
    private String cid;
    private String cstAuthGuid = null;
    private String serviceAccount = null;
    private static LambdaUtil lambdaUtil;
    private static AwsUtil awsUtil;

    static {
        lambdaUtil = new LambdaUtil();
        awsUtil = AwsUtil.getInstance();
        propertiesUtil = PropertiesUtil.getInstance();
    }

    public CRPLProvisionIpGatewayDeviceImpl(String cid) {
        this.cid = cid;
    }

    public ProvisionStatus processRequest(Map<String, Object> input) {
        try {
            // Convert input to json.
            inputJson = new JsonParser().parse(Util.getStringRepresentationOfObject(input)).getAsJsonObject();

            //getSAT
            lambdaUtil.setSatToken(lambdaUtil.getSatTokenForPartner(propertiesUtil.getProperty("crpl.pipgd.generate.sat.function.name"), cid, "Comcast"));
            // Initialize the billingAccountID
            billingAccountId = getBillingAccountIdFromInputRequest();

            // Invoke CET getAccountInformationFromES to fetch the account
            // sourceId
            // and update the input map
            updateInputWithAccountSourceId();

            if (cstAuthGuid == null) {
                return ProvisionStatus.INVALID_ACCOUNT;
            }

            log.info("cstAuthGuid from CET call=" + cstAuthGuid);

            String status = getStatusFromPayload();
            if (status == null || status.trim().length() <= 0) {
                return ProvisionStatus.INVALID_STATUS;
            }

            if (status.equalsIgnoreCase("active")) {
                // Invoke Provision Device Lambda function
                // Convert the json to a map to pass to ProvisionDevice
                // Invoke deviceProvision API to provision a device.
                // if provision device response is successful, then add
                // "smartinternet"
                // account product to the account Id
                System.out.println(inputJson.toString());
                Type type = new TypeToken<Map<String, Object>>() {
                }.getType();
                Map<String, Object> deviceProvisionInputMap = gson.fromJson(inputJson.toString(), type);
                Map<String, Object> provisionResponseMap = invokeProvisionDeviceLambdaFunction(deviceProvisionInputMap);
                if (!setServiceAccount(provisionResponseMap)) {
                    return ProvisionStatus.XBO_PROVISIONING_FAILED;
                }
                if (!addSmartInternetAccountProduct()) {
                    return ProvisionStatus.XBO_ADD_SMARTINTERNET_ACCOUNTPRODUCT_FAILED;
                }
            } else {
                log.info("Non Active Device Status=" + status + " received. Performing device status update and refresh");
                String ecmMac = getEcmMac();
                if (!updateDeviceStatus(ecmMac, status)) {
                    log.error("Failed to update status on one or more device");
                    return ProvisionStatus.FAILED;
                }
            }
//            runARefreshAfterDelaySinceXfiDoesntKnowHowToHandlePD();
//            sendAccountRefreshNotification(cstAuthGuid);
            return ProvisionStatus.SUCCESS;
        } catch (Exception e) {
            log.error("Exception while processing the request", e);
            e.printStackTrace();
        }
        return ProvisionStatus.FAILED;
    }

    protected String getStatusFromPayload() {
        return inputJson.getAsJsonObject("provisionDevice").getAsJsonObject("acctContext").getAsJsonObject("deviceInfo").get("status").getAsString();
    }

    protected String getEcmMac() {
        return inputJson.getAsJsonObject("provisionDevice").getAsJsonObject("acctContext").getAsJsonObject("deviceInfo").get("eCMMAC").getAsString();
    }

    protected boolean updateDeviceStatus(String ecmMac, String status) throws Exception {
        boolean result = true;
        List<JsonObject> devices = lambdaUtil.getDeviceList(propertiesUtil.getProperty("crpl.pipgd.get.device.function.name"), cid, "byECMMAC", ecmMac);
        for (JsonObject device : devices) {
            String deviceStatus = device.get("status").getAsString();
            String deviceId = device.get("id").getAsString();
            if (!DELETE.equals(deviceStatus) && !status.equals(deviceStatus)) {
                log.info("Updating device status to " + status + " for device ID = " + deviceId);
                String response = lambdaUtil.updateDeviceStatus(propertiesUtil.getProperty("crpl.pipgd.update.device.status.function.name"), cid, deviceId, status);
                log.info("Device Update Status = " + response);
                if (response.contains("Failed")) {
                    log.info("Device Status update failed for device id = " + deviceId);
                    result = false;
                }
            } else {
                log.info("Device " + deviceId + " is already in Delete Status or Status in payload is same as current device status. Device Status = " + deviceStatus);
            }
        }
        return result;
    }

    protected String getBillingAccountIdFromInputRequest() {
        return inputJson.getAsJsonObject("provisionDevice").getAsJsonObject("acctContext").get("billingAccountId").getAsString();
    }

    private void updateInputWithAccountSourceId() {
        String getAccountInfoResponse = lambdaUtil.getAccountInfoFromCet(propertiesUtil.getProperty("crpl.pipgd.get.account.cet.function.name"), cid, billingAccountId);
        if (getAccountInfoResponse != null) {
            JsonObject getAccountJson = new JsonParser().parse(getAccountInfoResponse).getAsJsonObject();
            cstAuthGuid = getAccountJson.getAsJsonObject("getEntitlementInfoResponse").getAsJsonObject("account").get("sourceId").getAsString();
            log.info("got Account SourceId=" + cstAuthGuid + " from CET ES");
            inputJson.getAsJsonObject("provisionDevice").getAsJsonObject("acctContext").addProperty("sourceId", cstAuthGuid);
        }
    }

    protected void sendAccountRefreshNotification(String cstAuthGuid) {
        try {
            String message = "{\"accountSourceId\":\"" + cstAuthGuid + "\",\"cid\":\"" + cid + "\"}";
            log.info("Sending message=" + message + " to refresh");
            String refreshTopicArn = propertiesUtil.getPropertyWithoutLogging("crpl.pipgd.account.refresh.topic");
            String env = System.getenv(ServiceAttributes.AWS_REGION);
            refreshTopicArn = refreshTopicArn.replaceAll("us-east-1", env.toLowerCase());
            message = Util.encodeMessage(message);
            awsUtil.sendSNSNotification(refreshTopicArn, message);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception parsing the input request. Exception=" + e.toString());
        }
    }

    protected boolean addSmartInternetAccountProduct() {
        if (serviceAccount != null) {
            String serviceAccountId = "";
            if (serviceAccount.contains("/")) {
                serviceAccountId = serviceAccount.substring(serviceAccount.lastIndexOf("/") + 1);
            } else {
                serviceAccountId = serviceAccount;
            }
            String addAccountProductPayload = "{\"accountProduct\": [{\"accountProductID\": \"smartinternet\",\"source\": \"xbo\",\"serviceAccountId\": \""
                    + serviceAccountId + "\"}]}";
            log.info("addAccountProductPayload=" + addAccountProductPayload);
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> addAccountProductInputMap = gson.fromJson(addAccountProductPayload, type);
            return invokeAddAccountProductPayload(addAccountProductInputMap);
        }
        log.info("No ServiceAccount found in the response. halting execution");
        return false;
    }

    protected boolean setServiceAccount(Map<String, Object> provisionResponseMap) {
        if (provisionResponseMap.containsKey("provisionDeviceResponse")) {
            Map<String, Object> provisionDeviceResponseMap = (Map<String, Object>) provisionResponseMap.get("provisionDeviceResponse");
            if (provisionDeviceResponseMap.containsKey("success")) {
                Boolean provisionResponseStatus = (Boolean) provisionDeviceResponseMap.get("success");
                if (provisionResponseStatus) {
                    if (provisionDeviceResponseMap.containsKey("payload")) {
                        String payloadMap = (String) provisionDeviceResponseMap.get("payload");
                        try {
                            JsonObject jsonPayload = new JsonParser().parse(payloadMap).getAsJsonObject();
                            serviceAccount = jsonPayload.get("serviceAccountUri").getAsString();
                            log.info("retrieved serviceAccount from the input Map. serviceAccountId=" + serviceAccount);
                            return true;
                        } catch (Exception e) {
                            log.error("Caught Json Exception while retriving Service Account ID");
                            e.printStackTrace();
                        }
                    } else {
                        log.info("Provision Response missing payloadMap");
                    }
                } else {
                    log.info("Provision Response failed");
                }
            } else {
                log.info("Provision Response does not contain provisionDeviceResponseMap");
            }
        }
        return false;
    }

    private Boolean invokeAddAccountProductPayload(Map<String, Object> addAccountProductInputMap) {
        addAccountProductInputMap.put(SAT_TOKEN, lambdaUtil.getSatToken());
        Map<String, Object> addAccountProductResponse = lambdaUtil.addAccountProduct(propertiesUtil.getProperty("crpl.pipgd.add.account.product.function.name"), addAccountProductInputMap);
        log.info("Add AccountProduct Response=" + addAccountProductResponse.toString());
        return (Boolean) addAccountProductResponse.get("success");
    }

    private Map<String, Object> invokeProvisionDeviceLambdaFunction(Map<String, Object> deviceProvisionInputMap) {
        deviceProvisionInputMap.put(SAT_TOKEN, lambdaUtil.getSatToken());
        Map<String, Object> response = lambdaUtil.provisionCRPLDevice(propertiesUtil.getProperty("crpl.pipgd.provision.device.function.name"), deviceProvisionInputMap);
        log.info("Provision device lambda response=" + response.toString());
        return response;
    }

    public String getServiceAccount() {
        return serviceAccount;
    }


    private void runARefreshAfterDelaySinceXfiDoesntKnowHowToHandlePD() throws InterruptedException {
        //putting in a delay that I didn't agree to put
        log.info("mhm====DELAY====mhm");
        TimeUnit.SECONDS.sleep(10);
    }
}
