package hubsoft.smartsheet.sf.automation;

import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Sheet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReferenceSheetTests {

    private final Constants constants;
    private final ReferenceSheets testReferenceSheets;

    private static SmartsheetRepository mockRepository;
    private static final Sheet testSheet = new Sheet();

    @Autowired
    public ReferenceSheetTests(Constants constants) {
        this.constants = constants;
        testReferenceSheets = new ReferenceSheets(constants, mockRepository);
    }

    @BeforeAll
    public static void setup() {
        mockRepository = Mockito.mock(SmartsheetRepository.class);
        testSheet.setId(123456L);
        testSheet.setName("empty test sheet");
    }

    @Test
    @DisplayName("all sheet ids in constants are requested from the repository and all returned sheets are saved in sheets map")
    public void testRun() throws SmartsheetException {
        int randomInt = new Random().nextInt();
        for (long sheetId: constants.getInputSheetIds()){
            Mockito.when(mockRepository.getInputSheet(sheetId)).thenReturn(new Sheet(sheetId + randomInt));
        }

        testReferenceSheets.run();

        for (long sheetId: constants.getInputSheetIds()){
            assertThat(ReferenceSheets.getSheet(sheetId).getId()).isEqualTo(sheetId + randomInt);
        }
    }

    @Test
    @DisplayName("the getter and the setter work")
    public void testGetterAndSetter(){
        ReferenceSheets.setSheet(testSheet.getId(), testSheet);
        Sheet returnedSheet = ReferenceSheets.getSheet(testSheet.getId());

        assertThat(returnedSheet).isEqualTo(testSheet);
    }
}
