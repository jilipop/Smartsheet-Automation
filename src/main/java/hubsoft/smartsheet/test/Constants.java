package hubsoft.smartsheet.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    @Value("${constants.ids.job-number-column}")
    private long jobNumberColumnId;

    @Value("${constants.ids.label-column}")
    private long labelColumnId;

    @Value("${constants.ids.project-name-column}")
    private long projectNameColumnId;

    @Value("${constants.ids.maedchenfilm-workspace}")
    private long maedchenFilmWorkSpaceId;

    @Value("${constants.ids.eleven-workspace}")
    private long elevenWorkSpaceId;

    public String getAccessToken() {
        return accessToken;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public long getInputSheetId() {
        return inputSheetId;
    }

    public long getTemplateFolderId() {
        return templateFolderId;
    }

    public long getJobNumberColumnId() {
        return jobNumberColumnId;
    }

    public long getLabelColumnId() {
        return labelColumnId;
    }

    public long getProjectNameColumnId() {
        return projectNameColumnId;
    }

    public long getMaedchenFilmWorkSpaceId() {
        return maedchenFilmWorkSpaceId;
    }

    public long getElevenWorkSpaceId() {
        return elevenWorkSpaceId;
    }
}
