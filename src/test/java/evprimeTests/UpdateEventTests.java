package evprimeTests;

import client.EVPrimeClient;
import data.PostEventDataFactory;
import data.SignUpLoginDataFactory;
import database.DbClient;
import io.restassured.response.Response;
import jdk.jfr.Description;
import models.request.PostUpdateEventRequest;
import models.request.SignUpLoginRequest;
import models.response.LoginResponse;
import models.response.PostUpdateErrorsResponse;
import models.response.PostUpdateDeleteEventResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.*;
import util.DateBuilder;

import java.sql.SQLException;

import static objectBuilder.PostEventObjectBuilder.createBodyForPostEvent;
import static objectBuilder.SignUpBuilder.createBodyForSIgnUp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UpdateEventTests {

    DbClient dbClient = new DbClient();
    private static String id;
    private static SignUpLoginRequest signUpRequest;
    private static LoginResponse loginResponseBody;
    private static PostUpdateEventRequest requestBody;
    private static PostUpdateDeleteEventResponse postResponse;

    static DateBuilder dateBuilder = new DateBuilder();

    @Before
    public void setUp() {
        signUpRequest = new SignUpLoginDataFactory(createBodyForSIgnUp())
                .setEmail(RandomStringUtils.randomAlphanumeric(10) + dateBuilder.currentTimeMinusOneHour() + "@mail.com")
                .setPassword(RandomStringUtils.randomAlphanumeric(10))
                .createRequest();

        new EVPrimeClient()
                .signUp(signUpRequest);

        Response loginResponse = new EVPrimeClient()
                .login(signUpRequest);

        loginResponseBody = loginResponse.body().as(LoginResponse.class);

        requestBody = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle("Liverpool - Manchester United football match")
                .setImage("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.goal.com%2Fen-sg%2Fnews%2Fliverpool-vs-manchester-united-lineups-live-updates%2Fbltf4a9e3c54804c6b8&psig=AOvVaw11pYwQiECKpPWu17jL6s6X&ust=1712771074871000&source=images&cd=vfe&opi=89978449&ved=0CBIQjRxqFwoTCOiy883XtYUDFQAAAAAdAAAAABAE")
                .setDate("2024-04-07")
                .setLocation("Anfield")
                .setDescription("The match between the biggest rivals.")
                .createRequest();

        Response response = new EVPrimeClient()
                .postEvent(requestBody, loginResponseBody.getToken());

        postResponse = response.body().as(PostUpdateDeleteEventResponse.class);
        id = postResponse.getMessage().substring(39);
    }

    @Test
    public void updateEventTest() throws SQLException {
        requestBody.setDate("2024-04-08");

        Response updateResponse = new EVPrimeClient()
                .updateEvent(requestBody, loginResponseBody.getToken(), postResponse.getMessage().substring(39));

        PostUpdateDeleteEventResponse updateResponseBody = updateResponse.body().as(PostUpdateDeleteEventResponse.class);

        assertEquals(201, updateResponse.statusCode());
        assertTrue(updateResponseBody.getMessage().contains("Successfully updated the event with id: "));
        assertEquals(requestBody.getDate(),dbClient.getEventFromDB(postResponse.getMessage().substring(39)).getDate());

    }
    @Test
    public void unsuccessfulRequestAuthorizationTokenTest() {

        new EVPrimeClient().signUp(signUpRequest);

        Response response = new EVPrimeClient().postEvent(requestBody, "invalid_or_no_token");

        assertEquals(401, response.statusCode());
        assertEquals("Not authenticated.",response.as(PostUpdateDeleteEventResponse.class).getMessage());

    }

    @Test
    public void invalidTitleTest(){

        PostUpdateEventRequest requestBodyWithInvalidTitle = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle("")
                .setImage("https://example.com/image.jpg")
                .setDate(dateBuilder.currentTime())
                .setLocation(RandomStringUtils.randomAlphanumeric(15))
                .setDescription(RandomStringUtils.randomAlphanumeric(20))
                .createRequest();

        Response updateResponse = new EVPrimeClient()
                .updateEvent(requestBodyWithInvalidTitle, loginResponseBody.getToken(), postResponse.getMessage().substring(39));

        assertEquals(422,updateResponse.statusCode());
        assertEquals("Updating the event failed due to validation errors.", updateResponse.as(PostUpdateErrorsResponse.class).getMessage());
        assertEquals("Invalid title.",updateResponse.as(PostUpdateErrorsResponse.class).getErrors().getTitle());
    }
    @Test
    public void invalidImageTest(){

        PostUpdateEventRequest requestBodyWithInvalidImage = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle(RandomStringUtils.randomAlphanumeric(10))
                .setImage("")
                .setDate(dateBuilder.currentTime())
                .setLocation(RandomStringUtils.randomAlphanumeric(15))
                .setDescription(RandomStringUtils.randomAlphanumeric(20))
                .createRequest();

        Response updateResponse = new EVPrimeClient()
                .updateEvent(requestBodyWithInvalidImage, loginResponseBody.getToken(), postResponse.getMessage().substring(39));

        assertEquals(422, updateResponse.statusCode());
        assertEquals("Updating the event failed due to validation errors.", updateResponse.as(PostUpdateErrorsResponse.class).getMessage());
        assertEquals("Invalid image.",updateResponse.as(PostUpdateErrorsResponse.class).getErrors().getImage());
    }

    @Test
    public void invalidDateTest(){

        PostUpdateEventRequest requestBodyWithInvalidDate = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle(RandomStringUtils.randomAlphanumeric(10))
                .setImage("https://picture.jpg")
                .setDate("")
                .setLocation(RandomStringUtils.randomAlphanumeric(15))
                .setDescription(RandomStringUtils.randomAlphanumeric(20))
                .createRequest();

        Response updateResponse = new EVPrimeClient()
                .updateEvent(requestBodyWithInvalidDate, loginResponseBody.getToken(), postResponse.getMessage().substring(39));

        assertEquals(422, updateResponse.statusCode());
        assertEquals("Updating the event failed due to validation errors.", updateResponse.as(PostUpdateErrorsResponse.class).getMessage());
        assertEquals("Invalid date.",updateResponse.as(PostUpdateErrorsResponse.class).getErrors().getDate());
    }

    @Test
    @Ignore
    @Description("Possible bug")
    public void invalidLocationTest(){
        PostUpdateEventRequest requestBodyWithInvalidLocation = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle(RandomStringUtils.randomAlphanumeric(10))
                .setImage("https://picture.jpg")
                .setDate(dateBuilder.currentTime())
                .setLocation("")
                .setDescription(RandomStringUtils.randomAlphanumeric(20))
                .createRequest();

        Response updateResponse = new EVPrimeClient()
                .updateEvent(requestBodyWithInvalidLocation, loginResponseBody.getToken(), postResponse.getMessage().substring(39));

        assertEquals(422, updateResponse.statusCode());
        assertEquals("Updating the event failed due to validation errors.", updateResponse.as(PostUpdateErrorsResponse.class).getMessage());
        assertEquals("Invalid location.",updateResponse.as(PostUpdateErrorsResponse.class).getErrors().getDescription());
    }

    @Test
    public void invalidDescriptionTest(){
        PostUpdateEventRequest requestBodyWithInvalidDescription = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle(RandomStringUtils.randomAlphanumeric(10))
                .setImage("https://picture.jpg")
                .setDate(dateBuilder.currentTime())
                .setLocation(RandomStringUtils.randomAlphanumeric(20))
                .setDescription("")
                .createRequest();

        Response updateResponse = new EVPrimeClient()
                .updateEvent(requestBodyWithInvalidDescription, loginResponseBody.getToken(), postResponse.getMessage().substring(39));

        assertEquals(422, updateResponse.statusCode());
        assertEquals("Updating the event failed due to validation errors.", updateResponse.as(PostUpdateErrorsResponse.class).getMessage());
        assertEquals("Invalid description.",updateResponse.as(PostUpdateErrorsResponse.class).getErrors().getDescription());
    }

    @After
    public void deleteEvent() throws SQLException {
        if (id != null) {
            new DbClient().isEventDeletedFromDb(id);
        }
    }

}
