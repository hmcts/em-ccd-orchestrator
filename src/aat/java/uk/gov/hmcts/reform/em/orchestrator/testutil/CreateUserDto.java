package uk.gov.hmcts.reform.em.orchestrator.testutil;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
class CreateUserDto {

    private String email;
    private String password;
    @Builder.Default
    private String forename = "test";
    @Builder.Default
    private String surname = "test";
    @Builder.Default
    private int levelOfAccess = 1;
    private List<CreateUserRolesDto> roles;
    @Builder.Default
    private CreateUserRolesDto userGroup = new CreateUserRolesDto("caseworker");

}
