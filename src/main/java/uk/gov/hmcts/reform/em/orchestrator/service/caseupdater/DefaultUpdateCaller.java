package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.config.Constants;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CdamDetailsDto;
import uk.gov.hmcts.reform.em.orchestrator.service.notification.NotificationService;
import uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("squid:S2629")
@Service
public class DefaultUpdateCaller {

    private final Logger logger = LoggerFactory.getLogger(DefaultUpdateCaller.class);

    private final CcdCallbackDtoCreator ccdCallbackDtoCreator;
    private final NotificationService notificationService;

    @Value("${notify.failureTemplateId}")
    private String failureTemplateId;

    private static final String CLONE_BUNDLE_EVENT = "cloneBundle";
    private static final String ASYNC_STITCHING_COMPLETE_EVENT = "asyncStitchingComplete";

    public DefaultUpdateCaller(CcdCallbackDtoCreator ccdCallbackDtoCreator,
                               NotificationService notificationService) {
        this.ccdCallbackDtoCreator = ccdCallbackDtoCreator;
        this.notificationService = notificationService;
    }

    public CcdCallbackResponseDto executeUpdate(CcdCaseUpdater ccdCaseUpdater, HttpServletRequest request) {
        CcdCallbackDto dto = ccdCallbackDtoCreator.createDto(request, "caseBundles");
        createCdamDetails(dto, request);
        CcdCallbackResponseDto ccdCallbackResponseDto = new CcdCallbackResponseDto(dto.getCaseData());
        try {
            ccdCallbackResponseDto.setData(ccdCaseUpdater.updateCase(dto));
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
                && !StringUtils.equals(ccdEvent, CLONE_BUNDLE_EVENT) && !StringUtils.equals(ccdEvent, ASYNC_STITCHING_COMPLETE_EVENT)) {
            notificationService.sendEmailNotification(
                    failureTemplateId,
                    dto.getJwt(),
                    dto.getCaseId(),
                    dto.getCaseData().has("caseTitle") ? dto.getCaseData().get("caseTitle").asText() : "Bundle",
                    ccdCallbackResponseDto.getErrors().toString()
            );
        }
        return ccdCallbackResponseDto;
    }

    private void createCdamDetails(CcdCallbackDto dto, HttpServletRequest request) {
        if (StringUtils.isNotBlank(dto.getCaseTypeId()) && StringUtils.isNotBlank(dto.getJurisdictionId())) {
            CdamDetailsDto cdamDetailsDto = CdamDetailsDto.builder()
                .caseTypeId(dto.getCaseTypeId())
                .jurisdictionId(dto.getJurisdictionId())
                .serviceAuth(request.getHeader("ServiceAuthorization"))
                .build();
            request.getSession().setAttribute(Constants.CDAM_DEATILS, cdamDetailsDto);
            logger.info(String.format("CaseTypeId : %s and JurisdictionId : %s for caseId : %s ",
                    StringUtilities.convertValidLog(cdamDetailsDto.getCaseTypeId()),
                    StringUtilities.convertValidLog(cdamDetailsDto.getJurisdictionId()),
                    StringUtilities.convertValidLog(dto.getCaseId())));
        }
    }

}
