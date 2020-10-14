package hubsoft.smartsheet.sf.automation;

import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Sheet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReferenceSheetTests {

    private static final Constants constants = new Constants();
    private static ReferenceSheets testReferenceSheets;

    private static SmartsheetRepository mockRepository;
    private static final Sheet testSheet = new Sheet();

    @BeforeAll
    public static void setup() {
        mockRepository = Mockito.mock(SmartsheetRepository.class);
        testReferenceSheets = new ReferenceSheets(constants, mockRepository);
        testSheet.setId(123456L);
        testSheet.setName("empty test sheet");
    }

    @Test
    @DisplayName("for each inputSheetId in constants, one sheet is requested from the repository")
    public void testRun() throws SmartsheetException {
        Mockito.when(mockRepository.getInputSheet(anyLong())).thenReturn(testSheet);

        testReferenceSheets.run();

        int inputSheetCount = constants.getInputSheetIds().size();
        Mockito.verify(mockRepository, times(inputSheetCount)).getInputSheet(anyLong());
    }

    @Test
    @DisplayName("the getter and the setter work")
    public void testGetterAndSetter(){
        ReferenceSheets.setSheet(testSheet.getId(), testSheet);
        Sheet returnedSheet = ReferenceSheets.getSheet(testSheet.getId());

        assertThat(returnedSheet).isEqualTo(testSheet);
    }
}
