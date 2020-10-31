package hubsoft.smartsheet.sf.automation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class WebHookServiceTests {

    @Mock
    private Constants mockConstants;

    @Mock
    private SmartsheetRepository mockRepository;

    @InjectMocks
    private WebHookService webHookService;

    @BeforeAll
    private static void setup(){

    }
}
