package hubsoft.smartsheet.sf.automation;

import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.*;
import hubsoft.smartsheet.sf.automation.enums.Id;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

import hubsoft.smartsheet.sf.automation.enums.Col;

@Service
public class WebHookService {

    private final EnumMap<Id, Long> ids;
    private final Map<String, Long> columnMap = new HashMap<>();
    private final SmartsheetRepository repository;

    private Sheet referenceSheet;

    public WebHookService(Constants constants, SmartsheetRepository repository) {
        ids = constants.getIds();
        this.repository = repository;
    }

    public void processTemplates(long inputSheetId) {
        try {
            Sheet inputSheet = repository.getSheet(inputSheetId);
            referenceSheet = ReferenceSheets.getSheet(inputSheetId);

            for (Column column: inputSheet.getColumns())
                columnMap.put(column.getTitle(), column.getId());

            Set<Row> rowsToProcess = getRowsToProcess(inputSheet.getRows());
            for (Row row: rowsToProcess)
                processRow(row);

            ReferenceSheets.setSheet(inputSheetId, inputSheet);
            System.out.println("Aktualisierung abgeschlossen.");
        } catch (Exception ex) {
            System.out.println("Fehler : " + ex.getMessage());
            System.out.println("Die Verarbeitung der neuen Projekteinträge ist gescheitert.");
        }
    }

    private Set<Row> getRowsToProcess(List<Row> rows){
        Set<Row> rowsToProcess = new HashSet<>();
        try {
            for (Row row : rows) {
                Cell kvCell = getCellByColumnTitle(row, "KV");
                Cell tCell = getCellByColumnTitle(row, "T");
                Cell slCell = getCellByColumnTitle(row, "SL");
                if (newCheckmark(row, kvCell) || newCheckmark(row, tCell) || newCheckmark(row, slCell)){
                    rowsToProcess.add(row);
                }
            }
        } catch (Exception ex) {
            System.out.println("Fehler : " + ex.getMessage());
            System.out.println("Die Prüfung auf neue Projekteinträge ist gescheitert.");
        }
        return rowsToProcess;
    }

    private void processRow(Row row) throws Exception {
        Map<Col, Cell> cells = buildCellMap(row);
        Map<Col, String> cellEntries = getCellEntries(cells);

        if (!(StringUtils.hasText(cellEntries.get(Col.JOB_NR))
                && StringUtils.hasText(cellEntries.get(Col.KUNDE))
                && StringUtils.hasText(cellEntries.get(Col.PROJEKT))))
            throw new Exception("Breche ab, weil nicht alle nötigen Zellen ausgefüllt sind.");

        String combinedName = combineName(cellEntries.get(Col.JOB_NR), cellEntries.get(Col.KUNDE), cellEntries.get(Col.PROJEKT));

        Long targetWorkspaceId = getTargetWorkSpaceId(cells.get(Col.LABEL));
        if (targetWorkspaceId == null) {
            System.out.println("Überspringe Tabellenzeile, weil das Label weder \"Mädchenfilm\" noch \"Eleven\" ist.");
            return;
        }

        boolean isMfWorkspace = targetWorkspaceId.equals(ids.get(Id.MF_WORKSPACE));

        if (newCheckmark(row, cells.get(Col.TIMING)))
            handleTimingCheckMark(isMfWorkspace, combinedName, cellEntries);

        if (newCheckmark(row, cells.get(Col.SHOTLIST))) {
            long targetId = isMfWorkspace ? ids.get(Id.SHOTLIST_WORKSPACE_MF) : ids.get(Id.SHOTLIST_WORKSPACE_ELEVEN);
            repository.copySheetToWorkspace(ids.get(Id.SHOTLIST_TEMPLATE), targetId, combinedName, "Shotlist");
        }

        if (newCheckmark(row, cells.get(Col.KV)))
            handleKvCheckMark(targetWorkspaceId, combinedName, cellEntries.get(Col.PROJEKT), cellEntries.get(Col.ASP));
    }

    private Map<Col, String> getCellEntries(Map<Col, Cell> cells){
        Map<Col, String> cellEntries = new HashMap<>();

        cellEntries.put(Col.JOB_NR, getStringFromCell(cells.get(Col.JOB_NR)));
        cellEntries.put(Col.KUNDE, getStringFromCell(cells.get(Col.KUNDE)));
        cellEntries.put(Col.PROJEKT, getStringFromCell(cells.get(Col.PROJEKT)));
        cellEntries.put(Col.ASP, getStringFromCell(cells.get(Col.ASP)));
        cellEntries.put(Col.AGENTUR, getStringFromCell(cells.get(Col.AGENTUR)));

        return cellEntries;
    }

    private String getStringFromCell(Cell cell){
        if (cell != null && cell.getDisplayValue() != null)
            return cell.getDisplayValue();
        else return "";
    }

    private void handleTimingCheckMark(boolean isMfWorkspace, String combinedName, Map<Col, String> cellEntries) throws SmartsheetException {
        long targetId = isMfWorkspace ? ids.get(Id.TIMING_WORKSPACE_MF) : ids.get(Id.TIMING_WORKSPACE_ELEVEN);
        Sheet timingSheet = repository.copySheetToWorkspace(ids.get(Id.TIMING_TEMPLATE), targetId, combinedName, "Timing");

        if (timingSheet != null) {
            timingSheet = repository.loadSheetWithRelevantRows(timingSheet, Set.of(1, 2, 3, 4, 5));
            Row row1 = updateRow(timingSheet, 0, Map.of("Phase", cellEntries.get(Col.PROJEKT)));
            Row row2 = updateRow(timingSheet, 1, Map.of("Phase", cellEntries.get(Col.JOB_NR)));
            Row row3 = updateRow(timingSheet, 2, Map.of("Phase", cellEntries.get(Col.KUNDE)));
            Row row4 = updateRow(timingSheet, 3, Map.of("Phase", "Agentur: " + cellEntries.get(Col.AGENTUR)));
            Row row5 = updateRow(timingSheet, 4, Map.of("Phase", "Producer: " + cellEntries.get(Col.ASP)));
            repository.insertRowsIntoSheet(timingSheet, List.of(row1, row2, row3, row4, row5));
        }
    }

