package hubsoft.smartsheet.sf.automation.webhookservice;

import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.*;
import hubsoft.smartsheet.sf.automation.Constants;
import hubsoft.smartsheet.sf.automation.ReferenceSheets;
import hubsoft.smartsheet.sf.automation.SmartsheetRepository;
import hubsoft.smartsheet.sf.automation.WebHookService;
import hubsoft.smartsheet.sf.automation.enums.Id;
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

import java.util.EnumMap;
import java.util.List;
import java.util.Random;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class WebHookServiceSlTests {

    private static final Random random = new Random();

    private static final Sheet testInputSheet = new Sheet(random.nextLong());
    private static final Sheet testRefSheet = new Sheet(testInputSheet.getId());

    private static final Column kvColumn = new Column(random.nextLong());
    private static final Column tColumn = new Column(random.nextLong());
    private static final Column slColumn = new Column(random.nextLong());
    private static final Column jobNumberColumn = new Column(random.nextLong());
    private static final Column clientColumn = new Column(random.nextLong());
    private static final Column labelColumn = new Column(random.nextLong());
    private static final Column projectColumn = new Column(random.nextLong());

    private static final Cell jobNumberCell = new Cell();
    private static final Cell clientCell = new Cell();
    private static final Cell labelCell = new Cell();
    private static final Cell projectCell = new Cell();
    private static final Cell kvCell = new Cell();
    private static final Cell tCell = new Cell();
    private static final Cell slCell = new Cell();

    private static final Cell kvRefCell = new Cell();
    private static final Cell tRefCell = new Cell();
    private static final Cell slRefCell = new Cell();

    private static final Row testRow = new Row(random.nextLong());
    private static final Row testRefRow = new Row(testRow.getId());

    private static final EnumMap<Id, Long> testIds = new EnumMap<>(Id.class);

    private static Constants mockConstants;

    @Mock
    private SmartsheetRepository mockRepository;

    @InjectMocks
    private WebHookService webHookService;

    @BeforeAll
    private static void setupTestData(){
        jobNumberColumn.setTitle("Job_Nr.");
        clientColumn.setTitle("Kunde");
        labelColumn.setTitle("Label");
        projectColumn.setTitle("Projektname");
        kvColumn.setTitle("KV");
        tColumn.setTitle("T");
        slColumn.setTitle("SL");

        jobNumberCell.setColumnId(jobNumberColumn.getId());
        clientCell.setColumnId(clientColumn.getId());
        labelCell.setColumnId(labelColumn.getId());
        projectCell.setColumnId(projectColumn.getId());
        kvCell.setColumnId(kvColumn.getId());
        tCell.setColumnId(tColumn.getId());
        slCell.setColumnId(slColumn.getId());

        kvRefCell.setColumnId(kvColumn.getId());
        tRefCell.setColumnId(tColumn.getId());
        slRefCell.setColumnId(slColumn.getId());

        testInputSheet.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, kvColumn, tColumn, slColumn));
        testRefSheet.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, kvColumn, tColumn, slColumn));

        testRow.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, kvColumn, tColumn, slColumn));
        testRow.setCells(List.of(jobNumberCell, clientCell, labelCell, projectCell, kvCell, tCell, slCell));
        testInputSheet.setRows(List.of(testRow));

        testRefRow.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, kvColumn, tColumn, slColumn));
        testRefRow.setCells(List.of(kvRefCell, tRefCell, slRefCell));
        testRefSheet.setRows(List.of(testRefRow));

        testIds.put(Id.SHOTLIST_WORKSPACE_MF, random.nextLong());
        testIds.put(Id.SHOTLIST_WORKSPACE_ELEVEN, random.nextLong());
        testIds.put(Id.SHOTLIST_TEMPLATE, random.nextLong());
        testIds.put(Id.MF_WORKSPACE, random.nextLong());
        testIds.put(Id.ELEVEN_WORKSPACE, random.nextLong());

        mockConstants = Mockito.mock(Constants.class);
        Mockito.when(mockConstants.getIds()).thenReturn(testIds);
    }

    @BeforeEach
    public void resetData() {
        resetCellEntries();
        randomizeCheckmarks();
        ReferenceSheets.setSheet(testInputSheet.getId(), testRefSheet);
    }

    private void resetCellEntries() {
        jobNumberCell.setDisplayValue("12345");
        clientCell.setDisplayValue("Testkunde");
        labelCell.setValue("Mädchenfilm");
        projectCell.setDisplayValue("Testname");
    }

    private void randomizeCheckmarks() {
        Boolean kvBool = random.nextBoolean();
        Boolean tBool = random.nextBoolean();
        kvCell.setValue(kvBool);
        tCell.setValue(tBool);
        kvRefCell.setValue(kvBool);
        tRefCell.setValue(tBool);
    }

    @BeforeEach
    public void stubGetSheet() throws SmartsheetException {
        Mockito.when(mockRepository.getSheet(testInputSheet.getId())).thenReturn(testInputSheet);
    }

    @Test
    @DisplayName("if there is a new SL checkmark, the correct template id is passed to the repository")
    public void testSlTemplate() throws SmartsheetException {
        slCell.setValue(true);
        slRefCell.setValue(false);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).copySheetToWorkspace(eq(testIds.get(Id.SHOTLIST_TEMPLATE)), anyLong(), any(), any());
    }

    @Test
    @DisplayName("if there is a new SL checkmark and the label is 'Mädchenfilm', the Shotlist MF workspace id is passed to the repository")
    public void testSlMaedchenfilm() throws SmartsheetException {
        slCell.setValue(true);
        slRefCell.setValue(false);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).copySheetToWorkspace(anyLong(), eq(testIds.get(Id.SHOTLIST_WORKSPACE_MF)), any(), any());
    }

    @Test
    @DisplayName("if there is a new SL checkmark and the label is 'Eleven', the Shotlist Eleven workspace id is passed to the repository")
    public void testSlEleven() throws SmartsheetException {
        slCell.setValue(true);
        slRefCell.setValue(false);
        labelCell.setValue("Eleven");

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).copySheetToWorkspace(anyLong(), eq(testIds.get(Id.SHOTLIST_WORKSPACE_ELEVEN)), any(), any());
    }

    @Test
    @DisplayName("if there is a new SL checkmark, the combined name is passed to the repository")
    public void testSlCombinedName() throws SmartsheetException {
        slCell.setValue(true);
        slRefCell.setValue(false);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).copySheetToWorkspace(anyLong(), anyLong(), eq(jobNumberCell.getDisplayValue()+ "_" + clientCell.getDisplayValue()+ "_" + projectCell.getDisplayValue()), any());
    }

    @Test
    @DisplayName("if there is a new SL checkmark, the appendix 'Shotlist' is passed to the repository")
    public void testSlNameAppendix() throws SmartsheetException {
        slCell.setValue(true);
        slRefCell.setValue(false);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).copySheetToWorkspace(anyLong(), anyLong(), any(), eq("Shotlist"));
    }
}
