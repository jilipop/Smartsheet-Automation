package hubsoft.smartsheet.test.models;

public class VerificationRequest {
    public String challenge;
    public Long webhookId;

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
