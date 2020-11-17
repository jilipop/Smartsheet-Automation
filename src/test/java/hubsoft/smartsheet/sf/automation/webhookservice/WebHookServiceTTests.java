package hubsoft.smartsheet.sf.automation.webhookservice;

import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Cell;
import com.smartsheet.api.models.Column;
import com.smartsheet.api.models.Row;
import com.smartsheet.api.models.Sheet;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.EnumMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static hubsoft.smartsheet.sf.automation.webhookservice.WebHookServiceGeneralTests.getCellByColumn;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class WebHookServiceTTests {

    private static final Random random = new Random();

    private static final Sheet testInputSheet = new Sheet(random.nextLong());
    private static final Sheet testRefSheet = new Sheet(testInputSheet.getId());
    private static final Sheet testTimingSheet = new Sheet(random.nextLong());

    private static final Column kvColumn = new Column(random.nextLong());
    private static final Column tColumn = new Column(random.nextLong());
    private static final Column slColumn = new Column(random.nextLong());
    private static final Column jobNumberColumn = new Column(random.nextLong());
    private static final Column clientColumn = new Column(random.nextLong());
    private static final Column labelColumn = new Column(random.nextLong());
    private static final Column projectColumn = new Column(random.nextLong());
    private static final Column aspColumn = new Column(random.nextLong());
    private static final Column agencyColumn = new Column(random.nextLong());

    private static final Column phaseColumn = new Column(random.nextLong());

    private static final Cell jobNumberCell = new Cell();
    private static final Cell clientCell = new Cell();
    private static final Cell labelCell = new Cell();
    private static final Cell projectCell = new Cell();
    private static final Cell kvCell = new Cell();
    private static final Cell tCell = new Cell();
    private static final Cell slCell = new Cell();
    private static final Cell aspCell = new Cell();
    private static final Cell agencyCell = new Cell();

    private static final Cell kvRefCell = new Cell();
    private static final Cell tRefCell = new Cell();
    private static final Cell slRefCell = new Cell();

    private static final Cell timingSheetProjectCell = new Cell();
    private static final Cell timingSheetJobNumberCell = new Cell();
    private static final Cell timingSheetClientNameCell = new Cell();
    private static final Cell timingSheetAgencyCell = new Cell();
    private static final Cell timingSheetAspCell = new Cell();

    private static final Row testRow = new Row(random.nextLong());
    private static final Row testRefRow = new Row(testRow.getId());

    private static final Row testTimingSheetRow1 = new Row(random.nextLong());
    private static final Row testTimingSheetRow2 = new Row(random.nextLong());
    private static final Row testTimingSheetRow3 = new Row(random.nextLong());
    private static final Row testTimingSheetRow4 = new Row(random.nextLong());
    private static final Row testTimingSheetRow5 = new Row(random.nextLong());

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
        aspColumn.setTitle("ASP");
        agencyColumn.setTitle("Agentur");

        phaseColumn.setTitle("Phase");

        jobNumberCell.setColumnId(jobNumberColumn.getId());
        clientCell.setColumnId(clientColumn.getId());
        labelCell.setColumnId(labelColumn.getId());
        projectCell.setColumnId(projectColumn.getId());
        aspCell.setColumnId(aspColumn.getId());
        agencyCell.setColumnId(agencyColumn.getId());
        kvCell.setColumnId(kvColumn.getId());
        tCell.setColumnId(tColumn.getId());
        slCell.setColumnId(slColumn.getId());

        kvRefCell.setColumnId(kvColumn.getId());
        tRefCell.setColumnId(tColumn.getId());
        slRefCell.setColumnId(slColumn.getId());

        timingSheetProjectCell.setColumnId(phaseColumn.getId());
        timingSheetJobNumberCell.setColumnId(phaseColumn.getId());
        timingSheetClientNameCell.setColumnId(phaseColumn.getId());
        timingSheetAgencyCell.setColumnId(phaseColumn.getId());
        timingSheetAspCell.setColumnId(phaseColumn.getId());

        testInputSheet.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, aspColumn, agencyColumn, kvColumn, tColumn, slColumn));
        testRefSheet.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, aspColumn, agencyColumn, kvColumn, tColumn, slColumn));

        testTimingSheet.setColumns((List.of(phaseColumn)));
        testTimingSheetRow1.setColumns(List.of(phaseColumn));
        testTimingSheetRow1.setCells(List.of(timingSheetProjectCell));
        testTimingSheetRow2.setColumns(List.of(phaseColumn));
        testTimingSheetRow2.setCells(List.of(timingSheetJobNumberCell));
        testTimingSheetRow3.setColumns(List.of(phaseColumn));
        testTimingSheetRow3.setCells(List.of(timingSheetClientNameCell));
        testTimingSheetRow4.setColumns(List.of(phaseColumn));
        testTimingSheetRow4.setCells(List.of(timingSheetAgencyCell));
        testTimingSheetRow5.setColumns(List.of(phaseColumn));
        testTimingSheetRow5.setCells(List.of(timingSheetAspCell));
        testTimingSheet.setRows(List.of(testTimingSheetRow1, testTimingSheetRow2, testTimingSheetRow3, testTimingSheetRow4, testTimingSheetRow5));

        testRow.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, aspColumn, agencyColumn, kvColumn, tColumn, slColumn));
        testRow.setCells(List.of(jobNumberCell, clientCell, labelCell, projectCell, aspCell, agencyCell, kvCell, tCell, slCell));
        testInputSheet.setRows(List.of(testRow));

        testRefRow.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, aspColumn, agencyColumn, kvColumn, tColumn, slColumn));
        testRefRow.setCells(List.of(kvRefCell, tRefCell, slRefCell));
        testRefSheet.setRows(List.of(testRefRow));

        testIds.put(Id.TIMING_WORKSPACE_MF, random.nextLong());
        testIds.put(Id.TIMING_WORKSPACE_ELEVEN, random.nextLong());
        testIds.put(Id.TIMING_TEMPLATE, random.nextLong());
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
        aspCell.setDisplayValue("Max Mustermann, Martina Mustermann");
        agencyCell.setDisplayValue("Testagentur");
    }

    private void randomizeCheckmarks() {
        Boolean kvBool = random.nextBoolean();
        Boolean slBool = random.nextBoolean();
        kvCell.setValue(kvBool);
        slCell.setValue(slBool);
        kvRefCell.setValue(kvBool);
        slRefCell.setValue(slBool);
    }

    @BeforeEach
    public void stubGetSheet() throws SmartsheetException {
        Mockito.when(mockRepository.getSheet(testInputSheet.getId())).thenReturn(testInputSheet);
    }

    @Test
    @DisplayName("if there is a new T checkmark, the correct template id is passed to the repository")
    public void testTTemplate() throws SmartsheetException {
        tCell.setValue(true);
        tRefCell.setValue(false);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).copySheetToWorkspace(eq(testIds.get(Id.TIMING_TEMPLATE)), anyLong(), any(), any());
    }

    @Test
    @DisplayName("if there is a new T checkmark and the label is 'Mädchenfilm', the Timing MF workspace id is passed to the repository")
    public void testTMaedchenfilm() throws SmartsheetException {
        tCell.setValue(true);
        tRefCell.setValue(false);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).copySheetToWorkspace(anyLong(), eq(testIds.get(Id.TIMING_WORKSPACE_MF)), any(), any());
    }

    @Test
    @DisplayName("if there is a new T checkmark and the label is 'Eleven', the Timing Eleven workspace id is passed to the repository")
    public void testTEleven() throws SmartsheetException {
        tCell.setValue(true);
        tRefCell.setValue(false);
        labelCell.setValue("Eleven");

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).copySheetToWorkspace(anyLong(), eq(testIds.get(Id.TIMING_WORKSPACE_ELEVEN)), any(), any());
    }

    @Test
    @DisplayName("if there is a new T checkmark, the combined name is passed to the repository")
    public void testTCombinedName() throws SmartsheetException {
        tCell.setValue(true);
        tRefCell.setValue(false);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).copySheetToWorkspace(anyLong(), anyLong(), eq(jobNumberCell.getDisplayValue()+ "_" + clientCell.getDisplayValue()+ "_" + projectCell.getDisplayValue()), any());
    }

    @Test
    @DisplayName("if there is a new T checkmark, the appendix 'Timing' is passed to the repository")
    public void testTNameAppendix() throws SmartsheetException {
        tCell.setValue(true);
        tRefCell.setValue(false);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).copySheetToWorkspace(anyLong(), anyLong(), any(), eq("Timing"));
    }

    @Test
    @DisplayName("if there is a new T checkmark, the timing sheet is passed to repository.loadSheetWithRelevantRows")
    public void testTLoadTimingSheet() throws SmartsheetException {
        tCell.setValue(true);
        tRefCell.setValue(false);
        Mockito.when(mockRepository.copySheetToWorkspace(anyLong(), anyLong(), any(), any())).thenReturn(testTimingSheet);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).loadSheetWithRelevantRows(eq(testTimingSheet), any());
    }

    @Test
    @DisplayName("if there is a new T checkmark, Set.of(1, 2, 3, 4, 5) is passed to repository.loadSheetWithRelevantRows to reduce bandwidth usage")
    public void testTLoadTimingSheetOnlyRelevantRows() throws SmartsheetException {
        tCell.setValue(true);
        tRefCell.setValue(false);
        Mockito.when(mockRepository.copySheetToWorkspace(anyLong(), anyLong(), any(), any())).thenReturn(testTimingSheet);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).loadSheetWithRelevantRows(any(), eq(Set.of(1, 2, 3, 4, 5)));
    }

    @Test
    @DisplayName("if there is a new T checkmark, the timing sheet is passed to repository.insertRowsIntoSheet")
    public void testTInsertRowsTimingSheet() throws SmartsheetException {
        tCell.setValue(true);
        tRefCell.setValue(false);
        Mockito.when(mockRepository.copySheetToWorkspace(anyLong(), anyLong(), any(), any())).thenReturn(testTimingSheet);
        Mockito.when(mockRepository.loadSheetWithRelevantRows(any(), any())).thenReturn(testTimingSheet);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).insertRowsIntoSheet(eq(testTimingSheet), any());
    }

    @Test
    @DisplayName("if there is a new T checkmark, the project name is correctly inserted into the timing sheet")
    public void testTInsertProjectName() throws SmartsheetException {
        tCell.setValue(true);
        tRefCell.setValue(false);
        Mockito.when(mockRepository.copySheetToWorkspace(anyLong(), anyLong(), any(), any())).thenReturn(testTimingSheet);
        Mockito.when(mockRepository.loadSheetWithRelevantRows(any(), any())).thenReturn(testTimingSheet);
        ArgumentCaptor<List<Row>> updatedRowsCaptor = ArgumentCaptor.forClass((Class) List.class);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).insertRowsIntoSheet(any(), updatedRowsCaptor.capture());
        Row updatedRow = updatedRowsCaptor.getValue().get(0);
        Cell updatedProjectNameCell = getCellByColumn(updatedRow.getCells(), phaseColumn);
        assertThat(updatedProjectNameCell.getValue()).isEqualTo(projectCell.getDisplayValue());
    }

    @Test
    @DisplayName("if there is a new T checkmark, the job number is correctly inserted into the timing sheet")
    public void testTInsertJobNumber() throws SmartsheetException {
        tCell.setValue(true);
        tRefCell.setValue(false);
        Mockito.when(mockRepository.copySheetToWorkspace(anyLong(), anyLong(), any(), any())).thenReturn(testTimingSheet);
        Mockito.when(mockRepository.loadSheetWithRelevantRows(any(), any())).thenReturn(testTimingSheet);
        ArgumentCaptor<List<Row>> updatedRowsCaptor = ArgumentCaptor.forClass((Class) List.class);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).insertRowsIntoSheet(any(), updatedRowsCaptor.capture());
        Row updatedRow = updatedRowsCaptor.getValue().get(1);
        Cell updatedJobNumberCell = getCellByColumn(updatedRow.getCells(), phaseColumn);
        assertThat(updatedJobNumberCell.getValue()).isEqualTo(jobNumberCell.getDisplayValue());
    }

    @Test
    @DisplayName("if there is a new T checkmark, the client name is correctly inserted into the timing sheet")
    public void testTInsertClientName() throws SmartsheetException {
        tCell.setValue(true);
        tRefCell.setValue(false);
        Mockito.when(mockRepository.copySheetToWorkspace(anyLong(), anyLong(), any(), any())).thenReturn(testTimingSheet);
        Mockito.when(mockRepository.loadSheetWithRelevantRows(any(), any())).thenReturn(testTimingSheet);
        ArgumentCaptor<List<Row>> updatedRowsCaptor = ArgumentCaptor.forClass((Class) List.class);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).insertRowsIntoSheet(any(), updatedRowsCaptor.capture());
        Row updatedRow = updatedRowsCaptor.getValue().get(2);
        Cell updatedClientNameCell = getCellByColumn(updatedRow.getCells(), phaseColumn);
        assertThat(updatedClientNameCell.getValue()).isEqualTo(clientCell.getDisplayValue());
    }

    @Test
    @DisplayName("if there is a new T checkmark, the agency is correctly inserted into the timing sheet")
    public void testTInsertAgency() throws SmartsheetException {
        tCell.setValue(true);
        tRefCell.setValue(false);
        Mockito.when(mockRepository.copySheetToWorkspace(anyLong(), anyLong(), any(), any())).thenReturn(testTimingSheet);
        Mockito.when(mockRepository.loadSheetWithRelevantRows(any(), any())).thenReturn(testTimingSheet);
        ArgumentCaptor<List<Row>> updatedRowsCaptor = ArgumentCaptor.forClass((Class) List.class);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).insertRowsIntoSheet(any(), updatedRowsCaptor.capture());
        Row updatedRow = updatedRowsCaptor.getValue().get(3);
        Cell updatedAgencyCell = getCellByColumn(updatedRow.getCells(), phaseColumn);
        assertThat(updatedAgencyCell.getValue()).isEqualTo("Agentur: " + agencyCell.getDisplayValue());
    }

    @Test
    @DisplayName("if there is a new T checkmark, the producer is correctly inserted into the timing sheet")
    public void testTInsertProducer() throws SmartsheetException {
        tCell.setValue(true);
        tRefCell.setValue(false);
        Mockito.when(mockRepository.copySheetToWorkspace(anyLong(), anyLong(), any(), any())).thenReturn(testTimingSheet);
        Mockito.when(mockRepository.loadSheetWithRelevantRows(any(), any())).thenReturn(testTimingSheet);
        ArgumentCaptor<List<Row>> updatedRowsCaptor = ArgumentCaptor.forClass((Class) List.class);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).insertRowsIntoSheet(any(), updatedRowsCaptor.capture());
        Row updatedRow = updatedRowsCaptor.getValue().get(4);
        Cell updatedProducerCell = getCellByColumn(updatedRow.getCells(), phaseColumn);
        assertThat(updatedProducerCell.getValue()).isEqualTo("Producer: " + aspCell.getDisplayValue());
    }
}
