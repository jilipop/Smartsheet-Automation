package hubsoft.smartsheet.sf.automation;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.models.*;
import com.smartsheet.api.models.enums.ObjectExclusion;
import hubsoft.smartsheet.sf.automation.enums.Id;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.naming.InvalidNameException;
import java.util.*;

import hubsoft.smartsheet.sf.automation.enums.ColName;

@Service
public class WebHookService {

    private final EnumMap<Id, Long> ids;
    private final Map<String, Long> columnMap = new HashMap<>();
    private final Smartsheet smartsheet;
    private final SmartsheetApiOperations apiDo;

    private Sheet referenceSheet;

    @Autowired
    public WebHookService(Constants constants, Smartsheet smartsheet, SmartsheetApiOperations apiDo) {
        ids = constants.getIds();
        this.smartsheet = smartsheet;
        this.apiDo = apiDo;
    }

    public void processTemplates(long inputSheetId) {
        try {
            Sheet inputSheet = smartsheet.sheetResources().getSheet(inputSheetId, null, EnumSet.of(ObjectExclusion.NONEXISTENT_CELLS), null, null, null, null, null);
            System.out.println(inputSheet.getRows().size() + " Zeilen aus der Datei " + inputSheet.getName() + " geladen.");

            referenceSheet = ReferenceSheets.getSheet(inputSheetId);

            for (Column column: inputSheet.getColumns())
                columnMap.put(column.getTitle(), column.getId());

            Set<Row> rowsToProcess = getRowsToProcess(inputSheet.getRows());
            for (Row row: rowsToProcess) {
                Map<ColName, Cell> cells = buildCellMap(row);

                String jobNumber, clientName, projectName;
                try {
                    jobNumber = cells.get(ColName.JOB_NR).getDisplayValue();
                    clientName = cells.get(ColName.KUNDE).getDisplayValue();
                    projectName = cells.get(ColName.PROJEKT).getDisplayValue();
                    if (!(StringUtils.hasText(jobNumber) && StringUtils.hasText(clientName) && StringUtils.hasText(projectName)))
                        throw new Exception();
                } catch (Exception ex) {
                    throw new Exception("Breche ab, weil nicht alle nötigen Zellen ausgefüllt sind.");
                }

                String asp = "";
                Cell aspCell = cells.get(ColName.ASP);
                if (aspCell != null && aspCell.getDisplayValue() != null)
                    asp = aspCell.getDisplayValue();

                String agency = "";
                Cell agencyCell = cells.get(ColName.AGENTUR);
                if (agencyCell != null && agencyCell.getDisplayValue() != null)
                    agency = agencyCell.getDisplayValue();

                String combinedName = combineName(jobNumber, clientName, projectName);

                try {
                    getTargetWorkSpaceId(cells.get(ColName.LABEL));
                } catch (Exception ex) {
                    System.out.println("Überspringe Tabellenzeile, weil das Label weder \"Mädchenfilm\" noch \"Eleven\" ist.");
                    continue;
                }
                boolean isMaedchenFilmWorkSpace = getTargetWorkSpaceId(cells.get(ColName.LABEL)) == ids.get(Id.MF_WORKSPACE);

                long targetId;
                if (newCheckmark(row, cells.get(ColName.TIMING))) {
                    targetId = isMaedchenFilmWorkSpace ? ids.get(Id.TIMING_WORKSPACE_MF) : ids.get(Id.TIMING_WORKSPACE_ELEVEN);
                    Sheet timingSheet = apiDo.copySheetToWorkspace(ids.get(Id.TIMING_TEMPLATE), targetId, combinedName, "Timing");

                    if (timingSheet != null) {
                        timingSheet = apiDo.loadSheetWithRelevantRows(timingSheet, Set.of(1, 2, 3, 4, 5));
                        Row row1 = apiDo.updateRow(timingSheet, 0, Map.of("Phase", projectName));
                        Row row2 = apiDo.updateRow(timingSheet, 1, Map.of("Phase", jobNumber));
                        Row row3 = apiDo.updateRow(timingSheet, 2, Map.of("Phase", clientName));
                        Row row4 = apiDo.updateRow(timingSheet, 3, Map.of("Phase", "Agentur: " + agency));
                        Row row5 = apiDo.updateRow(timingSheet, 4, Map.of("Phase", "Producer: " + asp));
                        apiDo.insertRowsIntoSheet(timingSheet, List.of(row1, row2, row3, row4, row5));
                    }

                }
                if (newCheckmark(row, cells.get(ColName.SHOTLIST))) {
                    targetId = isMaedchenFilmWorkSpace ? ids.get(Id.SHOTLIST_WORKSPACE_MF) : ids.get(Id.SHOTLIST_WORKSPACE_ELEVEN);
                    apiDo.copySheetToWorkspace(ids.get(Id.SHOTLIST_TEMPLATE), targetId, combinedName, "Shotlist");
                }
                if (newCheckmark(row, cells.get(ColName.KV))) {
                    targetId = apiDo.copyFolder(getTargetWorkSpaceId(cells.get(ColName.LABEL)), combinedName).getId();

                    List<Sheet> targetSheets = apiDo.renameSheets(targetId, combinedName);

                    Sheet finanzSheet = targetSheets.stream()
                            .filter(sheet -> sheet.getName().contains("Finanzen"))
                            .findFirst()
                            .orElse(null);

                    if (finanzSheet != null){
                        finanzSheet = apiDo.loadSheetWithRelevantRows(finanzSheet, Set.of(1));
                        Row firstRow = apiDo.updateRow(finanzSheet, 0, Map.of("Position", projectName, "Empfänger", asp));
                        apiDo.insertRowsIntoSheet(finanzSheet, List.of(firstRow));
                    }
                }
            }
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

    private Cell getCellByColumnTitle(Row row, String columnTitle) {
        Long colId = columnMap.get(columnTitle);

        return row.getCells().stream()
                .filter(cell -> colId.equals(cell.getColumnId()))
                .findFirst()
                .orElse(null);
    }

    private Map<ColName, Cell> buildCellMap(Row row) {
        final Map<String, ColName> colNames = Map.of(
                "Job_Nr.", ColName.JOB_NR,
                "Kunde", ColName.KUNDE,
                "Label", ColName.LABEL,
                "Projektname", ColName.PROJEKT,
                "ASP", ColName.ASP,
                "Agentur", ColName.AGENTUR,
                "KV", ColName.KV,
                "T", ColName.TIMING,
                "SL", ColName.SHOTLIST);
        EnumMap <ColName, Cell> cellMap = new EnumMap<>(ColName.class);
        columnMap.forEach((name, id) -> {
            Cell targetCell = row.getCells().stream()
                    .filter(cell -> cell.getColumnId().equals(id))
                    .findFirst()
                    .orElse(null);
            ColName colName = colNames.get(name);
            cellMap.put(colName, targetCell);
        });
        return cellMap;
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

    private long getTargetWorkSpaceId(Cell labelCell) throws InvalidNameException {
        if (labelCell != null && labelCell.getValue().equals("Mädchenfilm")) {
            return ids.get(Id.MF_WORKSPACE);
        } else if (labelCell != null && labelCell.getValue().equals("Eleven")){
            return ids.get(Id.ELEVEN_WORKSPACE);
        } else throw new InvalidNameException("Da das Label weder \"Mädchenfilm\" noch \"Eleven\" ist, hätte diese Zeile eigentlich übersprungen werden sollen. Das hat aber offenbar nicht geklappt.");
    }


}