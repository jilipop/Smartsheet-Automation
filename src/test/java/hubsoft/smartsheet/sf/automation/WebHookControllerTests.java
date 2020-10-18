package hubsoft.smartsheet.sf.automation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hubsoft.smartsheet.sf.automation.models.Callback;
import hubsoft.smartsheet.sf.automation.models.Event;
import hubsoft.smartsheet.sf.automation.models.VerificationRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WebHookControllerTests {

    private static Callback eventCallBack;
    private static Callback statusChangeCallBack;
    private static VerificationRequest verificationRequest;

    private final MockMvc mockMvc;

    @MockBean
    private WebHookService mockService;

    @MockBean
    private CallBackAuthenticator mockAuthenticator;

    @Autowired
    public WebHookControllerTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeAll
    public static void setup(){
        verificationRequest = new VerificationRequest(
                "d78dd1d3-01ce-4481-81de-92b4f3aa5ab1",
                2674017481058180L
        );
        eventCallBack = new Callback(
                4509506114742148L,
                List.of(
                        new Event("sheet", "updated"),
                        new Event("row", "created")
        ));
        statusChangeCallBack = new Callback(
                4509506114742148L,
                "DISABLED_SCOPE_INACCESSIBLE"
        );
    }

    @Test
    @DisplayName("When event callback gets authenticated, process templates and return 200")
    public void testEventCallbackSuccess() throws Exception {
        Mockito.when(mockAuthenticator.authenticate(any(), any(), anyLong())).thenReturn(true);

        mockMvc.perform(post("/smartsheet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(eventCallBack))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(mockService).processTemplates(eventCallBack.getScopeObjectId());
    }


    @Test
    @DisplayName("When JSON processing of callback fails, throw JsonProcessingException")
    public void testCallBackJSONError () {
        assertThatThrownBy(() -> mockMvc.perform(post("/smartsheet")
                .contentType(MediaType.APPLICATION_JSON)
                .content("Non-JSON-String")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
        ).isInstanceOf(JsonProcessingException.class);
    }


    @Test
    @DisplayName("When status change callback gets authenticated, return 200")
    public void testStatusChangeCallback() throws Exception {
        Mockito.when(mockAuthenticator.authenticate(any(), any(), anyLong())).thenReturn(true);

        mockMvc.perform(post("/smartsheet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(statusChangeCallBack))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("When authentication fails, return 403")
    public void testAuthenticationFailure() throws Exception {
        Mockito.when(mockAuthenticator.authenticate(anyString(), anyString(), anyLong())).thenReturn(false);

        mockMvc.perform(post("/smartsheet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(eventCallBack))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("When a healthy verification request arrives, answer it correctly")
    public void testVerificationResponseWithHealthyBody() throws Exception {
        String challengeString = verificationRequest.getChallenge();
        mockMvc.perform(post("/smartsheet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(verificationRequest))
                .header("Smartsheet-Hook-Challenge", challengeString)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Smartsheet-Hook-Response", challengeString))
                .andExpect(jsonPath("$.smartsheetHookResponse").value(challengeString))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("When a verification request arrives but the body can't be read, send back the challenge header and status 200")
    public void testVerificationResponseWhenBodyIsUnreadable() throws Exception {
        String challengeString = verificationRequest.getChallenge();
        mockMvc.perform(post("/smartsheet")
                .contentType(MediaType.APPLICATION_JSON)
                .content("Non-JSON-String")
                .header("Smartsheet-Hook-Challenge", challengeString)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Smartsheet-Hook-Response", challengeString))
                .andExpect(jsonPath("$.smartsheetHookResponse").doesNotExist())
                .andExpect(status().isOk());
    }

    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}