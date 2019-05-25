# Spring Boot Security SAML onelogin demo
Demo using spring-security-saml by onelogin.

## OneLogin configuration
To use OneLogin with this sample application, you'll have to:

- Create an [OneLogin developers account](https://www.onelogin.com/developer-signup)
- Add a SAML Test Connector (IdP) or SAML Test Connector (Advanced).
- Configure the OneLogin application with:
  - *RelayState:* You can use anything here.
  - *Audience:* localhost-demo
  - *Recipient:* http://localhost:8080/saml/SSO
  - *ACS (Consumer) URL Validator:* ^http://localhost:8080/saml/SSO.*$
  - *ACS (Consumer) URL:* http://localhost:8080/saml/SSO
  - *Single Logout URL:* http://localhost:8080/saml/SingleLogout
  - *Parameters:* You can add additional parameters like firstName, lastName.
- In the SSO tab:
  - *X.509 Certificate:* Copy-paste the existing X.509 PEM cerficate into idp-onelogin.xml (ds:X509Certificate).
  - *SAML Signature algorythm:* Use the SHA-256, although SHA-1 will still work.
  - *Issuer URL:* Replace the entityID in the idp-onelogin.xml with this value.
  - *SAML 2.0 Endpoint (HTTP):* Replace the location for the HTTP-Redirect and HTTP-POST binding in the idp-onelogin.xml with this value.
  - *SLO Endpoint (HTTP):* Replace the location for the HTTP-Redirect binding in the idp-onelogin.xml with this value.


## Original
[ulisesbocchio/spring-boot-security-saml-samples/spring-security-saml-sample](https://github.com/ulisesbocchio/spring-boot-security-saml-samples/tree/master/spring-security-saml-sample)

