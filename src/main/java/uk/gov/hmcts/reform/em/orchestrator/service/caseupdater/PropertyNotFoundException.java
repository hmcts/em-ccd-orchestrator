package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

public class PropertyNotFoundException extends RuntimeException {

    public PropertyNotFoundException(String message) {
        super(String.format("%s property could not be found", message));
    }

}
