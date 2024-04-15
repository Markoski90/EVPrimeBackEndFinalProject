package objectBuilder;

import models.request.SignUpLoginRequest;

public class SignUpBuilder {
    public static SignUpLoginRequest createBodyForSIgnUp(){
        return SignUpLoginRequest.builder()
                .email("default@mail.com")
                .password("defaultpassword")
                .build();
    }
}
