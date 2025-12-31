package com.serviceplus.dataapi;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class WhatsAppResidentCertificate {

    private static final String SALTED_STR = "Salted__";
    private static final byte[] SALTED_MAGIC = SALTED_STR.getBytes(StandardCharsets.US_ASCII);

    public static void main(String[] args) throws Exception {


        String pdfPath = "D:/test/toy.jpg";
        String base64Stringpdf = getBase64File(pdfPath);
        //System.out.println("Length :\n" + base64Stringpdf.length());


        String pdfPath2 = "D:/test/dummy.pdf";


        // String imagePath = "C:/Users/NIC-PC/Desktop/p.png";
        String imagePath = "D:/test/sig.png";
        String base64Stringimage = getBase64imagess(imagePath);

        //client ID == 822123

        String base64Stringpdf2 = getBase64File(pdfPath2);
        // Anugul Tehesil
            String completeJson = "{\"input\":{\"serviceId\":\"908\",\"appForm\":{\"locationName\":\"1588130\",\"salutation\":\"1\",\"candidatename\":\"Pitamber\",\"gender\":\"1\",\"maritalstatus\":\"2\",\"age\":\"35\",\"aadharno\":\"751012224561\",\"fathername\":\"Rajkishore\",\"mothername\":\"test\",\"mobileno\":\"8249279003\",\"presentdistrict\":\"1225884\",\"presenttehasil\":\"1227089\",\"presentvillage\":\"404262\",\"presentri\":\"1227447\",\"residingpresentaddyears\":\"5\",\"residingpresentaddmonths\":\"2\",\"permanentstate\":\"21\",\"sameaspresentadress\":\"1\",\"permanentdistrict\":\"1225884\",\"permanenttehasil\":\"1227089\",\"permanentvillage\":\"404262\",\"permanentri\":\"1227447\",\"anypersionotherthancandidate\":\"NO\",\"purpose\":\"Service\",\"place\":\"Anugul\",\"iagree\":\"Y\",\"candidatephoto\":\"" + base64Stringimage + "\",\"enclosures\":[{\"entype\":\"5022\",\"endocumenttype\":\"4996\",\"enbase64\":\"" + base64Stringpdf + "\"},{\"entype\":\"5022\",\"endocumenttype\":\"4217\",\"enbase64\":\"" + base64Stringpdf2 + "\"}]}}}";


        String token = "EA2312Hyz1034aZA06464oVg";

        String encryptedDetails = encryptExternal(completeJson, token);
        System.out.println("Encrypted:\n" + encryptedDetails);

   }

    public static String encryptExternal(String clearText, String password) throws Exception {
        byte[] pass = password.getBytes(StandardCharsets.US_ASCII);
        byte[] salt = new SecureRandom().generateSeed(8);
        byte[] inBytes = clearText.getBytes(StandardCharsets.UTF_8);

        byte[] passAndSalt = array_concat(pass, salt);
        byte[] hash = new byte[0];
        byte[] keyAndIv = new byte[0];

        for (int i = 0; i < 3 && keyAndIv.length < 48; i++) {
            byte[] hashData = array_concat(hash, passAndSalt);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            hash = md.digest(hashData);
            keyAndIv = array_concat(keyAndIv, hash);
        }

        byte[] key = Arrays.copyOfRange(keyAndIv, 0, 32);
        byte[] iv = Arrays.copyOfRange(keyAndIv, 32, 48);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));

        byte[] encrypted = cipher.doFinal(inBytes);
        byte[] saltedCipher = array_concat(array_concat(SALTED_MAGIC, salt), encrypted);

        return java.util.Base64.getEncoder().encodeToString(saltedCipher);
    }

    public static String decryptExternal(String encryptedText, String password) throws Exception {
        byte[] ctBytes = java.util.Base64.getDecoder().decode(encryptedText);
        byte[] saltHeader = Arrays.copyOfRange(ctBytes, 0, 8);
        byte[] salt = Arrays.copyOfRange(ctBytes, 8, 16);

        if (!Arrays.equals(saltHeader, SALTED_MAGIC)) {
            throw new IllegalArgumentException("Invalid salt header");
        }

        byte[] pass = password.getBytes(StandardCharsets.US_ASCII);
        byte[] passAndSalt = array_concat(pass, salt);
        byte[] hash = new byte[0];
        byte[] keyAndIv = new byte[0];

        for (int i = 0; i < 3 && keyAndIv.length < 48; i++) {
            byte[] hashData = array_concat(hash, passAndSalt);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            hash = md.digest(hashData);
            keyAndIv = array_concat(keyAndIv, hash);
        }

        byte[] key = Arrays.copyOfRange(keyAndIv, 0, 32);
        byte[] iv = Arrays.copyOfRange(keyAndIv, 32, 48);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));

        byte[] decrypted = cipher.doFinal(ctBytes, 16, ctBytes.length - 16);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private static byte[] array_concat(final byte[] a, final byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }



    private static String getBase64File(String filePath) throws Exception {
        // Read file into byte array
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));

        // Encode to Base64
        String base64String = Base64.getEncoder().encodeToString(fileBytes);

        System.out.println("Base64 Encoded String: ");
        System.out.println(base64String);

        return base64String;
    }

    private static String getBase64imagess(String filePath) throws Exception {
        // Read file into byte array
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));

        // Encode to Base64
        String base64String = Base64.getEncoder().encodeToString(fileBytes);

        System.out.println("Base64 Encoded String: ");
        System.out.println(base64String);

        return base64String;
    }
}
