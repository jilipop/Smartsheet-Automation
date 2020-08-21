package hubsoft.smartsheet.sf.automation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Constants {

    @Value("${constants.access-token}")
    private String accessToken;

    @Value("${constants.shared-secret}")
    private String sharedSecret;

    @Value("${constants.ids.input-sheet}")
    private long inputSheetId;

    @Value("${constants.ids.template-folder}")
    private long templateFolderId;

    @Value("${constants.ids.timing-workspace-maedchenfilm}")
    private long timingWorkspaceMaedchenFilmId;

    @Value("${constants.ids.timing-workspace-eleven}")
    private long timingWorkspaceElevenId;

    @Value("${constants.ids.shotlist-workspace-maedchenfilm}")
    private long shotlistWorkspaceMaedchenFilmId;

    @Value("${constants.ids.shotlist-workspace-eleven}")
    private long shotlistWorkspaceElevenId;

    @Value("${constants.ids.timing-sheet}")
    private long timingTemplateId;

    @Value("${constants.ids.shotlist-sheet}")
    private long shotlistTemplateId;

    @Value("${constants.ids.job-number-column}")
    private long jobNumberColumnId;

    @Value("${constants.ids.label-column}")
    private long labelColumnId;

    @Value("${constants.ids.client-name-column}")
    private long clientNameColumnId;

    @Value("${constants.ids.project-name-column}")
    private long projectNameColumnId;

    @Value("${constants.ids.asp-column}")
    private long aspColumnId;

    @Value("${constants.ids.kv-column}")
    private long kvColumnId;

    @Value("${constants.ids.t-column}")
    private long tColumnId;

    @Value("${constants.ids.sl-column}")
    private long slColumnId;

    @Value("${constants.ids.maedchenfilm-workspace}")
    private long maedchenFilmWorkSpaceId;

    @Value("${constants.ids.eleven-workspace}")
    private long elevenWorkSpaceId;

    public enum id{
        INPUT_SHEET,
        TEMPLATE_FOLDER,
        TIMING_WORKSPACE_MF,
        TIMING_WORKSPACE_ELEVEN,
        SHOTLIST_WORKSPACE_MF,
        SHOTLIST_WORKSPACE_ELEVEN,
        TIMING_TEMPLATE,
        SHOTLIST_TEMPLATE,
        JOB_NR_COLUMN,
        LABEL_COLUMN,
        CLIENT_COLUMN,
        PROJECT_COLUMN,
        ASP_COLUMN,
        KV_COLUMN,
        T_COLUMN,
        SL_COLUMN,
        MF_WORKSPACE,
        ELEVEN_WORKSPACE
    }

    private final Map<id, Long> ids = new HashMap<>();

    public String getAccessToken() {
        return accessToken;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public long getInputSheetId() {

        return inputSheetId;
    }

    public Map<id, Long> getIds() {
       if (ids.size() == 0) {
           ids.put(id.INPUT_SHEET, inputSheetId);
           ids.put(id.TEMPLATE_FOLDER, templateFolderId);
           ids.put(id.TIMING_WORKSPACE_MF, timingWorkspaceMaedchenFilmId);
           ids.put(id.TIMING_WORKSPACE_ELEVEN, timingWorkspaceElevenId);
           ids.put(id.SHOTLIST_WORKSPACE_MF, shotlistWorkspaceMaedchenFilmId);
           ids.put(id.SHOTLIST_WORKSPACE_ELEVEN, shotlistWorkspaceElevenId);
           ids.put(id.TIMING_TEMPLATE, timingTemplateId);
           ids.put(id.SHOTLIST_TEMPLATE, shotlistTemplateId);
           ids.put(id.JOB_NR_COLUMN, jobNumberColumnId);
           ids.put(id.LABEL_COLUMN, labelColumnId);
           ids.put(id.CLIENT_COLUMN, clientNameColumnId);
           ids.put(id.PROJECT_COLUMN, projectNameColumnId);
           ids.put(id.ASP_COLUMN, aspColumnId);
           ids.put(id.KV_COLUMN, kvColumnId);
           ids.put(id.T_COLUMN, tColumnId);
           ids.put(id.SL_COLUMN, slColumnId);
           ids.put(id.MF_WORKSPACE, maedchenFilmWorkSpaceId);
           ids.put(id.ELEVEN_WORKSPACE, elevenWorkSpaceId);
       }
        return ids;
    }
}
