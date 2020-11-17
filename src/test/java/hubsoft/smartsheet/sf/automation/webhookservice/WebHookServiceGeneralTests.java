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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class WebHookServiceGeneralTests {

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
    private static final Column aspColumn = new Column(random.nextLong());
    private static final Column agencyColumn = new Column(random.nextLong());

    private static final Cell kvCell = new Cell();
    private static final Cell tCell = new Cell();
    private static final Cell slCell = new Cell();
    private static final Cell jobNumberCell = new Cell();
    private static final Cell clientCell = new Cell();
    private static final Cell labelCell = new Cell();
    private static final Cell projectCell = new Cell();
    private static final Cell aspCell = new Cell();
    private static final Cell agencyCell = new Cell();

    private static final Cell kvCell2 = new Cell();
    private static final Cell tCell2 = new Cell();
    private static final Cell slCell2 = new Cell();
    private static final Cell jobNumberCell2 = new Cell();
    private static final Cell clientCell2 = new Cell();
    private static final Cell labelCell2 = new Cell();
    private static final Cell projectCell2 = new Cell();
    private static final Cell aspCell2 = new Cell();
    private static final Cell agencyCell2 = new Cell();

    private static final Cell kvRefCell = new Cell();
    private static final Cell tRefCell = new Cell();
    private static final Cell slRefCell = new Cell();
    private static final Cell jobNumberRefCell = new Cell();
    private static final Cell clientRefCell = new Cell();
    private static final Cell labelRefCell = new Cell();
    private static final Cell projectRefCell = new Cell();
    private static final Cell aspRefCell = new Cell();
    private static final Cell agencyRefCell = new Cell();

    private static final Cell kvRefCell2 = new Cell();
    private static final Cell tRefCell2 = new Cell();
    private static final Cell slRefCell2 = new Cell();
    private static final Cell jobNumberRefCell2 = new Cell();
    private static final Cell clientRefCell2 = new Cell();
    private static final Cell labelRefCell2 = new Cell();
    private static final Cell projectRefCell2 = new Cell();
    private static final Cell aspRefCell2 = new Cell();
    private static final Cell agencyRefCell2 = new Cell();

    private static final Row testRow = new Row(random.nextLong());
    private static final Row testRow2 = new Row(random.nextLong());
    private static final Row testRefRow = new Row(testRow.getId());
    private static final Row testRefRow2 = new Row(testRow2.getId());

    private static final Folder testFolder = new Folder(random.nextLong());

    private static final EnumMap<Id, Long> testIds = new EnumMap<>(Id.class);

    private Boolean kvBool;
    private Boolean tBool;
    private Boolean slBool;

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
        aspColumn.setTitle("ASP");
        agencyColumn.setTitle("Agentur");
        kvColumn.setTitle("KV");
        tColumn.setTitle("T");
        slColumn.setTitle("SL");

        jobNumberCell.setColumnId(jobNumberColumn.getId());
        clientCell.setColumnId(clientColumn.getId());
        labelCell.setColumnId(labelColumn.getId());
        projectCell.setColumnId(projectColumn.getId());
        aspCell.setColumnId(aspColumn.getId());
        agencyCell.setColumnId(agencyColumn.getId());
        kvCell.setColumnId(kvColumn.getId());
        tCell.setColumnId(tColumn.getId());
        slCell.setColumnId(slColumn.getId());
        jobNumberCell2.setColumnId(jobNumberColumn.getId());
        clientCell2.setColumnId(clientColumn.getId());
        labelCell2.setColumnId(labelColumn.getId());
        projectCell2.setColumnId(projectColumn.getId());
        aspCell2.setColumnId(aspColumn.getId());
        agencyCell2.setColumnId(agencyColumn.getId());
        kvCell2.setColumnId(kvColumn.getId());
        tCell2.setColumnId(tColumn.getId());
        slCell2.setColumnId(slColumn.getId());
        jobNumberRefCell.setColumnId(jobNumberColumn.getId());
        clientRefCell.setColumnId(clientColumn.getId());
        labelRefCell.setColumnId(labelColumn.getId());
        projectRefCell.setColumnId(projectColumn.getId());
        aspRefCell.setColumnId(aspColumn.getId());
        agencyRefCell.setColumnId(agencyColumn.getId());
        kvRefCell.setColumnId(kvColumn.getId());
        tRefCell.setColumnId(tColumn.getId());
        slRefCell.setColumnId(slColumn.getId());
        jobNumberRefCell2.setColumnId(jobNumberColumn.getId());
        clientRefCell2.setColumnId(clientColumn.getId());
        labelRefCell2.setColumnId(labelColumn.getId());
        projectRefCell2.setColumnId(projectColumn.getId());
        aspRefCell2.setColumnId(aspColumn.getId());
        agencyRefCell2.setColumnId(agencyColumn.getId());
        kvRefCell2.setColumnId(kvColumn.getId());
        tRefCell2.setColumnId(tColumn.getId());
        slRefCell2.setColumnId(slColumn.getId());

        testInputSheet.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, aspColumn, agencyColumn, kvColumn, tColumn, slColumn));
        testRefSheet.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, aspColumn, agencyColumn, kvColumn, tColumn, slColumn));

        testRow.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, aspColumn, agencyColumn, kvColumn, tColumn, slColumn));
        testRow2.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, aspColumn, agencyColumn, kvColumn, tColumn, slColumn));
        testRow.setCells(List.of(jobNumberCell, clientCell, labelCell, projectCell, aspCell, agencyCell, kvCell, tCell, slCell));
        testRow2.setCells(List.of(jobNumberCell2, clientCell2, labelCell2, projectCell2, aspCell2, agencyCell2, kvCell2, tCell2, slCell2));
        testInputSheet.setRows(List.of(testRow, testRow2));

        testRefRow.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, aspColumn, agencyColumn, kvColumn, tColumn, slColumn));
        testRefRow2.setColumns(List.of(jobNumberColumn, clientColumn, labelColumn, projectColumn, aspColumn, agencyColumn, kvColumn, tColumn, slColumn));
        testRefRow.setCells(List.of(jobNumberRefCell, clientRefCell, labelRefCell, projectRefCell, aspRefCell, agencyRefCell, kvRefCell, tRefCell, slRefCell));
        testRefRow2.setCells(List.of(jobNumberRefCell2, clientRefCell2, labelRefCell2, projectRefCell2, aspRefCell2, agencyRefCell2, kvRefCell2, tRefCell2, slRefCell2));
        testRefSheet.setRows(List.of(testRefRow, testRefRow2));

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
        jobNumberRefCell.setDisplayValue("12345");
        jobNumberCell2.setDisplayValue("54321");
        jobNumberRefCell2.setDisplayValue("54321");
        clientCell.setDisplayValue("Testkunde");
        clientRefCell.setDisplayValue("Testkunde");
        clientCell2.setDisplayValue("Testkunde2");
        clientRefCell2.setDisplayValue("Testkunde2");

        labelCell.setValue("Mädchenfilm");
        labelRefCell.setValue("Mädchenfilm");
        labelCell2.setValue("Eleven");
        labelRefCell2.setValue("Eleven");

        projectCell.setDisplayValue("Testname");
        projectRefCell.setDisplayValue("Testname");
        projectCell2.setDisplayValue("Testname2");
        projectRefCell2.setDisplayValue("Testname2");
        aspCell.setDisplayValue("Max Mustermann, Martina Mustermann");
        aspRefCell.setDisplayValue("Max Mustermann, Martina Mustermann");
        aspCell2.setDisplayValue("John Doe, Jane Doe");
        aspRefCell2.setDisplayValue("John Doe, Jane Doe");
        agencyCell.setDisplayValue("Testagentur");
        agencyRefCell.setDisplayValue("Testagentur");
        agencyCell2.setDisplayValue("Testagentur2");
        agencyRefCell2.setDisplayValue("Testagentur2");
    }

    private void randomizeCheckmarks() {
        kvBool = random.nextBoolean();
        tBool = random.nextBoolean();
        slBool = random.nextBoolean();
        Boolean kvBool2 = random.nextBoolean();
        Boolean tBool2 = random.nextBoolean();
        Boolean slBool2 = random.nextBoolean();

        kvCell.setValue(kvBool);
        tCell.setValue(tBool);
        slCell.setValue(slBool);
        kvCell2.setValue(kvBool2);
        tCell2.setValue(tBool2);
        slCell2.setValue(slBool2);

        kvRefCell.setValue(kvBool);
        tRefCell.setValue(tBool);
        slRefCell.setValue(slBool);
        kvRefCell2.setValue(kvBool2);
        tRefCell2.setValue(tBool2);
        slRefCell2.setValue(slBool2);
    }

    @BeforeEach
    public void stubGetSheet() throws SmartsheetException {
        Mockito.when(mockRepository.getSheet(testInputSheet.getId())).thenReturn(testInputSheet);
    }

    @Test
    @DisplayName("if there are no new checkmarks in columns KV/T/SL, no changes are made to the Smartsheet account nor the reference sheet checkmarks")
    public void testNoCheckmarks() throws SmartsheetException {
        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).getSheet(testInputSheet.getId());
        Mockito.verifyNoMoreInteractions(mockRepository);
        List<Cell> cells = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(0).getCells();
        assertThat(getCellByColumn(cells, kvColumn).getValue()).isEqualTo(kvBool);
        assertThat(getCellByColumn(cells, tColumn).getValue()).isEqualTo(tBool);
        assertThat(getCellByColumn(cells, slColumn).getValue()).isEqualTo(slBool);
    }

    @Test
    @DisplayName("if checkmarks are removed, no changes are made to the Smartsheet account, but the reference sheet is updated")
    public void testRemoveCheckMarks() throws SmartsheetException {
        kvCell.setValue(false);
        tCell.setValue(false);
        slCell.setValue(false);
        kvRefCell.setValue(true);
        tRefCell.setValue(true);
        slRefCell.setValue(true);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).getSheet(testInputSheet.getId());
        Mockito.verifyNoMoreInteractions(mockRepository);
        List<Cell> cells = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(0).getCells();
        List <Cell> boolCells = cells.stream()
                .filter(cell -> (cell.equals(getCellByColumn(cells, kvColumn)) ||
                        cell.equals(getCellByColumn(cells, tColumn))) ||
                        cell.equals(getCellByColumn(cells, slColumn)))
                .collect(Collectors.toList());
        for (Cell cell: boolCells)
            assertThat(cell.getValue()).isEqualTo(false);
    }

    @Test
    @DisplayName("if there are multiple rows with new checkmarks they are all processed")
    public void testMultipleRows() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        Mockito.when(mockRepository.copyFolderToWorkspace(anyLong(), anyLong(), any())).thenReturn(testFolder);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository, Mockito.times(2)).copyFolderToWorkspace(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("if there is a new checkmark but the label entry is missing, the row is skipped but other rows are processed")
    public void testLabelNullSkipRow() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        labelCell.setValue(null);
        Mockito.when(mockRepository.copyFolderToWorkspace(anyLong(), anyLong(), any())).thenReturn(testFolder);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository, Mockito.times(1)).copyFolderToWorkspace(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("if there is a new checkmark but the label entry is missing, the reference sheet is updated")
    public void testLabelNullRefSheetUpdate() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        labelCell.setValue(null);
        Mockito.when(mockRepository.copyFolderToWorkspace(anyLong(), anyLong(), any())).thenReturn(testFolder);

        webHookService.processTemplates(testInputSheet.getId());

        List<Cell> row1Cells = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(0).getCells();
        List<Cell> row2Cells = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(1).getCells();
        Cell row1KvRefCell = getCellByColumn(row1Cells, kvColumn);
        Cell row1LabelRefCell = getCellByColumn(row1Cells, labelColumn);
        Cell row2KvRefCell = getCellByColumn(row2Cells, kvColumn);
        assertThat(row1KvRefCell.getValue()).isEqualTo(true);
        assertThat(row2KvRefCell.getValue()).isEqualTo(true);
        assertThat(row1LabelRefCell.getValue()).isEqualTo(null);
    }

    @Test
    @DisplayName("if there is a new checkmark but the label is neither 'Mädchenfilm' nor 'Eleven', the row is skipped but other rows are processed")
    public void testLabelUnknownSkipRow() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        labelCell.setValue("Sesamstraße");
        Mockito.when(mockRepository.copyFolderToWorkspace(anyLong(), anyLong(), any())).thenReturn(testFolder);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository, Mockito.times(1)).copyFolderToWorkspace(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("if there is a new checkmark but the label is neither 'Mädchenfilm' nor 'Eleven', the reference sheet is updated")
    public void testLabelUnknownRefSheetUpdate() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        labelCell.setValue("Sesamstraße");
        Mockito.when(mockRepository.copyFolderToWorkspace(anyLong(), anyLong(), any())).thenReturn(testFolder);

        webHookService.processTemplates(testInputSheet.getId());

        List<Cell> row1Cells = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(0).getCells();
        List<Cell> row2Cells = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(1).getCells();
        Cell row1KvRefCell = getCellByColumn(row1Cells, kvColumn);
        Cell row1LabelRefCell = getCellByColumn(row1Cells, labelColumn);
        Cell row2KvRefCell = getCellByColumn(row2Cells, kvColumn);
        assertThat(row1KvRefCell.getValue()).isEqualTo(true);
        assertThat(row2KvRefCell.getValue()).isEqualTo(true);
        assertThat(row1LabelRefCell.getValue()).isEqualTo("Sesamstraße");
    }

    @Test
    @DisplayName("if there is a new checkmark but the job number is missing, no changes are made to the Smartsheet account")
    public void testMissingJobNumberNoSmartsheetChanges() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        jobNumberCell.setDisplayValue(null);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).getSheet(testInputSheet.getId());
        Mockito.verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("if there is a new checkmark but the job number is missing, no changes are made to the reference sheet checkmarks")
    public void testMissingJobNumberNoRefSheetChanges() {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        jobNumberCell.setDisplayValue(null);

        webHookService.processTemplates(testInputSheet.getId());

        List<Cell> cellsRow1 = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(0).getCells();
        assertThat(getCellByColumn(cellsRow1, kvColumn).getValue()).isEqualTo(false);
        assertThat(getCellByColumn(cellsRow1, jobNumberColumn).getDisplayValue()).isEqualTo("12345");
        List<Cell> cellsRow2 = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(1).getCells();
        assertThat(getCellByColumn(cellsRow2, kvColumn).getValue()).isEqualTo(false);
        assertThat(getCellByColumn(cellsRow2, jobNumberColumn).getDisplayValue()).isEqualTo("54321");
    }

    @Test
    @DisplayName("if there is a new checkmark but the job number is empty, no changes are made to the Smartsheet account")
    public void testEmptyJobNumberNoSmartsheetChanges() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        jobNumberCell.setDisplayValue("");

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).getSheet(testInputSheet.getId());
        Mockito.verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("if there is a new checkmark but the job number is empty, no changes are made to the reference sheet checkmarks")
    public void testEmptyJobNumberNoRefSheetChanges() {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        jobNumberCell.setDisplayValue("");

        webHookService.processTemplates(testInputSheet.getId());

        List<Cell> cellsRow1 = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(0).getCells();
        assertThat(getCellByColumn(cellsRow1, kvColumn).getValue()).isEqualTo(false);
        assertThat(getCellByColumn(cellsRow1, jobNumberColumn).getDisplayValue()).isEqualTo("12345");
        List<Cell> cellsRow2 = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(1).getCells();
        assertThat(getCellByColumn(cellsRow2, kvColumn).getValue()).isEqualTo(false);
        assertThat(getCellByColumn(cellsRow2, jobNumberColumn).getDisplayValue()).isEqualTo("54321");
    }

    @Test
    @DisplayName("if there is a new checkmark but the project name is missing, no changes are made to the Smartsheet account")
    public void testMissingProjectNameNoSmartsheetChanges() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        projectCell.setDisplayValue(null);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).getSheet(testInputSheet.getId());
        Mockito.verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("if there is a new checkmark but the project name is missing, no changes are made to the reference sheet checkmarks")
    public void testMissingProjectNameNoRefSheetChanges() {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        projectCell.setDisplayValue(null);

        webHookService.processTemplates(testInputSheet.getId());

        List<Cell> cellsRow1 = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(0).getCells();
        assertThat(getCellByColumn(cellsRow1, kvColumn).getValue()).isEqualTo(false);
        assertThat(getCellByColumn(cellsRow1, projectColumn).getDisplayValue()).isEqualTo("Testname");
        List<Cell> cellsRow2 = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(1).getCells();
        assertThat(getCellByColumn(cellsRow2, kvColumn).getValue()).isEqualTo(false);
        assertThat(getCellByColumn(cellsRow2, projectColumn).getDisplayValue()).isEqualTo("Testname2");
    }

    @Test
    @DisplayName("if there is a new checkmark but the project name is empty, no changes are made to the Smartsheet account")
    public void testEmptyProjectNameNoSmartsheetChanges() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        projectCell.setDisplayValue("");

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).getSheet(testInputSheet.getId());
        Mockito.verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("if there is a new checkmark but the project name is empty, no changes are made to the reference sheet checkmarks")
    public void testEmptyProjectNameNoRefSheetChanges() {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        projectCell.setDisplayValue("");

        webHookService.processTemplates(testInputSheet.getId());

        List<Cell> cellsRow1 = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(0).getCells();
        assertThat(getCellByColumn(cellsRow1, kvColumn).getValue()).isEqualTo(false);
        assertThat(getCellByColumn(cellsRow1, projectColumn).getDisplayValue()).isEqualTo("Testname");
        List<Cell> cellsRow2 = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(1).getCells();
        assertThat(getCellByColumn(cellsRow2, kvColumn).getValue()).isEqualTo(false);
        assertThat(getCellByColumn(cellsRow2, projectColumn).getDisplayValue()).isEqualTo("Testname2");
    }

    @Test
    @DisplayName("if there is a new checkmark but the client name is missing, no changes are made to the Smartsheet account")
    public void testMissingClientNameNoSmartsheetChanges() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        clientCell.setDisplayValue(null);

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).getSheet(testInputSheet.getId());
        Mockito.verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("if there is a new checkmark but the client name is missing, no changes are made to the reference sheet checkmarks")
    public void testMissingClientNameNoRefSheetChanges() {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        clientCell.setDisplayValue(null);

        webHookService.processTemplates(testInputSheet.getId());

        List<Cell> cellsRow1 = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(0).getCells();
        assertThat(getCellByColumn(cellsRow1, kvColumn).getValue()).isEqualTo(false);
        assertThat(getCellByColumn(cellsRow1, clientColumn).getDisplayValue()).isEqualTo("Testkunde");
        List<Cell> cellsRow2 = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(1).getCells();
        assertThat(getCellByColumn(cellsRow2, kvColumn).getValue()).isEqualTo(false);
        assertThat(getCellByColumn(cellsRow2, clientColumn).getDisplayValue()).isEqualTo("Testkunde2");
    }

    @Test
    @DisplayName("if there is a new checkmark but the client name is empty, no changes are made to the Smartsheet account")
    public void testEmptyClientNameNoSmartsheetChanges() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        clientCell.setDisplayValue("");

        webHookService.processTemplates(testInputSheet.getId());

        Mockito.verify(mockRepository).getSheet(testInputSheet.getId());
        Mockito.verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("if there is a new checkmark but the client name is empty, no changes are made to the reference sheet checkmarks")
    public void testEmptyClientNameNoRefSheetChanges() {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        kvCell2.setValue(true);
        kvRefCell2.setValue(false);
        clientCell.setDisplayValue("");

        webHookService.processTemplates(testInputSheet.getId());

        List<Cell> cellsRow1 = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(0).getCells();
        assertThat(getCellByColumn(cellsRow1, kvColumn).getValue()).isEqualTo(false);
        assertThat(getCellByColumn(cellsRow1, clientColumn).getDisplayValue()).isEqualTo("Testkunde");
        List<Cell> cellsRow2 = ReferenceSheets.getSheet(testInputSheet.getId()).getRows().get(1).getCells();
        assertThat(getCellByColumn(cellsRow2, kvColumn).getValue()).isEqualTo(false);
        assertThat(getCellByColumn(cellsRow2, clientColumn).getDisplayValue()).isEqualTo("Testkunde2");
    }

    @Test
    @DisplayName("if there is a new checkmark, the project name is long and the client name is short, the project name is abbreviated to keep the total length at 50 chars")
    public void testLongProjectName() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        projectCell.setDisplayValue("this is gonna be way too long, say 43 chars");
        final int maxFileNameLengthImposedBySmartsheet = 50;
        final int longestTemplateNameFixedChars = 23;
        int charsRemainingForProjectName = maxFileNameLengthImposedBySmartsheet - longestTemplateNameFixedChars - clientCell.getDisplayValue().length();
        String abbreviatedProjectName = projectCell.getDisplayValue().substring(0, charsRemainingForProjectName - 1);
        String combinedName = jobNumberCell.getDisplayValue()+ "_" + clientCell.getDisplayValue() + "_" + abbreviatedProjectName;

        webHookService.processTemplates(testInputSheet.getId());

        assertThat(combinedName.length() + "_Voreinstellungen".length()).isEqualTo(50);
        Mockito.verify(mockRepository).copyFolderToWorkspace(anyLong(), anyLong(), eq(combinedName));
    }

    @Test
    @DisplayName("if there is a new checkmark, the project name is short and the client name is long, the project name is abbreviated to keep the total length at 50 chars")
    public void testLongClientName() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        clientCell.setDisplayValue("21 chars are too many");
        final int maxFileNameLengthImposedBySmartsheet = 50;
        final int longestTemplateNameFixedChars = 23;
        int charsRemainingForProjectName = maxFileNameLengthImposedBySmartsheet - longestTemplateNameFixedChars - clientCell.getDisplayValue().length();
        String abbreviatedProjectName = projectCell.getDisplayValue().substring(0, charsRemainingForProjectName - 1);
        String combinedName = jobNumberCell.getDisplayValue()+ "_" + clientCell.getDisplayValue() + "_" + abbreviatedProjectName;

        webHookService.processTemplates(testInputSheet.getId());

        assertThat(combinedName.length() + "_Voreinstellungen".length()).isEqualTo(50);
        Mockito.verify(mockRepository).copyFolderToWorkspace(anyLong(), anyLong(), eq(combinedName));
    }


    @Test
    @DisplayName("if there is a new checkmark, the project name and the client name are long, the project name is abbreviated to 4 chars, then to keep the file name length at 50 chars")
    public void testLongProjectAndClientName() throws SmartsheetException {
        kvCell.setValue(true);
        kvRefCell.setValue(false);
        projectCell.setDisplayValue("this is gonna be way too long, say 43 chars");
        clientCell.setDisplayValue("26 chars are just too many");
        final int maxFileNameLengthImposedBySmartsheet = 50;
        final int longestTemplateNameFixedChars = 23;
        int charsRemainingForClientName = maxFileNameLengthImposedBySmartsheet - longestTemplateNameFixedChars - 4;
        String abbreviatedProjectName = projectCell.getDisplayValue().substring(0, 4);
        String abbreviatedClientName = clientCell.getDisplayValue().substring(0, charsRemainingForClientName -1);
        String combinedName = jobNumberCell.getDisplayValue()+ "_" + abbreviatedClientName + "_" + abbreviatedProjectName;
        System.out.println(combinedName);

        webHookService.processTemplates(testInputSheet.getId());

        assertThat(combinedName.length() + "_Voreinstellungen".length()).isEqualTo(50);
        Mockito.verify(mockRepository).copyFolderToWorkspace(anyLong(), anyLong(), eq(combinedName));
    }

    public static Cell getCellByColumn(List<Cell> cells, Column col) {
        return cells.stream()
                .filter(cell -> col.getId().equals(cell.getColumnId()))
                .findFirst()
                .orElse(null);
    }
}
