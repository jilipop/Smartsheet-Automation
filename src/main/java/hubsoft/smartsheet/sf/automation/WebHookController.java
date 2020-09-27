package hubsoft.smartsheet.sf.automation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hubsoft.smartsheet.sf.automation.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class WebHookController {

    private final WebHookService webHookService;
    private final CallBackAuthenticator authenticator;
    private final ObjectMapper mapper;

    @Autowired
    public WebHookController(WebHookService webHookService, CallBackAuthenticator authenticator, ObjectMapper mapper) {
        this.webHookService = webHookService;
        this.authenticator = authenticator;
        this.mapper = mapper;
    }

    @PostMapping("/smartsheet")
    public ResponseEntity<String> callbackResponse(HttpEntity<String> httpEntity,
                                                   @RequestHeader(value = "Smartsheet-Hook-Challenge", required = false) String challengeHeader,
                                                   @RequestHeader(value = "Smartsheet-Hmac-SHA256", required = false) String hmacHeader) throws JsonProcessingException {
        final String requestBodyString = httpEntity.getBody();

        if (challengeHeader == null){
            Callback callback;
            long inputSheetId;
            try {
                callback = mapper.readValue(requestBodyString, Callback.class);
                inputSheetId = callback.getScopeObjectId();
            } catch (JsonProcessingException e) {
                System.out.println("Konnte die Id der Projekte-Tabelle nicht aus dem Callback auslesen.");
                throw e;
            }
            if (!authenticator.authenticate(hmacHeader, requestBodyString, inputSheetId)) {
                System.out.println("Ein Callback mit falscher Authentifizierung wurde abgelehnt.");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            else if (callback.getEvents() != null){
                System.out.println("Smartsheet hat Updates gemeldet:");
                callback.getEvents().forEach(event -> System.out.println(event.getObjectType() + " " + event.getEventType()));
                System.out.println("Das ist der rohe JSON-String:");
                System.out.println(requestBodyString);

                webHookService.processTemplates(callback.getScopeObjectId());
            } else if (callback.getNewWebhookStatus() != null){
                    System.out.println("Smartsheet hat eine Änderung des Webhook-Status gesendet: " + callback.getNewWebhookStatus());
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Smartsheet-Hook-Response", challengeHeader);
            try {
                VerificationRequest request = mapper.readValue(requestBodyString, VerificationRequest.class);
                HookResponseBody hookResponseBody = new HookResponseBody(request.getChallenge());
                System.out.println("Smartsheet hat eine Verifikations-Anfrage geschickt: " + request.getChallenge());
                return new ResponseEntity<>(mapper.writeValueAsString(hookResponseBody), headers, HttpStatus.OK);
            } catch (JsonProcessingException e) {
                System.out.println(e.getMessage());
                return new ResponseEntity<>(headers, HttpStatus.OK);
            }
        }
    }
}