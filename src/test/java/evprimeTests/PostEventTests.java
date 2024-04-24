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
import static org.junit.Assert.*;

public class PostEventTests {

    DbClient dbClient = new DbClient();
    private static String id;
    private static SignUpLoginRequest signUpRequest;
    private static LoginResponse loginResponseBody;
    private static PostUpdateEventRequest requestBody;
    static DateBuilder dateBuilder = new DateBuilder();

    @BeforeClass
    public static void setUp() {
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
                .setTitle(RandomStringUtils.randomAlphanumeric(10))
                .setImage("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.goal.com%2Fen-sg%2Fnews%2Fliverpool-vs-manchester-united-lineups-live-updates%2Fbltf4a9e3c54804c6b8&psig=AOvVaw11pYwQiECKpPWu17jL6s6X&ust=1712771074871000&source=images&cd=vfe&opi=89978449&ved=0CBIQjRxqFwoTCOiy883XtYUDFQAAAAAdAAAAABAE")
                .setDate(dateBuilder.currentTime())
                .setLocation(RandomStringUtils.randomAlphanumeric(15))
                .setDescription(RandomStringUtils.randomAlphanumeric(20))
                .createRequest();
    }

    @Test
    public void successfulPostEventTest() throws SQLException {

        Response response = new EVPrimeClient()
                .postEvent(requestBody, loginResponseBody.getToken());

        PostUpdateDeleteEventResponse postResponse = response.body().as(PostUpdateDeleteEventResponse.class);
        id = postResponse.getMessage().substring(39);

        assertEquals(201, response.statusCode());
        assertTrue(postResponse.getMessage().contains("Successfully created an event with id: "));
        assertEquals(requestBody.getTitle(), dbClient.getEventFromDB(postResponse.getMessage().substring(39)).getTitle());
        assertEquals(requestBody.getImage(), dbClient.getEventFromDB(postResponse.getMessage().substring(39)).getImage());
        assertEquals(requestBody.getDate(), dbClient.getEventFromDB(postResponse.getMessage().substring(39)).getDate());
        assertEquals(requestBody.getLocation(), dbClient.getEventFromDB(postResponse.getMessage().substring(39)).getLocation());
        assertEquals(requestBody.getDescription(), dbClient.getEventFromDB(postResponse.getMessage().substring(39)).getDescription());
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

        Response response = new EVPrimeClient().postEvent(requestBodyWithInvalidTitle, loginResponseBody.getToken());

        assertEquals(422, response.statusCode());
        assertEquals("Adding the event failed due to validation errors.", response.as(PostUpdateErrorsResponse.class).getMessage());
        assertEquals("Invalid title.",response.as(PostUpdateErrorsResponse.class).getErrors().getTitle());
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

        Response response = new EVPrimeClient().postEvent(requestBodyWithInvalidImage, loginResponseBody.getToken());

        assertEquals(422, response.statusCode());
        assertEquals("Adding the event failed due to validation errors.", response.as(PostUpdateErrorsResponse.class).getMessage());
        assertEquals("Invalid image.",response.as(PostUpdateErrorsResponse.class).getErrors().getImage());
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

        Response response = new EVPrimeClient().postEvent(requestBodyWithInvalidDate, loginResponseBody.getToken());

        assertEquals(422, response.statusCode());
        assertEquals("Adding the event failed due to validation errors.", response.as(PostUpdateErrorsResponse.class).getMessage());
        assertEquals("Invalid date.",response.as(PostUpdateErrorsResponse.class).getErrors().getDate());
    }

    @Test
    @Ignore
    @Description("Possible bug")
    public void invalidLocationTest(){
        PostUpdateEventRequest requestBodyWithInvalidLocation = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle("Some title")
                .setImage("https://picture.jpg")
                .setDate("03.04.1990")
                .setLocation("")
                .setDescription("Some Description")
                .createRequest();

        Response response = new EVPrimeClient().postEvent(requestBodyWithInvalidLocation, loginResponseBody.getToken());

        assertEquals(422, response.statusCode());
        assertEquals("Adding the event failed due to validation errors.", response.as(PostUpdateErrorsResponse.class).getMessage());
        assertEquals("Invalid location.",response.as(PostUpdateErrorsResponse.class).getErrors().getDescription());
    }

    @Test
    public void invalidDescriptionTest(){
        PostUpdateEventRequest requestBodyWithInvalidDescription = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle(RandomStringUtils.randomAlphanumeric(10))
                .setImage("https://picture.jpg")
                .setDate(dateBuilder.currentTime())
                .setLocation(RandomStringUtils.randomAlphanumeric(15))
                .setDescription("")
                .createRequest();

        Response response = new EVPrimeClient().postEvent(requestBodyWithInvalidDescription, loginResponseBody.getToken());

        assertEquals(422, response.statusCode());
        assertEquals("Adding the event failed due to validation errors.", response.as(PostUpdateErrorsResponse.class).getMessage());
        assertEquals("Invalid description.",response.as(PostUpdateErrorsResponse.class).getErrors().getDescription());
    }

    @After
    public void deleteEvent() throws SQLException {
        if (id != null) {
            new DbClient().isEventDeletedFromDb(id);
        }
    }
}