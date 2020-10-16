package hubsoft.smartsheet.sf.automation;

import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Sheet;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReferenceSheetTests {

    private final Constants constants;
    private ReferenceSheets testReferenceSheets;

    private static SmartsheetRepository mockRepository;
    private static final Sheet testSheet = new Sheet();

    @Autowired
    public ReferenceSheetTests(Constants constants) {
        this.constants = constants;
    }

    @BeforeAll
    public static void setup() {
        mockRepository = Mockito.mock(SmartsheetRepository.class);
        testSheet.setId(123456L);
        testSheet.setName("empty test sheet");
    }

    @BeforeEach
    public void resetInstance() {
        testReferenceSheets = new ReferenceSheets(constants, mockRepository);
    }

    @Test
    @Order(1)
    @DisplayName("before calling run(), the sheets map does not contain any entries matching the sheet ids in constants")
    public void testSheetMapContentsBeforeRun() {
        for (long sheetId: constants.getInputSheetIds()){
            assertThat(ReferenceSheets.getSheet(sheetId)).isNull();
        }
    }

    @Test
    @Order(2)
    @DisplayName("during run() all sheet ids in constants are requested from the repository")
    public void testRepositorySheetRequests() throws SmartsheetException {
        testReferenceSheets.run();

        for (long sheetId: constants.getInputSheetIds()) {
            Mockito.verify(mockRepository).getInputSheet(sheetId);
        }
    }

    @Test
    @Order(3)
    @DisplayName("after calling run(), the sheets map contains entries for all sheet ids in constants")
    public void testSheetMapContentsAfterRun() throws SmartsheetException {
        for (long sheetId : constants.getInputSheetIds()) {
            Mockito.when(mockRepository.getInputSheet(sheetId)).thenReturn(new Sheet(sheetId));
        }

        testReferenceSheets.run();

        for (long sheetId : constants.getInputSheetIds()) {
            assertThat(ReferenceSheets.getSheet(sheetId).getId()).isEqualTo(sheetId);
        }
    }


    @Test
    @DisplayName("an object passed to the setter can then be retrieved through the getter")
    public void testGetterAndSetter(){
        ReferenceSheets.setSheet(testSheet.getId(), testSheet);
        Sheet returnedSheet = ReferenceSheets.getSheet(testSheet.getId());

        assertThat(returnedSheet).isEqualTo(testSheet);
    }
}
