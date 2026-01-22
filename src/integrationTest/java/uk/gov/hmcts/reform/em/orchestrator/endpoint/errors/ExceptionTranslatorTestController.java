package uk.gov.hmcts.reform.em.orchestrator.endpoint.errors;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@Profile("exception-test-controller-enabled")
public class ExceptionTranslatorTestController {

    @GetMapping("/test/feign-conflict")
    public void feignGatewayTimeout() {
        throw new FeignException.Conflict("feign conflict", createFeignRequest(), null, new HashMap<>());
    }


    @GetMapping("/test/feign-bad-gateway")
    public void feignBadGateway() {
        throw new FeignException.BadGateway("feign bad gateway", createFeignRequest(), null, new HashMap<>());
    }

    private static Request createFeignRequest() {
        return Request.create(Request.HttpMethod.GET, "url",
                new HashMap<>(), null, new RequestTemplate());
    }

    @GetMapping("/test/missing-servlet-request-part")
    public void missingServletRequestPartException(@RequestPart String part) {
        // for testing
    }

    @GetMapping("/test/missing-servlet-request-parameter")
    public void missingServletRequestParameterException(@RequestParam String param) {
        // for testing
    }

}
