import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.servlets.models.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestApi {

    private static final String BASE_URL = "http://localhost:8080/user";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Long createdUserId;

    @Test
    @Order(1)
    void testCreateUser() throws IOException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();

        String jsonRequest = """
            {
                "name": "alice",
                "email": "alice@mail.ru"
            }
        """;

        // Создаем POST-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/user"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        User createdUser = mapper.readValue(response.body(), User.class);
        assertNotNull(createdUser);
        assertEquals("alice", createdUser.getName());
        assertEquals("alice@mail.ru", createdUser.getEmail());
        createdUserId = createdUser.getId();

    }

    @Test
    @Order(2)
    void testGetUser() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?id=" + createdUserId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        User user = mapper.readValue(response.body(), User.class);
        assertEquals("alice", user.getName());
        assertEquals("alice@mail.ru", user.getEmail());
    }

    @Test
    @Order(3)
    void testUpdateUser() throws IOException, InterruptedException {
        String jsonRequest = """
            {
                "name": "Alice Updated",
                "email": "alice2@mail.com"
            }
        """;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?id=" + createdUserId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        User user = mapper.readValue(response.body(), User.class);
        assertEquals("Alice Updated", user.getName());
        assertEquals("alice2@mail.com", user.getEmail());
    }

    @Test
    @Order(4)
    void testDeleteUser() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?id=" + createdUserId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    @Order(5)
    void testGetDeletedUser() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?id=" + createdUserId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }
}
