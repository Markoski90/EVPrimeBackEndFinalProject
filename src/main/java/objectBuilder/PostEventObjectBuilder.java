package objectBuilder;

import models.request.PostUpdateEventRequest;

public class PostEventObjectBuilder {

    public static PostUpdateEventRequest createBodyForPostEvent() {
        return PostUpdateEventRequest.builder()
                .title("default@mail.com")
                .image("default password")
                .date("03.04.1990")
                .location("default location")
                .description("default description")
                .build();
    }

}