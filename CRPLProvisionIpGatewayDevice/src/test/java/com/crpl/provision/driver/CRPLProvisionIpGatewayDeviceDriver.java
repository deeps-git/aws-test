package com.crpl.provision.driver;

import com.amazonaws.services.lambda.runtime.Context;
import com.crpl.provision.CRPLProvisionIpGatewayDeviceHandler;
import com.crpl.provision.TestContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@Ignore
public class CRPLProvisionIpGatewayDeviceDriver {

    static final Logger log = Logger.getLogger(CRPLProvisionIpGatewayDeviceHandler.class);
    private static Map<String, Object> input;
    Gson gson = new GsonBuilder().create();
    Type type = new TypeToken<Map<String, Object>>() {
    }.getType();


    @BeforeClass
    public static void createInput() throws IOException {
        input = null;
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testProvisionIpGatewayDevice() {
        CRPLProvisionIpGatewayDeviceHandler handler = new CRPLProvisionIpGatewayDeviceHandler();
        Context ctx = createContext();

        //Success Use Case
        String testSuccessUseCase = getFieldScenarioSuccessPayload();

        input = gson.fromJson(testSuccessUseCase, type);
        Map<String, Object> output = handler.handleRequest(input, ctx);

        if (output != null) {
            Assert.assertTrue(output.containsKey("success") && (boolean) output.get("success"));
        } else {
            Assert.fail();
        }


    }

    @Test
    public void testProvisionIpGatewayDeviceForActive() {
        CRPLProvisionIpGatewayDeviceHandler handler = new CRPLProvisionIpGatewayDeviceHandler();
        Context ctx = createContext();
        //Pending Delete Use case
        String activeSuccessPayload = getActiveSuccessPayload();

        input = gson.fromJson(activeSuccessPayload, type);
        Map<String, Object> output = handler.handleRequest(input, ctx);

        if (output != null) {
            Assert.assertTrue(output.containsKey("success") && (boolean) output.get("success"));
        } else {
            Assert.fail();
        }

        //Pending Delete Use case
        String activeBadAccountPayload = getActiveBadAccountPayload();

        input = gson.fromJson(activeBadAccountPayload, type);
        output = handler.handleRequest(input, ctx);

        if (output != null) {
            Assert.assertTrue(output.containsKey("success") && !(boolean) output.get("success"));
        } else {
            Assert.fail();
        }
    }

    @Test
    public void testProvisionIpGatewayDeviceForBadAccount() {
        CRPLProvisionIpGatewayDeviceHandler handler = new CRPLProvisionIpGatewayDeviceHandler();
        Context ctx = createContext();

        //Missing or BAD account sourceId
        String testAccountSourceMissing = getBadAccountPayloadWrongSourceAndBilling();
        input = gson.fromJson(testAccountSourceMissing, type);
        Map<String, Object> output = handler.handleRequest(input, ctx);

        // validates the response to be false, provisioning should fail in this case, fails if unable to get output or is null
        if (output != null) {
            Assert.assertTrue(output.containsKey("success") && !(boolean) output.get("success"));
        } else {
            Assert.fail();
        }

        testAccountSourceMissing = getBadAccountPayloadWrongSource();
        input = gson.fromJson(testAccountSourceMissing, type);
        output = handler.handleRequest(input, ctx);

        // validates the response to be false, provisioning should fail in this case, fails if unable to get output or is null
        if (output != null) {
            Assert.assertTrue(output.containsKey("success") && !(boolean) output.get("success"));
        } else {
            Assert.fail();
        }

        /*testAccountSourceMissing = getBadAccountPayloadWrongSourceAndBillingButStillInES();
        input = gson.fromJson(testAccountSourceMissing, type);
        output = handler.handleRequest(input, ctx);

        // validates the response to be false, provisioning should fail in this case, fails if unable to get output or is null
        if (output != null) {
            Assert.assertTrue(output.containsKey("success") && !(boolean) output.get("success"));
        } else {
            Assert.fail();
        }*/
    }

    @Test
    public void testProvisionIpGatewayDeviceForPendingDelete() {
        CRPLProvisionIpGatewayDeviceHandler handler = new CRPLProvisionIpGatewayDeviceHandler();
        Context ctx = createContext();
        //Pending Delete Use case
        String pendingDeleteUseCase = "{\"provisionDevice\": {\"acctContext\": {\"sourceId\": \"test123-32131\",\"source\": \"CET\",\"billingAccountId\": \"8499106990017804\",\"deviceInfo\": {\"deviceType\": \"IpGateway\",\"sourceId\": \"3C:7A:8A:54:61:FA\",\"source\": \"CET\",\"status\": \"PendingDelete\",\"eCMMAC\": \"3C:7A:8A:54:61:FA\",\"mocaMAC\": \"00:00:00:00:00\",\"eMtaMAC\": \"3C:7A:8A:54:61:FB\",\"wanMAC\": \"3C:7A:8A:54:61:FC\",\"serialNumber\": \"FBEBUT7AF945517\",\"make\": \"Arris\",\"model\": \"TG1682G\",\"hasDvr\": \"N\",\"hasQAM\": \"N\",\"numTuners\": \"0\",\"dvrNumTuners\": \"0\",\"dvrCapacity\": \"0\",\"dvrCapacityUnits\": \"\"}}},\"cid\":\"crpl-test-ipgateway-provisioning-pd-1\"}";

        input = gson.fromJson(pendingDeleteUseCase, type);
        Map<String, Object> output = handler.handleRequest(input, ctx);

        if (output != null) {
            Assert.assertTrue(output.containsKey("success") && (boolean) output.get("success"));
        } else {
            Assert.fail();
        }

        //Pending Delete Use case
        String badAccountUseCase = "{\"provisionDevice\": {\"acctContext\": {\"sourceId\": \"test123-32131\",\"source\": \"CET\",\"billingAccountId\": \"1313213\",\"deviceInfo\": {\"deviceType\": \"IpGateway\",\"sourceId\": \"3C:7A:8A:54:61:FA\",\"source\": \"CET\",\"status\": \"PendingDelete\",\"eCMMAC\": \"3C:7A:8A:54:61:FA\",\"mocaMAC\": \"00:00:00:00:00\",\"eMtaMAC\": \"3C:7A:8A:54:61:FB\",\"wanMAC\": \"3C:7A:8A:54:61:FC\",\"serialNumber\": \"FBEBUT7AF945517\",\"make\": \"Arris\",\"model\": \"TG1682G\",\"hasDvr\": \"N\",\"hasQAM\": \"N\",\"numTuners\": \"0\",\"dvrNumTuners\": \"0\",\"dvrCapacity\": \"0\",\"dvrCapacityUnits\": \"\"}}},\"cid\":\"crpl-test-ipgateway-provisioning-pd-2\"}";

        input = gson.fromJson(badAccountUseCase, type);
        output = handler.handleRequest(input, ctx);

        if (output != null) {
            Assert.assertTrue(output.containsKey("success") && !(boolean) output.get("success"));
        } else {
            Assert.fail();
        }
    }

    private String getBadAccountPayloadWrongSourceAndBilling() {
        StringBuilder payload = new StringBuilder();
        payload.append("{\"provisionDevice\": {\"acctContext\":");
        payload.append("{\"sourceId\": \"282500330409042015Comcast.RTVE123\",\"source\": \"CET\",\"billingAccountId\": \"8069100020004916123\",");
        payload.append("\"deviceInfo\": {\"deviceType\": \"IpGateway\"," +
                "\"sourceId\": \"10:86:8C:45:82:78\",\"source\": \"CET\"," +
                "\"status\": \"PendingDelete\"," +
                "\"eCMMAC\": \"10:86:8C:45:82:78\"," +
                "\"mocaMAC\": \"00:00:00:00:00\"," +
                "\"eMtaMAC\": \"10:86:8C:45:82:79\"," +
                "\"wanMAC\": \"10:86:8C:45:82:80\"," +
                "\"serialNumber\": \"F22BUE687702793\"," +
                "\"make\": \"Arris\",\"model\": \"TG1682G\"," +
                "\"hasDvr\": \"N\",\"hasQAM\": \"N\",\"numTuners\": \"0\"," +
                "\"dvrNumTuners\": \"0\",\"dvrCapacity\": \"0\",\"dvrCapacityUnits\": \"\"}");
        payload.append("}},\"cid\":\"crpl-test-ipgateway-provisioning-1\"}");
        return payload.toString();
    }

    private String getBadAccountPayloadWrongSource() {
        StringBuilder payload = new StringBuilder();
        payload.append("{\"provisionDevice\": {\"acctContext\":");
        payload.append("{\"sourceId\": \"282500330409042015Comcast.RTVE123\",\"source\": \"CET\",\"billingAccountId\": \"8069100020004916\",");
        payload.append("\"deviceInfo\": {\"deviceType\": \"IpGateway\"," +
                "\"sourceId\": \"10:86:8C:45:82:78\",\"source\": \"CET\"," +
                "\"status\": \"PendingDelete\"," +
                "\"eCMMAC\": \"10:86:8C:45:82:78\"," +
                "\"mocaMAC\": \"00:00:00:00:00\"," +
                "\"eMtaMAC\": \"10:86:8C:45:82:79\"," +
                "\"wanMAC\": \"10:86:8C:45:82:80\"," +
                "\"serialNumber\": \"F22BUE687702793\"," +
                "\"make\": \"Arris\",\"model\": \"TG1682G\"," +
                "\"hasDvr\": \"N\",\"hasQAM\": \"N\",\"numTuners\": \"0\"," +
                "\"dvrNumTuners\": \"0\",\"dvrCapacity\": \"0\",\"dvrCapacityUnits\": \"\"}");
        payload.append("}},\"cid\":\"crpl-test-ipgateway-provisioning-2\"}");
        return payload.toString();
    }

    /*private String getBadAccountPayloadWrongSourceAndBillingButStillInES() {
        StringBuilder payload = new StringBuilder();
        payload.append("{\"provisionDevice\": {\"acctContext\":");
        payload.append("{\"sourceId\": \"282500330409042015Comcast.RTVE\",\"source\": \"CET\",\"billingAccountId\": \"8069100020004916\",");
        payload.append("\"deviceInfo\": {\"deviceType\": \"IpGateway\"," +
                "\"sourceId\": \"10:86:8C:45:82:78\",\"source\": \"CET\"," +
                "\"status\": \"PendingDelete\"," +
                "\"eCMMAC\": \"10:86:8C:45:82:78\"," +
                "\"mocaMAC\": \"00:00:00:00:00\"," +
                "\"eMtaMAC\": \"10:86:8C:45:82:79\"," +
                "\"wanMAC\": \"10:86:8C:45:82:80\"," +
                "\"serialNumber\": \"F22BUE687702793\"," +
                "\"make\": \"Arris\",\"model\": \"TG1682G\"," +
                "\"hasDvr\": \"N\",\"hasQAM\": \"N\",\"numTuners\": \"0\"," +
                "\"dvrNumTuners\": \"0\",\"dvrCapacity\": \"0\",\"dvrCapacityUnits\": \"\"}");
        payload.append("}},\"cid\":\"crpl-test-ipgateway-provisioning-3\"}");
        return payload.toString();
    }*/

    private String getFieldScenarioSuccessPayload() {
        StringBuilder payload = new StringBuilder();
        payload.append("{\"provisionDevice\": {\"acctContext\":");
        payload.append("{\"sourceId\": \"651544540415102015Comcast.RTVE\",\"source\": \"CET\",\"billingAccountId\": \"8993112810158456\",");
        payload.append("\"deviceInfo\": {\"deviceType\": \"IpGateway\"," +
                "\"sourceId\": \"10:86:8C:45:82:78\",\"source\": \"CET\"," +
                "\"status\": \"PendingDelete\"," +
                "\"eCMMAC\": \"10:86:8C:45:82:78\"," +
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

    private String getActiveSuccessPayload() {
        StringBuilder payload = new StringBuilder();
        payload.append("{\"provisionDevice\": {\"acctContext\": ");
        payload.append("{\"sourceId\": \"384741272308092005Comcast.USR9JR\",\"source\": \"CET\",\"billingAccountId\": \"0951830050902\",");
        payload.append("\"deviceInfo\": {\"deviceType\": \"IpGateway\"," +
                "\"sourceId\": \"3C:7A:8A:54:61:FA\",\"source\": \"CET\"," +
                "\"status\": \"Active\"," +
                "\"eCMMAC\": \"10:86:8C:C6:58:B2\"," +
                "\"mocaMAC\": \"00:00:00:00:00\"," +
                "\"eMtaMAC\": \"10:86:8C:C6:58:B3\"," +
                "\"wanMAC\": \"10:86:8C:C6:58:B4\"," +
                "\"serialNumber\": \"FBEBUT7AF945517\"," +
                "\"make\": \"Arris\",\"model\": \"TG1682G\"," +
                "\"hasDvr\": \"N\",\"hasQAM\": \"N\",\"numTuners\": \"0\"," +
                "\"dvrNumTuners\": \"0\",\"dvrCapacity\": \"0\",\"dvrCapacityUnits\": \"\"}");
        payload.append("}},\"cid\":\"crpl-test-ipgateway-provisioning-active-1\"}");
        return payload.toString();
    }

    private String getActiveBadAccountPayload() {
        StringBuilder payload = new StringBuilder();
        payload.append("{\"provisionDevice\": {\"acctContext\": ");
        payload.append("{\"sourceId\": \"test-123-123\",\"source\": \"CET\",\"billingAccountId\": \"123123\",");
        payload.append("\"deviceInfo\": {\"deviceType\": \"IpGateway\"," +
                "\"sourceId\": \"3C:7A:8A:54:61:FA\",\"source\": \"CET\"," +
                "\"status\": \"Active\"," +
                "\"eCMMAC\": \"10:86:8C:C6:58:B2\"," +
                "\"mocaMAC\": \"00:00:00:00:00\"," +
                "\"eMtaMAC\": \"10:86:8C:C6:58:B3\"," +
                "\"wanMAC\": \"10:86:8C:C6:58:B4\"," +
                "\"serialNumber\": \"FBEBUT7AF945517\"," +
                "\"make\": \"Arris\",\"model\": \"TG1682G\"," +
                "\"hasDvr\": \"N\",\"hasQAM\": \"N\",\"numTuners\": \"0\"," +
                "\"dvrNumTuners\": \"0\",\"dvrCapacity\": \"0\",\"dvrCapacityUnits\": \"\"}");
        payload.append("}},\"cid\":\"crpl-test-ipgateway-provisioning-active-1\"}");
        return payload.toString();
    }
}
