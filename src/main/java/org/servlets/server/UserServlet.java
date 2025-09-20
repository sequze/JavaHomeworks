package org.servlets.server;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.servlets.models.User;

@WebServlet(urlPatterns = "/user")
public class UserServlet extends HttpServlet {

    private static final Map<Long, User> users = new HashMap<>();
    private static Long idGenerator = 1L;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static Long id() {
        return idGenerator++;
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = objectMapper.readValue(req.getInputStream(), User.class);

        Long id = id();

        user.setId(id);
        users.put(id, user);

        resp.setContentType("application/json");
        objectMapper.writeValue(resp.getOutputStream(), user);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        resp.setContentType("application/json");

        if (idParam == null) {
            objectMapper.writeValue(resp.getOutputStream(), users.values());
        } else {
            long id = Long.parseLong(idParam);
            User user = users.get(id);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(resp.getOutputStream(),
                        Map.of("error", "User with id=" + id + " not found"));
            } else {
                objectMapper.writeValue(resp.getOutputStream(), user);
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        resp.setContentType("application/json");

        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(),
                    Map.of("error", "Missing id parameter"));
            return;
        }

        long id = Long.parseLong(idParam);
        User user = users.get(id);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getOutputStream(),
                    Map.of("error", "User with id=" + id + " not found"));
            return;
        }

        User updated = objectMapper.readValue(req.getInputStream(), User.class);
        user.setName(updated.getName());
        user.setEmail(updated.getEmail());
        objectMapper.writeValue(resp.getOutputStream(), user);
    }

    // DELETE (DELETE /user?id=1)
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        resp.setContentType("application/json");

        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(),
                    Map.of("error", "Missing id parameter"));
            return;
        }

        long id = Long.parseLong(idParam);
        User removed = users.remove(id);
        if (removed == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getOutputStream(),
                    Map.of("error", "User with id=" + id + " not found"));
        } else {
            objectMapper.writeValue(resp.getOutputStream(),
                    Map.of("message", "User with id=" + id + " deleted"));
        }
    }
}
