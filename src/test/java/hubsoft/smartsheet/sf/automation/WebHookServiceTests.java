package hubsoft.smartsheet.sf.automation;

import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Cell;
import com.smartsheet.api.models.Column;
import com.smartsheet.api.models.Row;
import com.smartsheet.api.models.Sheet;
import com.smartsheet.api.models.enums.ColumnType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class WebHookServiceTests {

    private static Random random = new Random();

    private static Sheet testInputSheet = new Sheet(random.nextLong());
    private static Sheet testRefSheet = new Sheet(testInputSheet.getId());

    private static Column kvColumn = new Column(random.nextLong());
    private static Column tColumn = new Column(random.nextLong());
    private static Column slColumn = new Column(random.nextLong());

    private static Cell kvCell = new Cell();
    private static Cell tCell = new Cell();
    private static Cell slCell = new Cell();

    private static Cell kvRefCell = new Cell();
    private static Cell tRefCell = new Cell();
    private static Cell slRefCell = new Cell();

    private static Row testRow = new Row(random.nextLong());
    private static Row testRefRow = new Row(testRow.getId());

    private Boolean kvBool;
    private Boolean tBool;
    private Boolean slBool;

    @Mock
    private Constants mockConstants;

    @Mock
    private SmartsheetRepository mockRepository;

    @InjectMocks
    private WebHookService webHookService;

    @BeforeAll
    private static void setup(){
        kvColumn.setTitle("KV");
        tColumn.setTitle("T");
        slColumn.setTitle("SL");

        kvColumn.setType(ColumnType.CHECKBOX);
        tColumn.setType(ColumnType.CHECKBOX);
        slColumn.setType(ColumnType.CHECKBOX);

        kvCell.setColumnType(ColumnType.CHECKBOX);
        tCell.setColumnType(ColumnType.CHECKBOX);
        slCell.setColumnType(ColumnType.CHECKBOX);
        kvRefCell.setColumnType(ColumnType.CHECKBOX);
        tRefCell.setColumnType(ColumnType.CHECKBOX);
        slRefCell.setColumnType(ColumnType.CHECKBOX);

        kvCell.setColumnId(kvColumn.getId());
        tCell.setColumnId(tColumn.getId());
        slCell.setColumnId(slColumn.getId());
        kvRefCell.setColumnId(kvColumn.getId());
        tRefCell.setColumnId(tColumn.getId());
        slRefCell.setColumnId(slColumn.getId());

        testInputSheet.setColumns(List.of(kvColumn, tColumn, slColumn));
        testRefSheet.setColumns(List.of(kvColumn, tColumn, slColumn));

        testRow.setColumns(List.of(kvColumn, tColumn, slColumn));
        testRow.setCells(List.of(kvCell, tCell, slCell));
        testInputSheet.setRows(List.of(testRow));

        testRefRow.setColumns(List.of(kvColumn, tColumn, slColumn));
        testRefRow.setCells(List.of(kvRefCell, tRefCell, slRefCell));
        testRefSheet.setRows(List.of(testRefRow));
    }

    @BeforeEach
    public void reset(){
        kvBool = random.nextBoolean();
        tBool = random.nextBoolean();
        slBool = random.nextBoolean();
    }

    @Test
    @DisplayName("if there are no new checkmarks in columns KV/T/SL, no changes are made to the Smartsheet account nor the reference sheet values")
    public void testNoCheckmarks() throws SmartsheetException {
        kvCell.setValue(kvBool);
        tCell.setValue(tBool);
        slCell.setValue(slBool);
        kvRefCell.setValue(kvBool);
        tRefCell.setValue(tBool);
        slRefCell.setValue(slBool);
        ReferenceSheets.setSheet(testInputSheet.getId(), testRefSheet);
        Mockito.when(mockRepository.getSheet(anyLong())).thenReturn(testInputSheet);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).getSheet(testInputSheet.getId());
        Mockito.verifyNoMoreInteractions(mockRepository);
        assertThat(ReferenceSheets.getSheet(testInputSheet.getId())).isEqualTo(testRefSheet);
    }

    @Test
    @DisplayName("if checkmarks are removed, no changes are made to the Smartsheet account, but the reference sheet is updated accordingly")
    public void restRemoveCheckMarks() throws SmartsheetException {
        kvCell.setValue(false);
        tCell.setValue(false);
        slCell.setValue(false);
        kvRefCell.setValue(true);
        tRefCell.setValue(true);
        slRefCell.setValue(true);
        ReferenceSheets.setSheet(testInputSheet.getId(), testRefSheet);
        Mockito.when(mockRepository.getSheet(anyLong())).thenReturn(testInputSheet);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).getSheet(testInputSheet.getId());
        Mockito.verifyNoMoreInteractions(mockRepository);
        assertThat(ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(0).getCells().get(0).getValue()).isEqualTo(false);
        assertThat(ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(0).getCells().get(1).getValue()).isEqualTo(false);
        assertThat(ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(0).getCells().get(2).getValue()).isEqualTo(false);
    }

//    @Test
//    @DisplayName("if there is a new KV checkmark, ")
}
