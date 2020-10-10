package hubsoft.smartsheet.sf.automation;

import com.smartsheet.api.SheetResources;
import com.smartsheet.api.Smartsheet;
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

    private static SheetResources mockSheetResources;
    private static Smartsheet mockSmartsheet;
    private static final Sheet testSheet = new Sheet();

    @BeforeAll
    public static void setup() {
        mockSmartsheet = Mockito.mock(Smartsheet.class);
        mockSheetResources = Mockito.mock(SheetResources.class);
        testReferenceSheets = new ReferenceSheets(constants, mockSmartsheet);
        testSheet.setId(123456L);
        testSheet.setName("empty test sheet");
    }

    @Test
    @DisplayName("for each inputSheetId in constants, one sheet is added to map 'sheets'")
    public void testRun() throws SmartsheetException {
        Mockito.when(mockSmartsheet.sheetResources()).thenReturn(mockSheetResources);
        Mockito.when(mockSheetResources.getSheet(anyLong(), isNull(), any(), isNull(), isNull(), isNull(), isNull(), isNull())).thenReturn(testSheet);

        testReferenceSheets.run();

        int inputSheetCount = constants.getInputSheetIds().size();
        Mockito.verify(mockSheetResources, times(inputSheetCount)).getSheet(anyLong(), isNull(), any(), isNull(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("the getter and the setter work")
    public void testGetterAndSetter(){
        ReferenceSheets.setSheet(testSheet.getId(), testSheet);
        Sheet testSheet2 = ReferenceSheets.getSheet(testSheet.getId());

        assertThat(testSheet2).isEqualTo(testSheet);
    }
}
