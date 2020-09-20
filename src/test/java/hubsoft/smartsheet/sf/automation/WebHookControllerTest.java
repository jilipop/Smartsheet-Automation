package hubsoft.smartsheet.sf.automation;

import com.fasterxml.jackson.databind.ObjectMapper;
import hubsoft.smartsheet.sf.automation.models.Callback;
import hubsoft.smartsheet.sf.automation.models.Event;
import hubsoft.smartsheet.sf.automation.models.VerificationRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class WebHookControllerTest {

    private static VerificationRequest verificationRequest;

    @MockBean
    private WebHookService service;

    private final MockMvc mockMvc;

    @Autowired
    public WebHookControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeAll
    public static void setup(){
        verificationRequest = new VerificationRequest(
                "d78dd1d3-01ce-4481-81de-92b4f3aa5ab1",
                2674017481058180L
        );
    }

    @Test
    @DisplayName("Successful Callback")
    public void testSuccessfulCallback() throws Exception {
        mockMvc.perform(post("/smartsheet")
        .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(
                        new Callback(4509506114742148L, List.of(
                                new Event("sheet", "updated"),
                                new Event("row", "created")
                        ))))
                .accept(MediaType.APPLICATION_JSON));

    }

    @Test
    @DisplayName("Verification Request")
    public void testVerificationRequest() throws Exception {
        mockMvc.perform(post("/smartsheet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(verificationRequest))
                .header("Smartsheet-Hook-Challenge", verificationRequest.getChallenge())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Smartsheet-Hook-Response", verificationRequest.getChallenge()))
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