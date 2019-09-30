# IpGatewayProvisioning

Provisions IpGateway device (XB3's and XB6's) in XBO, also adds smart-internet accountProduct to the account as part of the provisioning logic.
<br/> How it works? <br/>
<pre>
<ol>
<li>Gets BillingAccount Id from the request</li>
<li>Gets SourceId using CRPLAWSUtil call to CRPLCETProxyWS by billingAccountId, if null provisioning fails with INVALID ACCOUNT status</li>
<li>Gets Status from Payload</li>
<li>If status Active
<ul>
<li>Invokes ProvisionDevice Lambda with the input.</li>
<li>Gets ServiceAccount from the ProvisionDevice Response, if null provisioning fails with XBO_PROVISIONING_FAILED status</li>
<li>Adds Smart Internet Account Product to the serviceAccountId using CRPLBowsAddAccountProduct, if failure in adding the accountProduct, provisioning fails with XBO_ADD_SMARTINTERNET_ACCOUNTPRODUCT_FAILED status</li>
</ul>
</li>
<li>If non-active device found
<ul>
<li>Gets ECMMAC from the input and updates <b>ALL</b> devices(gets all devices using CRPLGetDevice) with the ecmmac status using CRPLUpdateDeviceStatus to the status in input by ecmMAC,if failure then provisioning ends with FAILED status</li>
</ul>
</li>
<li>Performs an account refresh by sending SNS to AccountRefresh topic, refresh happens using CRPLRefreshAccount</li>
</ol>
</pre>
 <br/>Sample Input:<br/>
 <pre>
 {  
     provisionDevice:   {  
        acctContext:      {  
           sourceId:282500330409042015Comcast.RTVE123,
           source:CET,
           billingAccountId:8069100020004916123,
           deviceInfo:         {  
              deviceType:IpGateway,
              sourceId:10:86:8            C:45:82:78,
              source:CET,
              status:PendingDelete,
              eCMMAC:10:86:8            C:45:82:78,
              mocaMAC:00:00:00:00:00,
              eMtaMAC:10:86:8            C:45:82:79,
              wanMAC:10:86:8            C:45:82:80,
              serialNumber:F22BUE687702793,
              make:Arris,
              model:TG1682G,
              hasDvr:N,
              hasQAM:N,
              numTuners:0,
              dvrNumTuners:0,
              dvrCapacity:0,
              dvrCapacityUnits:
           }
        }
     },
     cid:crpl-test-ipgateway-provisioning-1
  }
 </pre>
<pre>
Dependencies
<a href="https://github.comcast.com/CRPL/SupportFunctions-Device/tree/dev/CRPLProvisionDevice">CRPLProvisionDevice</a>
<a href="https://github.comcast.com/CRPL/SupportFunctions-Account/tree/dev/CRPLBowsAddAccountProduct">CRPLBowsAddAccountProduct</a>
<a href="https://github.comcast.com/CRPL/SupportFunctions/tree/dev/CRPLCetProxyWebService">CRPLCetProxyWebService</a>
<a href="https://github.comcast.com/CRPL/SupportFunctions-Device/tree/dev/CRPLGetDevice">CRPLGetDevice</a>
<a href="https://github.comcast.com/CRPL/SupportFunctions-Device/tree/dev/CRPLUpdateDeviceStatus">CRPLUpdateDeviceStatus</a>
<a href="https://github.comcast.com/CRPL/SupportFunctions-Commons/tree/dev/CRPLAwsUtil">CRPLAwsUtil</a>
<a href="https://github.comcast.com/CRPL/SupportFunctions-Commons/tree/dev/CRPLGenerateSATToken">CRPLGenerateSATToken</a>
END TO END
<a href="https://github.comcast.com/CRPL/SupportFunctions-Account/tree/dev/CRPLGenerateRefreshAccount">CRPLRefreshAccount</a>
</pre>
