package uk.gov.hmcts.reform.em.orchestrator.stitching.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the DocumentTask entity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallbackDto implements Serializable {

    private Long id;

    private CallbackState callbackState = CallbackState.NEW;

    private String failureDescription;

    private String callbackUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFailureDescription() {
        return failureDescription;
    }

    public void setFailureDescription(String failureDescription) {
        this.failureDescription = failureDescription;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public CallbackState getCallbackState() {
        return callbackState;
    }

    public void setCallbackState(CallbackState callbackState) {
        this.callbackState = callbackState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CallbackDto callbackDto = (CallbackDto) o;
        if (callbackDto.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), callbackDto.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public String toString() {
        return "CallbackDto(id=" + this.getId() + ", callbackState=" + this.getCallbackState()
            + ", failureDescription=" + this.getFailureDescription() + ", callbackUrl=" + this.getCallbackUrl() + ")";
    }
}
