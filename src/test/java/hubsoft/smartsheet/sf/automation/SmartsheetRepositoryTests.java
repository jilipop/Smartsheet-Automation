package hubsoft.smartsheet.sf.automation;

import com.smartsheet.api.*;
import com.smartsheet.api.models.*;
import com.smartsheet.api.models.enums.DestinationType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class SmartsheetRepositoryTests {

    @Mock
    private Smartsheet mockSmartsheet;

    @InjectMocks
    private SmartsheetRepository repository;

    private static SheetResources mockSheetResources;
    private static FolderResources mockFolderResources;
    private static SheetRowResources mockRowResources;

    private static Sheet testSheet;
    private static Folder testFolder;
    private static Workspace testWorkSpace;
    private static List<Row> testRowList;

    private static Set<Integer> testSet;

    @BeforeAll
    public static void setup() {
        mockSheetResources = Mockito.mock(SheetResources.class);
        mockFolderResources = Mockito.mock(FolderResources.class);
        mockRowResources = Mockito.mock(SheetRowResources.class);

        Random random = new Random();
        testSheet = new Sheet(new Random().nextLong());
        testSheet.setName("TestSheet-00000_Projekte-00000_Projekt-XXXXX_Projekte-XXXXX_Projekt");
        testFolder = new Folder(random.nextLong());
        testFolder.setSheets(List.of(testSheet));
        testWorkSpace = new Workspace();
        testWorkSpace.setId(random.nextLong());
        testSet = Set.of(random.nextInt(), random.nextInt(), random.nextInt());
        testRowList = List.of(new Row(random.nextLong()), new Row(random.nextLong()), new Row(random.nextLong()));
    }

    @Test
    @DisplayName("getSheet method requests sheet with given id from smartsheet API and returns it")
    public void testGetSheet() throws SmartsheetException {
        Mockito.when(mockSmartsheet.sheetResources()).thenReturn(mockSheetResources);
        Mockito.when(mockSheetResources.getSheet(eq(testSheet.getId()), any(), any(), any(), any(), any(), any(), any())).thenReturn(testSheet);

        Sheet returnedSheet = repository.getSheet(testSheet.getId());

        Mockito.verify(mockSheetResources).getSheet(eq(testSheet.getId()), any(), any(), any(), any(), any(), any(), any());
        assertThat(returnedSheet).isEqualTo(testSheet);
    }

    @Test
    @DisplayName("copyFolderToWorkspace method passes supplied arguments to Smartsheet API and returns the folder received from there")
    public void testCopyFolderToWorkspace() throws SmartsheetException {
        Mockito.when(mockSmartsheet.folderResources()).thenReturn(mockFolderResources);
        Mockito.when(mockFolderResources.copyFolder(anyLong(), any(), any(), any())).thenReturn(testFolder);
        ArgumentCaptor<ContainerDestination> destinationCaptor = ArgumentCaptor.forClass(ContainerDestination.class);

        Folder returnedFolder = repository.copyFolderToWorkspace(testFolder.getId(), testWorkSpace.getId(), "testName");

        Mockito.verify(mockFolderResources).copyFolder(eq(testFolder.getId()), destinationCaptor.capture(), any(), any());
        assertThat(returnedFolder).isEqualTo(testFolder);

        assertThat(destinationCaptor.getValue().getDestinationType()).isEqualTo(DestinationType.WORKSPACE);
        assertThat(destinationCaptor.getValue().getDestinationId()).isEqualTo(testWorkSpace.getId());
        assertThat(destinationCaptor.getValue().getNewName()).isEqualTo("testName");
    }

    @Test
    @DisplayName("renameSheets method requests a folder by the id passed to it from Smartsheet API and returns the contained sheets")
    public void testReturnRenamedSheets() throws SmartsheetException {
        Mockito.when(mockSmartsheet.folderResources()).thenReturn(mockFolderResources);
        Mockito.when(mockSmartsheet.sheetResources()).thenReturn(mockSheetResources);
        Mockito.when(mockFolderResources.getFolder(eq(testFolder.getId()), any())).thenReturn(testFolder);

        List<Sheet> returnedSheets = repository.renameSheets(testFolder.getId(), "combinedName");

        assertThat(returnedSheets).isEqualTo(testFolder.getSheets());
    }

    @Test
    @DisplayName("renameSheets method passes the correctly renamed sheet to the Smartsheet API")
    public void testRenameSheetsCorrectly() throws SmartsheetException {
        Mockito.when(mockSmartsheet.folderResources()).thenReturn(mockFolderResources);
        Mockito.when(mockSmartsheet.sheetResources()).thenReturn(mockSheetResources);
        Mockito.when(mockFolderResources.getFolder(anyLong(), any())).thenReturn(testFolder);
        ArgumentCaptor<Sheet> sheetParametersCaptor = ArgumentCaptor.forClass(Sheet.class);

        repository.renameSheets(testFolder.getId(), "combinedName");

        Mockito.verify(mockSheetResources).updateSheet(sheetParametersCaptor.capture());
        assertThat(sheetParametersCaptor.getValue().getName()).isEqualTo("TestSheet-combinedName-combinedName-combinedName-combinedName");
        assertThat(sheetParametersCaptor.getValue().getId()).isEqualTo(testSheet.getId());
    }

    @Test
    @DisplayName("copySheetToWorkspace method correctly passes supplied arguments to Smartsheet API and returns the sheet received from there")
    public void testCopySheetToWorkspace() throws SmartsheetException {
        Mockito.when(mockSmartsheet.sheetResources()).thenReturn(mockSheetResources);
        Mockito.when(mockSheetResources.createSheetInWorkspaceFromTemplate(anyLong(), any(), any())).thenReturn(testSheet);
        ArgumentCaptor<Sheet> sheetCaptor = ArgumentCaptor.forClass(Sheet.class);

        Sheet returnedSheet = repository.copySheetToWorkspace(testSheet.getId(), testWorkSpace.getId(), "combinedName", "nameAppendix");

        assertThat(returnedSheet).isEqualTo(testSheet);
        Mockito.verify(mockSheetResources).createSheetInWorkspaceFromTemplate(eq(testWorkSpace.getId()), sheetCaptor.capture(), any());
        assertThat(sheetCaptor.getValue().getFromId()).isEqualTo(testSheet.getId());
        assertThat(sheetCaptor.getValue().getName()).isEqualTo("combinedName_nameAppendix");
    }

    @Test
    @DisplayName("loadSheetWithRelevantRows method requests a sheet with the supplied arguments from smartsheet API and returns it")
    public void testLoadSheetWithRelevantRows() throws SmartsheetException {
        Mockito.when(mockSmartsheet.sheetResources()).thenReturn(mockSheetResources);
        Mockito.when(mockSheetResources.getSheet(eq(testSheet.getId()), any(), any(), any(), eq(testSet), any(), isNull(), isNull())).thenReturn(testSheet);

        Sheet sheet = repository.loadSheetWithRelevantRows(testSheet, testSet);

        assertThat(sheet).isEqualTo(testSheet);
    }

    @Test
    @DisplayName("insertRowsIntoSheet method requests a row update with the supplied parameters from Smartsheet API and returns the result")
    public void testInsertRowsIntoSheetSuccess() throws SmartsheetException {
        Mockito.when(mockSmartsheet.sheetResources()).thenReturn(mockSheetResources);
        Mockito.when(mockSheetResources.rowResources()).thenReturn(mockRowResources);
        Mockito.when(mockRowResources.updateRows(testSheet.getId(), testRowList)).thenReturn(testRowList);

        List<Row> returnedRows = repository.insertRowsIntoSheet(testSheet, testRowList);

        assertThat(returnedRows).isEqualTo(testRowList);
    }

    @Test
    @DisplayName("on Exception insertRowsIntoSheet method returns null")
    public void testInsertRowsIntoSheetFailure() throws SmartsheetException {
        Mockito.when(mockSmartsheet.sheetResources()).thenReturn(mockSheetResources);
        Mockito.when(mockSheetResources.rowResources()).thenReturn(mockRowResources);
        Mockito.when(mockRowResources.updateRows(testSheet.getId(), testRowList)).thenThrow(SmartsheetException.class);

        List<Row> returnedRows = repository.insertRowsIntoSheet(testSheet, testRowList);

        assertThat(returnedRows).isNull();
    }

}
