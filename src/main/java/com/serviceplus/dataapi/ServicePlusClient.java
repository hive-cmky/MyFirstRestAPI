//package com.serviceplus.dataapi;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Component;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//
//@Component
//public class ServicePlusClient {
//
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    @Value("${serviceplus.base-url}")
//    private String baseUrl;
//
//    @Value("${serviceplus.client-id}")
//    private String clientId;
//
//    @Value("${serviceplus.auth-key}")
//    private String authKey;
//
//    @Value("${serviceplus.uid}")
//    private String uid;
//
//    @Value("${serviceplus.usecret}")
//    private String usecret;
//
//    // ============================
//    // 1️⃣ foreignHostAuth (FIRST CURL)
//    // ============================
//    public ResponseEntity<String> authenticate() {
//
//        HttpHeaders headers = buildHeaders();
//        HttpEntity<Void> request = new HttpEntity<>(headers);
//
//        String url = baseUrl + "/configure/foreignHostAuth";
//
//        return restTemplate.exchange(
//                url,
//                HttpMethod.POST,
//                request,
//                String.class
//        );
//    }
//
//    // ============================
//    // 2️⃣ foreignHostInitRequest (SECOND CURL)
//    // ============================
//    public ResponseEntity<String> initRequest(
//            String referenceNo,
//            String encryptedDetails
//    ) {
//
//        HttpHeaders headers = buildHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("reference_no", referenceNo);
//        body.add("encrypted_details", encryptedDetails);
//
//        HttpEntity<MultiValueMap<String, Object>> request =
//                new HttpEntity<>(body, headers);
//
//        String url = baseUrl + "/configure/foreignHostInitRequest";
//
//        return restTemplate.postForEntity(url, request, String.class);
//    }
//
//    // ============================
//    // COMMON HEADERS
//    // ============================
//    private HttpHeaders buildHeaders() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("client_id", clientId);
//        headers.set("auth_key", authKey);
//        headers.set("uid", uid);
//        headers.set("usecret", usecret);
//        return headers;
//    }
//}

package com.serviceplus.dataapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class ServicePlusClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${serviceplus.base-url}")
    private String baseUrl;

    @Value("${serviceplus.client-id}")
    private String clientId;

    @Value("${serviceplus.auth-key}")
    private String authKey;

    @Value("${serviceplus.uid}")
    private String uid;

    @Value("${serviceplus.usecret}")
    private String usecret;

    // GET NEW ENCRYPTION TOKEN
    public String generateEncryptionToken() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("client_id", clientId);
        headers.set("auth_key", authKey);
        headers.set("uid", uid);
        headers.set("usecret", usecret);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
               baseUrl + "/configure/foreignHostAuth",
               HttpMethod.POST,
               request,
               String.class
        );
        JsonNode json = mapper.readTree(response.getBody());
        return json.path("token").asText();
    }

    // push encrypted payload
    public String pushApplication(String referenceNo, String encryptedDetails) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("client_id", clientId);
        headers.set("auth_key", authKey);
        headers.set("usecret", usecret);
        headers.set("uid", uid);

        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("reference_no", referenceNo);
        body.add("encrypted_details", encryptedDetails);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/configure/foreignHostInitRequest",
                request,
                String.class
        );
        return response.getBody();
    }

    // ==================================================
    // ✅ EXACT MATCH OF YOUR WORKING CURL
    // ==================================================
    public ResponseEntity<String> initRequest(
            String referenceNo,
            String encryptedDetails
    ) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("client_id", clientId);
        headers.set("auth_key", authKey);
        headers.set("uid", uid);
        headers.set("usecret", usecret);

        // VERY IMPORTANT
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> formData =
                new LinkedMultiValueMap<>();

        // ❌ DO NOT ADD QUOTES
        formData.add("reference_no", referenceNo);
        formData.add("encrypted_details", encryptedDetails);

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(formData, headers);

        String url = baseUrl + "/configure/foreignHostInitRequest";

        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );
    }

    // ==================================================
    // OPTIONAL: keep auth method but DO NOT USE IT
    // ==================================================
    public ResponseEntity<String> authenticate() {
        HttpHeaders headers = buildHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String url = baseUrl + "/configure/foreignHostAuth";

        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("client_id", clientId);
        headers.set("auth_key", authKey);
        headers.set("uid", uid);
        headers.set("usecret", usecret);
        return headers;
    }
}

