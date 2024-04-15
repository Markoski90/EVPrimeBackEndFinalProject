package evprimeTests;

import client.EVPrimeClient;
import io.restassured.response.Response;
import models.response.GetAllEventsResponse;
import org.junit.Test;

import static org.junit.Assert.*;

public class GetAllEventsTests {

    @Test
    public void getAllEventsTest(){
        Response response = new EVPrimeClient()
                .getAllEvents();

        GetAllEventsResponse responseBody = response.body().as(GetAllEventsResponse.class);

        assertEquals(200,response.statusCode());
        assertFalse(responseBody.getEvents().isEmpty());
    }
}



