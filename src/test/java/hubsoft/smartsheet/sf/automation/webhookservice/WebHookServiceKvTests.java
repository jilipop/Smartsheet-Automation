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
public class WebHookServiceKvTests {

    private static final Random random = new Random();

    private static final Sheet testInputSheet = new Sheet(random.nextLong());
    private static final Sheet testRefSheet = new Sheet(testInputSheet.getId());
    private static final Sheet testFinanzSheet = new Sheet(random.nextLong());
    private static final Sheet someSheet1 = new Sheet(random.nextLong());
    private static final Sheet someSheet2 = new Sheet(random.nextLong());

    private static List<Sheet> testTargetSheets;

    private static final Column kvColumn = new Column(random.nextLong());
    private static final Column tColumn = new Column(random.nextLong());
    private static final Column slColumn = new Column(random.nextLong());
    private static final Column jobNumberColumn = new Column(random.nextLong());
    private static final Column clientColumn = new Column(random.nextLong());
    private static final Column labelColumn = new Column(random.nextLong());
    private static final Column projectColumn = new Column(random.nextLong());
    private static final Column aspColumn = new Column(random.nextLong());
    private static final Column agencyColumn = new Column(random.nextLong());

    private static final Column positionColumn = new Column(random.nextLong());
    private static final Column recipientColumn = new Column(random.nextLong());

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

    private static final Cell finanzSheetPositionCell = new Cell();
    private static final Cell finanzSheetRecipientCell = new Cell();

    private static final Row testRow = new Row(random.nextLong());
    private static final Row testRefRow = new Row(testRow.getId());

    private static final Row testFinanzSheetRow = new Row(random.nextLong());

    private static final Folder testFolder = new Folder(random.nextLong());

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

        positionColumn.setTitle("Position");
        recipientColumn.setTitle("Empfänger");

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

        finanzSheetPositionCell.setColumnId(positionColumn.getId());
        finanzSheetRecipientCell.setColumnId(recipientColumn.getId());

