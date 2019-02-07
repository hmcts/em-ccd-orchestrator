package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CcdValue<T> {
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public CcdValue(T t) {
        setValue(t);
    }

    public CcdValue(){}
}
