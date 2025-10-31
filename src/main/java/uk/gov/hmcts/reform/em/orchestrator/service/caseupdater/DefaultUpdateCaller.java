package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;
import uk.gov.hmcts.reform.em.orchestrator.service.notification.NotificationService;

import java.util.List;
import java.util.Set;

@Service
public class DefaultUpdateCaller {

    private final Logger logger = LoggerFactory.getLogger(DefaultUpdateCaller.class);

    private final CcdCallbackDtoCreator ccdCallbackDtoCreator;
    private final NotificationService notificationService;

    private final Validator validator;

    @Value("${notify.failureTemplateId}")
    private String failureTemplateId;

    @Value("${cdam.validation.enabled}")
    private boolean enableCdamValidation;

    private static final String CLONE_BUNDLE_EVENT = "cloneBundle";
    private static final String ASYNC_STITCHING_COMPLETE_EVENT = "asyncStitchingComplete";

    public DefaultUpdateCaller(CcdCallbackDtoCreator ccdCallbackDtoCreator,
                               NotificationService notificationService,
                               Validator validator) {
        this.ccdCallbackDtoCreator = ccdCallbackDtoCreator;
        this.notificationService = notificationService;
        this.validator = validator;
    }

    public ResponseEntity<CcdCallbackResponseDto> executeUpdate(CcdCaseUpdater ccdCaseUpdater,
                                                                HttpServletRequest request) {
        CcdCallbackDto dto = ccdCallbackDtoCreator.createDto(request, "caseBundles");
        dto.setServiceAuth(request.getHeader("ServiceAuthorization"));

        CcdCallbackResponseDto ccdCallbackResponseDto = new CcdCallbackResponseDto(dto.getCaseData());
        if (enableCdamValidation) {
            Set<ConstraintViolation<CcdCallbackDto>> violations = validator.validate(dto);

            if (!violations.isEmpty()) {
                List<String> messages = violations.stream().map(ConstraintViolation::getMessage).toList();
                ccdCallbackResponseDto.getErrors().addAll(messages);
                return ResponseEntity
                        .badRequest()
                        .body(ccdCallbackResponseDto);
            }
        }

        try {
            ccdCallbackResponseDto.setData(ccdCaseUpdater.updateCase(dto));
            ccdCallbackResponseDto.setDocumentTaskId(dto.getDocumentTaskId());
        } catch (InputValidationException e) {
            logger.error(e.getMessage(), e);
            ccdCallbackResponseDto.getErrors().addAll(e.getViolations());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            ccdCallbackResponseDto.getErrors().add(e.getMessage());
        }

        String ccdEvent = dto.getEventId();
        if (CollectionUtils.isNotEmpty(ccdCallbackResponseDto.getErrors())
                && BooleanUtils.isTrue(dto.getEnableEmailNotification())
                && !Strings.CS.equals(ccdEvent, CLONE_BUNDLE_EVENT)
                && !Strings.CS.equals(ccdEvent, ASYNC_STITCHING_COMPLETE_EVENT)) {
            notificationService.sendEmailNotification(
                    failureTemplateId,
                    dto.getJwt(),
                    dto.getCaseId(),
                    dto.getCaseData().has("caseTitle") ? dto.getCaseData().get("caseTitle").asText() : "Bundle",
                    ccdCallbackResponseDto.getErrors().toString()
            );
        }

        if (ccdCallbackResponseDto.getErrors().isEmpty()) {
            return ResponseEntity.ok(ccdCallbackResponseDto);
        }
        return ResponseEntity
                .badRequest()
                .body(ccdCallbackResponseDto);
    }

}
