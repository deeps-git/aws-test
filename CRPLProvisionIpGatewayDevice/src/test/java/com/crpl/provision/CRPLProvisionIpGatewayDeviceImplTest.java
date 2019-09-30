package com.crpl.provision;

import com.amazonaws.services.lambda.runtime.Context;
import com.crpl.provision.errors.ProvisionStatus;
import com.crpl.support.PropertiesUtil;
import com.crpl.support.Util;
import com.crpl.support.attributes.ServiceAttributes;
import com.crpl.support.aws.lambda.LambdaUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.*;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class CRPLProvisionIpGatewayDeviceImplTest {

    static final Logger log = Logger.getLogger(CRPLProvisionIpGatewayDeviceImplTest.class);
    private static HashMap<String, Object> input;
    private static CRPLProvisionIpGatewayDeviceImpl crplProvisionIpGatewayDevice;
    private static LambdaUtil lambdaUtil;
    private static PropertiesUtil propertiesUtil;
    private static Gson gson;
    Type type = new TypeToken<Map<String, Object>>() {
    }.getType();


    @Before
    public void createInput() throws IOException {
        crplProvisionIpGatewayDevice = mock(CRPLProvisionIpGatewayDeviceImpl.class);
        lambdaUtil = mock(LambdaUtil.class);
        propertiesUtil = mock(PropertiesUtil.class);
        gson = new GsonBuilder().create();
        input = new HashMap<>();

        Whitebox.setInternalState(crplProvisionIpGatewayDevice, "lambdaUtil", lambdaUtil);
        Whitebox.setInternalState(crplProvisionIpGatewayDevice, "propertiesUtil", propertiesUtil);
        Whitebox.setInternalState(crplProvisionIpGatewayDevice, "gson", gson);
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        ctx.setFunctionName("CRPLProvisionIpGatewayDevice");

        return ctx;
    }


    @Test
    public void testGetEcmMac() {
        String expected = "whateverECMMAC";
        String status = "whateverStatus";

        Whitebox.setInternalState(crplProvisionIpGatewayDevice, "inputJson", new JsonParser().parse(getFieldScenarioSuccessPayload(status)));
        when(crplProvisionIpGatewayDevice.getEcmMac()).thenCallRealMethod();

        String actual = crplProvisionIpGatewayDevice.getEcmMac();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetStatusFromPayload() {
        String expected = "whateverStatus";

        Whitebox.setInternalState(crplProvisionIpGatewayDevice, "inputJson", new JsonParser().parse(getFieldScenarioSuccessPayload(expected)));
        when(crplProvisionIpGatewayDevice.getStatusFromPayload()).thenCallRealMethod();

        String actual = crplProvisionIpGatewayDevice.getStatusFromPayload();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testInitBillingAccountIdFromInputRequest() {
        String expected = "whateverBillingId";
        String status = "whateverStatus";

        Whitebox.setInternalState(crplProvisionIpGatewayDevice, "inputJson", new JsonParser().parse(getFieldScenarioSuccessPayload(status)));
        when(crplProvisionIpGatewayDevice.getBillingAccountIdFromInputRequest()).thenCallRealMethod();

        String actual = crplProvisionIpGatewayDevice.getBillingAccountIdFromInputRequest();
        Assert.assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateDeviceStatus() throws Exception {
        String ecmMAC = "whateverECMMAC";
        String status = "Active";

        Whitebox.setInternalState(crplProvisionIpGatewayDevice, "inputJson", new JsonParser().parse(getFieldScenarioSuccessPayload(status)));
        when(propertiesUtil.getProperty("crpl.pipgd.get.device.function.name")).thenReturn("whatever");

        //if device is delete status it shouldnt call anything, if it would it throws nullpointer
        when(lambdaUtil.getDeviceList(propertiesUtil.getProperty("crpl.pipgd.get.device.function.name"), null, "byECMMAC", ecmMAC)).thenReturn(getMockDeviceList("Delete"));
        when(crplProvisionIpGatewayDevice.updateDeviceStatus(ecmMAC, status)).thenCallRealMethod();

        Assert.assertTrue(crplProvisionIpGatewayDevice.updateDeviceStatus(ecmMAC, status));

        //if device is sameStatus as input it shouldnt call anything, if it would it throws nullpointer
        status = "PendingDelete";
        when(lambdaUtil.getDeviceList(propertiesUtil.getProperty("crpl.pipgd.get.device.function.name"), null, "byECMMAC", ecmMAC)).thenReturn(getMockDeviceList(status));
        when(crplProvisionIpGatewayDevice.updateDeviceStatus(ecmMAC, status)).thenCallRealMethod();

        Assert.assertTrue(crplProvisionIpGatewayDevice.updateDeviceStatus(ecmMAC, status));

        //it should go through, but will throw null pointer since cant mock local variable
        status = "Active";
        when(lambdaUtil.getDeviceList(propertiesUtil.getProperty("crpl.pipgd.get.device.function.name"), null, "byECMMAC", ecmMAC)).thenReturn(getMockDeviceList("PendingDelete"));
        when(crplProvisionIpGatewayDevice.updateDeviceStatus(ecmMAC, status)).thenCallRealMethod();

        when(lambdaUtil.updateDeviceStatus(propertiesUtil.getProperty("crpl.pipgd.update.device.status.function.name"), null, "whateverId", status)).thenReturn("Failed");
    }

    /**
     * false for failed provision
     * false for no payload returned in response
     * false for success key not there
     * false for any other exception thrown
     * true for success
     */
    @Test
    public void testSetServiceAccount() {
        Map<String, Object> provisionResponseMap = getProvisionDeviceSuccessResponse();
        when(crplProvisionIpGatewayDevice.setServiceAccount(provisionResponseMap)).thenCallRealMethod();

        Assert.assertTrue(crplProvisionIpGatewayDevice.setServiceAccount(provisionResponseMap));

        provisionResponseMap = Util.getMapRepresentationOfObject("{\"provisionDeviceResponse\":{\"success\":true}}");
        when(crplProvisionIpGatewayDevice.setServiceAccount(provisionResponseMap)).thenCallRealMethod();
        Assert.assertFalse(crplProvisionIpGatewayDevice.setServiceAccount(provisionResponseMap));

        provisionResponseMap = Util.getMapRepresentationOfObject("{\"provisionDeviceResponse\":{\"success\":false}}");
        when(crplProvisionIpGatewayDevice.setServiceAccount(provisionResponseMap)).thenCallRealMethod();
        Assert.assertFalse(crplProvisionIpGatewayDevice.setServiceAccount(provisionResponseMap));

        provisionResponseMap = Util.getMapRepresentationOfObject("{\"provisionDeviceResponse\":{\"sucwcess\":false}}");
        when(crplProvisionIpGatewayDevice.setServiceAccount(provisionResponseMap)).thenCallRealMethod();
        Assert.assertFalse(crplProvisionIpGatewayDevice.setServiceAccount(provisionResponseMap));

        provisionResponseMap = Util.getMapRepresentationOfObject("{\"provisionDeviceResponse\":{\"success\":true,\"payload\":\"null\"}}");
        when(crplProvisionIpGatewayDevice.setServiceAccount(provisionResponseMap)).thenCallRealMethod();
        Assert.assertFalse(crplProvisionIpGatewayDevice.setServiceAccount(provisionResponseMap));
    }


    /**
     * false for null serviceAccount
     * break flow for any other exception
     * true for success
     */
    @Test(expected = RuntimeException.class)
    public void testaddSmartInternetAccountProduct() {
        when(crplProvisionIpGatewayDevice.addSmartInternetAccountProduct()).thenCallRealMethod();

        Whitebox.setInternalState(crplProvisionIpGatewayDevice, "serviceAccount", "whateverId");
        Assert.assertTrue(crplProvisionIpGatewayDevice.addSmartInternetAccountProduct());

        when(crplProvisionIpGatewayDevice.addSmartInternetAccountProduct()).thenCallRealMethod();
        Assert.assertFalse(crplProvisionIpGatewayDevice.addSmartInternetAccountProduct());

        when(crplProvisionIpGatewayDevice.addSmartInternetAccountProduct()).thenThrow(NullPointerException.class);
        Assert.assertFalse(crplProvisionIpGatewayDevice.addSmartInternetAccountProduct());
    }

    /**
     * invalid account if cstauthguid is null
     * invalid status if status is not passed or null
     * for non-active device, success if update device is true else failed
     * for active device, if provisionDevice response doesnt have status or fails retrieving it then XBO_PROVISIONING_FAILED, if addSmartInternetAccountProduct fails then XBO_ADD_SMARTINTERNET_ACCOUNTPRODUCT_FAILED
     * else success
     */
    @Test
    public void testProcessRequest() throws Exception {
        String satToken = "whatever";
        String cstAuthGuid = "whatever";
        String status = "whateverStatus";
        Map<String, Object> inputMap = Util.getMapRepresentationOfObject(getFieldScenarioSuccessPayload(status));
        Whitebox.setInternalState(crplProvisionIpGatewayDevice, "inputJson", new JsonParser().parse(getFieldScenarioSuccessPayload(status)));
        when(lambdaUtil.getSatTokenForPartner(propertiesUtil.getProperty("crpl.pipgd.generate.sat.function.name"), null, "Comcast")).thenReturn(satToken);
        doCallRealMethod().when(lambdaUtil).setSatToken(satToken);
        when(crplProvisionIpGatewayDevice, "getBillingAccountIdFromInputRequest").thenCallRealMethod();
        when(crplProvisionIpGatewayDevice.processRequest(inputMap)).thenCallRealMethod();
//        doNothing().when(crplProvisionIpGatewayDevice,"updateInputWithAccountSourceId");

        ProvisionStatus temp = crplProvisionIpGatewayDevice.processRequest(inputMap);
        Assert.assertTrue(temp.equals(ProvisionStatus.INVALID_ACCOUNT));

        Whitebox.setInternalState(crplProvisionIpGatewayDevice, "cstAuthGuid", cstAuthGuid);
        temp = crplProvisionIpGatewayDevice.processRequest(inputMap);
        Assert.assertTrue(temp.equals(ProvisionStatus.INVALID_STATUS));

        //for non-active device
        when(crplProvisionIpGatewayDevice, "getStatusFromPayload").thenCallRealMethod();
        when(crplProvisionIpGatewayDevice.getEcmMac()).thenCallRealMethod();
        String ecmMAc = crplProvisionIpGatewayDevice.getEcmMac();
        when(crplProvisionIpGatewayDevice.updateDeviceStatus(ecmMAc, status)).thenReturn(true);
        doNothing().when(crplProvisionIpGatewayDevice).sendAccountRefreshNotification(cstAuthGuid);
        temp = crplProvisionIpGatewayDevice.processRequest(inputMap);
        Assert.assertTrue(temp.equals(ProvisionStatus.SUCCESS));

        when(crplProvisionIpGatewayDevice.updateDeviceStatus(ecmMAc, status)).thenReturn(false);
        temp = crplProvisionIpGatewayDevice.processRequest(inputMap);
        Assert.assertTrue(temp.equals(ProvisionStatus.FAILED));

        //for active device
        status = "active";
        Map<String, Object> provisionDeviceSuccessResponse = getProvisionDeviceSuccessResponse();
        inputMap = Util.getMapRepresentationOfObject(getFieldScenarioSuccessPayload(status));
        when(crplProvisionIpGatewayDevice.processRequest(inputMap)).thenCallRealMethod();
        when(crplProvisionIpGatewayDevice, "invokeProvisionDeviceLambdaFunction", inputMap).thenReturn(provisionDeviceSuccessResponse);
        when(crplProvisionIpGatewayDevice.setServiceAccount(provisionDeviceSuccessResponse)).thenReturn(false);
        temp = crplProvisionIpGatewayDevice.processRequest(inputMap);
        Assert.assertTrue(temp.equals(ProvisionStatus.XBO_PROVISIONING_FAILED));

        when(crplProvisionIpGatewayDevice.setServiceAccount(provisionDeviceSuccessResponse)).thenCallRealMethod();
        when(crplProvisionIpGatewayDevice.addSmartInternetAccountProduct()).thenReturn(false);
        temp = crplProvisionIpGatewayDevice.processRequest(inputMap);
        Assert.assertTrue(temp.equals(ProvisionStatus.XBO_ADD_SMARTINTERNET_ACCOUNTPRODUCT_FAILED));

        when(crplProvisionIpGatewayDevice.addSmartInternetAccountProduct()).thenReturn(true);
        temp = crplProvisionIpGatewayDevice.processRequest(inputMap);
        Assert.assertTrue(temp.equals(ProvisionStatus.SUCCESS));
    }


    private String getFieldScenarioSuccessPayload(String status) {
        StringBuilder payload = new StringBuilder();
        payload.append("{\"provisionDevice\": {\"acctContext\":");
        payload.append("{\"sourceId\": \"651544540415102015Comcast.RTVE\",\"source\": \"CET\",\"billingAccountId\": \"whateverBillingId\",");
        payload.append("\"deviceInfo\": {\"deviceType\": \"IpGateway\"," +
                "\"sourceId\": \"10:86:8C:45:82:78\",\"source\": \"CET\"," +
                "\"status\": \"" + status + "\"," +
                "\"eCMMAC\": \"whateverECMMAC\"," +
                "\"mocaMAC\": \"00:00:00:00:00\"," +
                "\"eMtaMAC\": \"10:86:8C:45:82:79\"," +
                "\"wanMAC\": \"10:86:8C:45:82:80\"," +
                "\"serialNumber\": \"F22BUE687702793\"," +
                "\"make\": \"Arris\",\"model\": \"TG1682G\"," +
                "\"hasDvr\": \"N\",\"hasQAM\": \"N\",\"numTuners\": \"0\"," +
                "\"dvrNumTuners\": \"0\",\"dvrCapacity\": \"0\",\"dvrCapacityUnits\": \"\"}");
        payload.append("}},\"cid\":\"crpl-test-ipgateway-provisioning-success-1\"}");
        return payload.toString();
    }

    /**
     * it will return list of mock devices of the status passed
     *
     * @return
     */
    private List<JsonObject> getMockDeviceList(String status) {
        ArrayList<JsonObject> jsonObjectArrayList = new ArrayList<>();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(ServiceAttributes.ID, "whateverId");
        jsonObject.addProperty(ServiceAttributes.STATUS, status);

        jsonObjectArrayList.add(jsonObject);
        return jsonObjectArrayList;
    }

    private Map<String, Object> getProvisionDeviceSuccessResponse() {
        return Util.getMapRepresentationOfObject("{\"provisionDeviceResponse\":{\"success\":true,\"payload\":\"{\\\"deviceId\\\":\\\"6475cb9d-6e42-4234-adf7-73869fb761b7\\\",\\\"partnerId\\\":\\\"Comcast\\\",\\\"deviceUri\\\":\\\"http://c3.dds2.xbo.ccp.xcal.tv:10240/deviceDataService/data/PhysicalDevice/9133585058516258540\\\",\\\"serviceAccountUri\\\":\\\"http://c3.ads2.xbo.ccp.xcal.tv:10249/serviceAccountDataService/data/ServiceAccount/529271822635744304\\\",\\\"responseVersion\\\":\\\"1.0\\\"}\"}}");
    }
}
