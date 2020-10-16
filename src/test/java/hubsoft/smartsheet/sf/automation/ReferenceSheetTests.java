package hubsoft.smartsheet.sf.automation;

import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Sheet;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReferenceSheetTests {

    private ReferenceSheets testReferenceSheets;

    private final Set<Long> testSheetIds = Set.of(new Random().nextLong(), new Random().nextLong(), new Random().nextLong());
    private static final Sheet testSheet = new Sheet();

    @MockBean
    private SmartsheetRepository mockRepository;

    @MockBean
    private Constants mockConstants;


    @BeforeAll
    public static void setup() {
        testSheet.setId(new Random().nextLong());
        testSheet.setName("empty test sheet");
    }

    @BeforeEach
    public void resetInstance() {
        testReferenceSheets = new ReferenceSheets(mockConstants, mockRepository);
    }

    @Test
    @Order(1)
    @DisplayName("before run(), the sheets map does not contain any entries matching the sheet ids")
    public void testSheetMapContentsBeforeRun() {
       testSheetIds.forEach(sheetId -> assertThat(ReferenceSheets.getSheet(sheetId)).isNull());
    }

    @Test
    @Order(2)
    @DisplayName("during run() all sheet ids are requested from the repository")
    public void testRepositorySheetRequests() throws SmartsheetException {
        Mockito.when(mockConstants.getInputSheetIds()).thenReturn(testSheetIds);

        testReferenceSheets.run();

        for (long sheetId: testSheetIds) {
            Mockito.verify(mockRepository).getInputSheet(sheetId);
        }
    }

    @Test
    @Order(3)
    @DisplayName("after run(), the sheets map contains entries for all sheet ids")
    public void testSheetMapContentsAfterRun() throws SmartsheetException {
        Mockito.when(mockConstants.getInputSheetIds()).thenReturn(testSheetIds);
        for (long sheetId : testSheetIds) {
            Mockito.when(mockRepository.getInputSheet(sheetId)).thenReturn(new Sheet(sheetId));
        }

        testReferenceSheets.run();

        for (long sheetId : testSheetIds) {
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
