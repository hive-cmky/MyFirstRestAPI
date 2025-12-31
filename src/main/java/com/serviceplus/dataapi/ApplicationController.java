package com.serviceplus.dataapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    @Autowired
    private ServicePlusClient servicePlusClient;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final AtomicInteger PAYLOAD_COUNTER = new AtomicInteger(0);

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitApplication(
            @RequestBody JsonNode formData) {

        int payloadNo = PAYLOAD_COUNTER.incrementAndGet();

        JsonNode input = formData.path("input");
        String token = input.path("token").asText(null);
        String referenceNo = input.path("referenceNo").asText(null);

        String encryptedDetails;
        String realReferenceNo = "NA";
        // transactionId = "";

        try {
            // 🔹 Encrypt FULL JSON (token + reference inside)
            String runtimeJson = objectMapper.writeValueAsString(formData);
            encryptedDetails =
                    WhatsAppResidentCertificate.encryptExternal(runtimeJson, token);

            // 🔹 Logging
            System.out.println("\n====================================");
            System.out.println("PAYLOAD #" + payloadNo);
            System.out.println("====================================");
            System.out.println(runtimeJson);
            System.out.println("\nEncrypted Payload:");
            System.out.println(encryptedDetails);

            // 🔥 Call ServicePlus ONLY ONCE
            servicePlusClient.authenticate();
            ResponseEntity<String> spResponse =
                    servicePlusClient.initRequest(referenceNo, encryptedDetails);

            System.out.println("\n=== ServicePlus Response ===");
            System.out.println(spResponse.getBody());

            // 🔹 Parse ServicePlus JSON
            JsonNode spJson =
                    objectMapper.readTree(spResponse.getBody());

            realReferenceNo =
                   spJson.path("referenceNo ").asText("NA");
            //transactionId =  spJson.path("transactionId").asText("NA");

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }

        String receiptDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        // ✅ SEND REAL APPLICATION NUMBER TO FLUTTER
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Application submitted successfully.",
                "applicationId", realReferenceNo,
                "receiptDate", receiptDate,
                "serviceName", "Resident Certificate"
        ));
    }
}




