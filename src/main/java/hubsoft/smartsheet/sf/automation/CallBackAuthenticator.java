package hubsoft.smartsheet.sf.automation;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.GeneralSecurityException;

@Component
public class CallBackAuthenticator {

    private final Constants constants;

    public CallBackAuthenticator(Constants constants) {
        this.constants = constants;
    }

    public boolean authenticate(String hmacHeader, String requestBody, long inputSheetId) throws GeneralSecurityException {
        return hmacHeader.equals(calculateHmac(constants.getSharedSecrets().get(inputSheetId), requestBody));
    }

    private String calculateHmac(String sharedSecret, String callbackBody) throws GeneralSecurityException {
        String HMAC_SHA256_ALGORITHM = "HmacSHA256";
        int HMAC_RADIX = 16;

        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(new SecretKeySpec(sharedSecret.getBytes(), HMAC_SHA256_ALGORITHM));

        byte[] rawHmac = mac.doFinal(callbackBody.getBytes());
        return new BigInteger(1, rawHmac).toString(HMAC_RADIX);
    }
}
