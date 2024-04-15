package evprimeTests;

import client.EVPrimeClient;
import data.PostEventDataFactory;
import data.SignUpLoginDataFactory;
import database.DbClient;
import io.restassured.response.Response;
import models.request.PostUpdateEventRequest;
import models.request.SignUpLoginRequest;
import models.response.GetAllEventsResponse;
import models.response.LoginResponse;
import models.response.PostUpdateDeleteEventResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import java.sql.SQLException;

import static objectBuilder.PostEventObjectBuilder.createBodyForPostEvent;
import static objectBuilder.SignUpBuilder.createBodyForSIgnUp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetSingleEventTests {

    private static String id;
    private SignUpLoginRequest signUpRequest;
    private LoginResponse loginResponseBody;
    private PostUpdateEventRequest requestBody;
    private PostUpdateDeleteEventResponse postResponse;

    @Before
    public void setUp() {
        signUpRequest = new SignUpLoginDataFactory(createBodyForSIgnUp())
                .setEmail(RandomStringUtils.randomAlphanumeric(10) + "@mail.com")
                .setPassword(RandomStringUtils.randomAlphanumeric(10))
                .createRequest();

        new EVPrimeClient()
                .signUp(signUpRequest);

        Response loginResponse = new EVPrimeClient()
                .login(signUpRequest);

        loginResponseBody = loginResponse.body().as(LoginResponse.class);

        requestBody = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle("Liverpool - Manchester United football match 1")
                .setImage("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.goal.com%2Fen-sg%2Fnews%2Fliverpool-vs-manchester-united-lineups-live-updates%2Fbltf4a9e3c54804c6b8&psig=AOvVaw11pYwQiECKpPWu17jL6s6X&ust=1712771074871000&source=images&cd=vfe&opi=89978449&ved=0CBIQjRxqFwoTCOiy883XtYUDFQAAAAAdAAAAABAE")
                .setDate("2024-04-07")
                .setLocation("Anfield")
                .setDescription("The match between the biggest rivals 1.")
                .createRequest();

        Response response = new EVPrimeClient()
                .postEvent(requestBody, loginResponseBody.getToken());

        postResponse = response.body().as(PostUpdateDeleteEventResponse.class);
        id = postResponse.getMessage().substring(39);
    }

    @Test
    public void getEventByIdTest() {
        Response getEventResponse = new EVPrimeClient()
                .getEventById(postResponse.getMessage().substring(39));

        GetAllEventsResponse responseBody = getEventResponse.body().as(GetAllEventsResponse.class);

        assertEquals(200, getEventResponse.statusCode());
        assertEquals(requestBody.getTitle(), responseBody.getEvents().get(0).getTitle());
        assertEquals(requestBody.getDate(), responseBody.getEvents().get(0).getDate());
        assertEquals(requestBody.getImage(), responseBody.getEvents().get(0).getImage());
        assertEquals(requestBody.getDescription(), responseBody.getEvents().get(0).getDescription());
        assertEquals(requestBody.getLocation(), responseBody.getEvents().get(0).getLocation());
    }
    @Test
    public void wrongIdTest() {
        Response getEventResponse = new EVPrimeClient()
                .getEventById(postResponse.getMessage().substring(38));

        GetAllEventsResponse responseBody = getEventResponse.body().as(GetAllEventsResponse.class);

        assertEquals(200, getEventResponse.statusCode());
        assertTrue(responseBody.getEvents().isEmpty());
    }

    @AfterClass
    public static void deleteEvent() throws SQLException {
        assertTrue(new DbClient()
                .isEventDeletedFromDb(id));
    }
}
