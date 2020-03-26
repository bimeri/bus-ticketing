package net.gowaka.gowaka.network.api.payamgo.utils;

import net.gowaka.gowaka.exception.ApiException;
import net.gowaka.gowaka.exception.ErrorCodes;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.http.HttpStatus;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;

public class Hashes {
    public static String getClientInitiationHash(Double amount, String currencyCode,
                                                 String appTransactionNumber, String appUserPhoneNumber,
                                                 String paymentResponseUrl, String clientSecret) {
        String hashingStr = amount + currencyCode +
                appTransactionNumber + appUserPhoneNumber + paymentResponseUrl + clientSecret;

//        System.out.println("amount: " + amount);
//        System.out.println("currencyCode: " + currencyCode);
//        System.out.println("appTransactionNumber: " + appTransactionNumber);
//        System.out.println("appUserPhoneNumber: " + appUserPhoneNumber);
//        System.out.println("paymentResponseUrl: " + paymentResponseUrl);
//        System.out.println("clientSecret: " + clientSecret);
//        System.out.println("str to hash: " + hashingStr);
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("md5");
            messageDigest.update(hashingStr.getBytes());
            String hashedResult = DatatypeConverter.printHexBinary(messageDigest.digest()).toLowerCase();
//            System.out.println("hashedResult: " + hashedResult);
            return hashedResult;
        } catch (Exception ex) {
            throw new ApiException(ErrorCodes.MD5_HASH_FAILED.getMessage(), ErrorCodes.MD5_HASH_FAILED.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static String getClientCheckStatusHash(String processingNumber, String clientSecret) {
        String hashedStr = processingNumber + clientSecret;
        return MD5Encoder.encode(hashedStr.getBytes());
    }
}
