/**
 *
 */
package com.crpl.provision.errors;


/**
 * @author radari200
 */
public enum ProvisionStatus {
    INVALID_ACCOUNT("0", "Account passed is not found in biller.", false),
    XBO_PROVISIONING_FAILED("1", "XBO Device Provisioning has failed", false),
    XBO_ADD_SMARTINTERNET_ACCOUNTPRODUCT_FAILED("2", "XBO Add smartinternet accountProduct failed", false),
    FAILED("3", "Failed to process the request", false),
    INVALID_STATUS("4", "Invalid Status in the request", false),
    SUCCESS("200", "Successfully provisioned or updated the device.", true);

    private final String errorCode;
    private final String message;
    private boolean status;


    ProvisionStatus(String errorCode, String message, boolean status) {
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public boolean getStatus() {
        return status;
    }
}
