package uk.gov.hmcts.reform.em.orchestrator.stitching.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the DocumentTask entity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentTaskDTO implements Serializable {

    private Long id;

    @NotNull
    private StitchingBundleDTO  bundle;

    private TaskState taskState;

    private String failureDescription;

    private CallbackDto callback;

    @JsonIgnore
    private String jwt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StitchingBundleDTO getBundle() {
        return bundle;
    }

    public void setBundle(StitchingBundleDTO bundle) {
        this.bundle = bundle;
    }

    public TaskState getTaskState() {
        return taskState;
    }

    public void setTaskState(TaskState taskState) {
        this.taskState = taskState;
    }

    public String getFailureDescription() {
        return failureDescription;
    }

    public void setFailureDescription(String failureDescription) {
        this.failureDescription = failureDescription;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DocumentTaskDTO documentTaskDTO = (DocumentTaskDTO) o;
        if (documentTaskDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), documentTaskDTO.getId());
    }

    public CallbackDto getCallback() {
        return callback;
    }

    public void setCallback(CallbackDto callback) {
        this.callback = callback;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "DocumentTaskDTO{"
                + "id=" + getId()
                + ", bundle='" + getBundle() + "'"
                + ", taskState='" + getTaskState() + "'"
                + ", failureDescription='" + getFailureDescription() + "'"
                + "}";
    }
}
