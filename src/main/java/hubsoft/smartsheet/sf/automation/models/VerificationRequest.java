package hubsoft.smartsheet.sf.automation.models;

public class VerificationRequest {
    public String challenge;
    public Long webhookId;

    public VerificationRequest() {
    }

    public VerificationRequest(String challenge, Long webhookId) {
        this.challenge = challenge;
        this.webhookId = webhookId;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public Long getWebHookId() {
        return webhookId;
    }

    public void setWebHookId(Long webHookId) {
        this.webhookId = webHookId;
    }
}
