package com.crpl.provision;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.crpl.provision.errors.ProvisionStatus;
import com.crpl.support.Util;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.crpl.support.attributes.ServiceAttributes.CID;

public class CRPLProvisionIpGatewayDeviceHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final Logger log = Logger.getLogger(CRPLProvisionIpGatewayDeviceHandler.class);
    private String cid = null;

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        // Initialize the trackingId OR cid
        cid = Util.getTrackingIdFromObjectMap(input);
        org.apache.log4j.MDC.put(CID, cid);
        log.info("Provision Gateway Device Input Request=" + input.toString());

        // Process request.
        CRPLProvisionIpGatewayDeviceImpl CRPLProvisionIpGatewayDeviceImpl = new CRPLProvisionIpGatewayDeviceImpl(cid);
        ProvisionStatus provisionStatus = CRPLProvisionIpGatewayDeviceImpl.processRequest(input);
        log.info("Provision Response Status = " + provisionStatus.getMessage());

        // Generate response
        Map<String, Object> provisionDeviceResponseMap = new HashMap<String, Object>();
        provisionDeviceResponseMap.put("success", provisionStatus.getStatus());
        provisionDeviceResponseMap.put("statusCode", provisionStatus.getErrorCode());
        provisionDeviceResponseMap.put("message", provisionStatus.getMessage());
        String serviceAccountId = CRPLProvisionIpGatewayDeviceImpl.getServiceAccount();
        if (serviceAccountId != null) {
            if (serviceAccountId.contains("/")) {
                serviceAccountId = serviceAccountId.substring(serviceAccountId.lastIndexOf("/") + 1);
            }
            provisionDeviceResponseMap.put("serviceAccountId", serviceAccountId);
        }
        log.info("Response Map = " + provisionDeviceResponseMap.toString());

        return provisionDeviceResponseMap;
    }
}
