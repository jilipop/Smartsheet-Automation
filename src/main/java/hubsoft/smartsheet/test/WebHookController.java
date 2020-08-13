package hubsoft.smartsheet.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hubsoft.smartsheet.test.models.EventCallback;
import hubsoft.smartsheet.test.models.HookResponseBody;
import hubsoft.smartsheet.test.models.StatusChangeCallback;
import hubsoft.smartsheet.test.models.VerificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class WebHookController {

    private final WebHookService webHookService;
    private final ObjectMapper mapper;

    @Autowired
    public WebHookController(WebHookService webHookService, ObjectMapper mapper) {
        this.webHookService = webHookService;
        this.mapper = mapper;
    }

    @PostMapping("/smartsheet")
    public ResponseEntity<String> callbackResponse(HttpEntity<String> httpEntity,
                                                   @RequestHeader(value = "Smartsheet-Hook-Challenge", required = false) String challengeHeader,
                                                   @RequestHeader(value = "Smartsheet-Hmac-SHA256", required = false) String hmacHeader){
        final String requestBodyString = httpEntity.getBody();

        if (challengeHeader == null){
            if (!webHookService.authenticateCallBack(hmacHeader, requestBodyString))
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            else if (requestBodyString.contains("\"events\":")){
                try {
                    EventCallback eventCallback = mapper.readValue(requestBodyString, EventCallback.class);
                } catch (JsonProcessingException e) {
                    System.out.println(e.getMessage());
                }
                webHookService.updateSheets();
            } else {
                try {
                    StatusChangeCallback statusChangeCallback = mapper.readValue(requestBodyString, StatusChangeCallback.class);
                } catch (JsonProcessingException e) {
                    System.out.println(e.getMessage());
                }
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Smartsheet-Hook-Response", challengeHeader);
            try {
                VerificationRequest request = mapper.readValue(requestBodyString, VerificationRequest.class);
                HookResponseBody hookResponseBody = new HookResponseBody(request.getChallenge());
                return new ResponseEntity<>(mapper.writeValueAsString(hookResponseBody), headers, HttpStatus.OK);
            } catch (JsonProcessingException e) {
                System.out.println(e.getMessage());
                return new ResponseEntity<>(headers, HttpStatus.OK);
            }
        }
    }
}