    private void handleKvCheckMark(Long targetWorkspaceId, String combinedName, String projectName, String asp) throws SmartsheetException {
        long targetId = repository.copyFolderToWorkspace(
                ids.get(Id.TEMPLATE_FOLDER),
                targetWorkspaceId,
                combinedName)
                .getId();

        List<Sheet> targetSheets = repository.renameSheets(targetId, combinedName);

        Sheet finanzSheet = targetSheets.stream()
                .filter(sheet -> sheet.getName().contains("Finanzen"))
                .findFirst()
                .orElse(null);

        if (finanzSheet != null){
            finanzSheet = repository.loadSheetWithRelevantRows(finanzSheet, Set.of(1));
            Row firstRow = updateRow(finanzSheet, 0, Map.of("Position", projectName, "Empfänger", asp));
            repository.insertRowsIntoSheet(finanzSheet, List.of(firstRow));
        }
    }

    private Cell getCellByColumnTitle(Row row, String columnTitle) {
        Long colId = columnMap.get(columnTitle);

        return row.getCells().stream()
                .filter(cell -> colId.equals(cell.getColumnId()))
                .findFirst()
                .orElse(null);
    }

    private boolean newCheckmark(Row row, Cell cell) {
        if (Objects.nonNull(cell) && cell.getValue().equals(true)) {
            cell = sameRowInReferenceSheet(cell, row);
            return !(Objects.nonNull(cell) && cell.getValue().equals(true));
        }
        return false;
    }

    private Cell sameRowInReferenceSheet(Cell cell, Row row){
        Row referenceRow = referenceSheet.getRows().stream()
                .filter(oldRow -> oldRow.getId().equals(row.getId()))
                .findFirst()
                .orElse(null);

        if (referenceRow == null)
            return null;
        else
            return referenceRow.getCells().stream()
                    .filter(refCell -> refCell.getColumnId().equals(cell.getColumnId()))
                    .findFirst()
                    .orElse(null);
    }

    private Map<Col, Cell> buildCellMap(Row row) {
        final Map<String, Col> colNames = Map.of(
                "Job_Nr.", Col.JOB_NR,
                "Kunde", Col.KUNDE,
                "Label", Col.LABEL,
                "Projektname", Col.PROJEKT,
                "ASP", Col.ASP,
                "Agentur", Col.AGENTUR,
                "KV", Col.KV,
                "T", Col.TIMING,
                "SL", Col.SHOTLIST);
        EnumMap <Col, Cell> cellMap = new EnumMap<>(Col.class);
        columnMap.forEach((name, id) -> {
            Cell targetCell = row.getCells().stream()
                    .filter(cell -> cell.getColumnId().equals(id))
                    .findFirst()
                    .orElse(null);
            Col col = colNames.get(name);
            cellMap.put(col, targetCell);
        });
        return cellMap;
    }

    private String combineName(String jobNumber, String clientName, String projectName){
        final int maxFileNameLengthImposedBySmartsheet = 50;
        final int longestTemplateNameFixedChars = 23; //inklusive "_" zwischen Kundenname und Projektname
        final int remainingLength = maxFileNameLengthImposedBySmartsheet - longestTemplateNameFixedChars;
        final int minimumProjectNameLength = 4;

        if (clientName.length() > remainingLength - minimumProjectNameLength)
            clientName = clientName.substring(0, remainingLength - minimumProjectNameLength -1);

        if (clientName.length() + projectName.length() > remainingLength)
            projectName = projectName.substring(0, remainingLength - clientName.length() -1);

        return jobNumber + "_" + clientName + "_" + projectName;
    }

    private Long getTargetWorkSpaceId(Cell labelCell) throws NullPointerException {
        if (labelCell == null || labelCell.getValue() == null)
            return null;
        else if (labelCell.getValue().equals("Mädchenfilm"))
            return ids.get(Id.MF_WORKSPACE);
        else if (labelCell.getValue().equals("Eleven"))
            return ids.get(Id.ELEVEN_WORKSPACE);
        else return null;
    }

    public Row updateRow(Sheet sheetToUpdate, int rowIndex, Map<String, String> cellData) {
        Row rowToUpdate = sheetToUpdate.getRows().get(rowIndex);
        List<Cell> cellsToUpdate = new ArrayList<>();

        Map<String, Long> newColumnMap = new HashMap<>();
        for (Column column : sheetToUpdate.getColumns())
            newColumnMap.put(column.getTitle(), column.getId());

        cellData.forEach((columnTitle, value) -> {
            Cell targetCell = rowToUpdate.getCells().stream()
                    .filter(cell -> cell.getColumnId().equals(newColumnMap.get(columnTitle)))
                    .findFirst()
                    .orElse(null);
            if (targetCell != null) {
                targetCell.setStrict(false);
                targetCell.setValue(value);
                cellsToUpdate.add(targetCell);
            }
        });
        Row newRow = new Row();
        newRow.setId(rowToUpdate.getId());
        newRow.setCells(cellsToUpdate);

        return newRow;
    }
}