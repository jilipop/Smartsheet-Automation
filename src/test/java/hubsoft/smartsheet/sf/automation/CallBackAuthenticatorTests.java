package hubsoft.smartsheet.sf.automation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.security.GeneralSecurityException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class CallBackAuthenticatorTests {

    private static String correctHmacHeader;
    private static String incorrectHmacHeader;
    private static String callbackBody;
    private static long inputSheetId;
    private static String sharedSecret;

    @Mock
    private Constants mockConstants;

    @InjectMocks
    private CallBackAuthenticator authenticator;

    @BeforeAll
    public static void setup(){
        correctHmacHeader = "e1424c03fd39f3d2ce36d0b25d56507e2a2f79d38091826c51c8c9febf285bd5";
        incorrectHmacHeader = "some string";
        callbackBody = "{\n" +
                "    \"nonce\": \"36819e00-29b1-4aa8-885b-c0159fb9f115\",\n" +
                "    \"timestamp\": \"2020-08-25T16:07:13.554+0000\",\n" +
                "    \"webhookId\": 1595355369367428,\n" +
                "    \"scope\": \"sheet\",\n" +
                "    \"scopeObjectId\": 8036104550016900,\n" +
                "    \"events\": [\n" +
                "        {\n" +
                "            \"objectType\": \"row\",\n" +
                "            \"eventType\": \"deleted\",\n" +
                "            \"id\": 5338365760169860,\n" +
                "            \"userId\": 5589195528923012,\n" +
                "            \"timestamp\": \"2020-08-25T16:07:06.000+0000\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        inputSheetId = 3781490619246468L;
        sharedSecret = "8qhuvdiwg87hm0yjnbuxdicv6";
    }

    @BeforeEach
    public void resetInstanceAndStubSharedSecret() {
        authenticator = new CallBackAuthenticator(mockConstants);
        Mockito.when(mockConstants.getSharedSecrets()).thenReturn(Map.of(inputSheetId, sharedSecret));
    }

    @Test
    @DisplayName("Given a request body and a matching shared secret return true")
    public void testSuccessfulAuthentication() throws GeneralSecurityException {
        boolean authenticationResult = authenticator.authenticate(correctHmacHeader, callbackBody, inputSheetId);

        assertThat(authenticationResult).isEqualTo(true);
    }

    @Test
    @DisplayName("Given a request body and an incorrect shared secret return false")
    public void testFailingAuthentication() throws GeneralSecurityException {
        boolean authenticationResult = authenticator.authenticate(incorrectHmacHeader, callbackBody, inputSheetId);

        assertThat(authenticationResult).isEqualTo(false);
    }
}