        testInputSheet.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, aspColumn, agencyColumn, kvColumn, tColumn, slColumn));
        testRefSheet.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, aspColumn, agencyColumn, kvColumn, tColumn, slColumn));

        testFinanzSheet.setColumns(List.of(positionColumn, recipientColumn));
        testFinanzSheetRow.setColumns(List.of(positionColumn, recipientColumn));
        testFinanzSheetRow.setCells(List.of(finanzSheetPositionCell, finanzSheetRecipientCell));
        testFinanzSheet.setRows(List.of(testFinanzSheetRow));
        testFinanzSheet.setName("Test_testFinanzen_test");
        someSheet1.setName("someSheet1");
        someSheet2.setName("someSheet2");

        testTargetSheets = List.of(someSheet1, testFinanzSheet, someSheet2);

        testRow.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, aspColumn, agencyColumn, kvColumn, tColumn, slColumn));
        testRow.setCells(List.of(jobNumberCell, clientCell, labelCell, projectCell, aspCell, agencyCell, kvCell, tCell, slCell));
        testInputSheet.setRows(List.of(testRow));

        testRefRow.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, aspColumn, agencyColumn, kvColumn, tColumn, slColumn));
        testRefRow.setCells(List.of(kvRefCell, tRefCell, slRefCell));
        testRefSheet.setRows(List.of(testRefRow));

        testIds.put(Id.TEMPLATE_FOLDER, random.nextLong());
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
        Boolean tBool = random.nextBoolean();
        Boolean slBool = random.nextBoolean();
        tCell.setValue(tBool);
        slCell.setValue(slBool);
        tRefCell.setValue(tBool);
        slRefCell.setValue(slBool);
    }

    @BeforeEach
    public void stubGetSheet() throws SmartsheetException {
        Mockito.when(mockRepository.getSheet(testInputSheet.getId())).thenReturn(testInputSheet);
    }

    @Test
    @DisplayName("if there is a new KV checkmark, the correct source folder id is passed to the repository")
    public void testKvSourceFolder() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).copyFolderToWorkspace(eq(testIds.get(Id.TEMPLATE_FOLDER)), anyLong(), any());
    }

    @Test
    @DisplayName("if there is a new KV checkmark and the label is 'Mädchenfilm', the correct destination workspace id is passed to the repository")
    public void testKvMaedchenfilm() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).copyFolderToWorkspace(anyLong(), eq(testIds.get(Id.MF_WORKSPACE)), any());
    }

    @Test
    @DisplayName("if there is a new KV checkmark and the label is 'Eleven', the correct destination workspace id is passed to the repository")
    public void testKvEleven() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        labelCell.setValue("Eleven");

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).copyFolderToWorkspace(anyLong(), eq(testIds.get(Id.ELEVEN_WORKSPACE)), any());
    }

    @Test
    @DisplayName("if there is a new KV checkmark, the combined name is passed to the repository")
    public void testKvCombinedName() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).copyFolderToWorkspace(anyLong(), anyLong(), eq(jobNumberCell.getDisplayValue()+ "_" + clientCell.getDisplayValue()+ "_" + projectCell.getDisplayValue()));
    }

    @Test
    @DisplayName("if there is a new KV checkmark, the correct folder id is passed to repository.renameSheets")
    public void testKvSheetRenameFolderId() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        Mockito.when(mockRepository.copyFolderToWorkspace(anyLong(), anyLong(), any())).thenReturn(testFolder);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).renameSheets(eq(testFolder.getId()), any());
    }

    @Test
    @DisplayName("if there is a new KV checkmark, the correct combined name is passed to repository.renameSheets")
    public void testKvSheetRenameCombinedName() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        Mockito.when(mockRepository.copyFolderToWorkspace(anyLong(), anyLong(), any())).thenReturn(testFolder);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).renameSheets(anyLong(), eq(jobNumberCell.getDisplayValue()+ "_" + clientCell.getDisplayValue()+ "_" + projectCell.getDisplayValue()));
    }

    @Test
    @DisplayName("if there is a new KV checkmark, 'Finanzsheet' is found and passed to repository.loadSheetWithRelevantRows")
    public void testKvLoadFinanzSheet() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        Mockito.when(mockRepository.copyFolderToWorkspace(anyLong(), anyLong(), any())).thenReturn(testFolder);
        Mockito.when(mockRepository.renameSheets(anyLong(), any())).thenReturn(testTargetSheets);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).loadSheetWithRelevantRows(eq(testFinanzSheet), any());
    }

    @Test
    @DisplayName("if there is a new KV checkmark, Set.of(1) is passed to repository.loadSheetWithRelevantRows to reduce bandwidth usage")
    public void testKvLoadFinanzSheetOnlyRelevantRow() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        Mockito.when(mockRepository.copyFolderToWorkspace(anyLong(), anyLong(), any())).thenReturn(testFolder);
        Mockito.when(mockRepository.renameSheets(anyLong(), any())).thenReturn(testTargetSheets);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).loadSheetWithRelevantRows(any(), eq(Set.of(1)));
    }

    @Test
    @DisplayName("if there is a new KV checkmark, 'Finanzsheet' is passed to repository.insertRowsIntoSheet")
    public void testKvInsertRowsFinanzSheet() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        Mockito.when(mockRepository.copyFolderToWorkspace(anyLong(), anyLong(), any())).thenReturn(testFolder);
        Mockito.when(mockRepository.renameSheets(anyLong(), any())).thenReturn(testTargetSheets);
        Mockito.when(mockRepository.loadSheetWithRelevantRows(any(), any())).thenReturn(testFinanzSheet);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).insertRowsIntoSheet(eq(testFinanzSheet), any());
    }

    @Test
    @DisplayName("if there is a new KV checkmark, the project name is correctly inserted into 'Finanzsheet'")
    public void testKvInsertProjectName() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        Mockito.when(mockRepository.copyFolderToWorkspace(anyLong(), anyLong(), any())).thenReturn(testFolder);
        Mockito.when(mockRepository.renameSheets(anyLong(), any())).thenReturn(testTargetSheets);
        Mockito.when(mockRepository.loadSheetWithRelevantRows(any(), any())).thenReturn(testFinanzSheet);
        ArgumentCaptor<List<Row>> updatedRowCaptor = ArgumentCaptor.forClass((Class) List.class);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).insertRowsIntoSheet(any(), updatedRowCaptor.capture());
        Row updatedRow = updatedRowCaptor.getValue().get(0);
        Cell updatedPositionCell = getCellByColumn(updatedRow.getCells(), positionColumn);
        assertThat(updatedPositionCell.getValue()).isEqualTo(projectCell.getDisplayValue());
    }

    @Test
    @DisplayName("if there is a new KV checkmark, the recipient is correctly inserted into 'Finanzsheet'")
    public void testKvInsertAsp() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        Mockito.when(mockRepository.copyFolderToWorkspace(anyLong(), anyLong(), any())).thenReturn(testFolder);
        Mockito.when(mockRepository.renameSheets(anyLong(), any())).thenReturn(testTargetSheets);
        Mockito.when(mockRepository.loadSheetWithRelevantRows(any(), any())).thenReturn(testFinanzSheet);
        ArgumentCaptor<List<Row>> updatedRowCaptor = ArgumentCaptor.forClass((Class) List.class);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).insertRowsIntoSheet(any(), updatedRowCaptor.capture());
        Row updatedRow = updatedRowCaptor.getValue().get(0);
        Cell updatedRecipientCell = getCellByColumn(updatedRow.getCells(), recipientColumn);
        assertThat(updatedRecipientCell.getValue()).isEqualTo(aspCell.getDisplayValue());
    }
}
