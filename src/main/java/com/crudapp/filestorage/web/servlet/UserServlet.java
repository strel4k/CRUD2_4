package com.crudapp.filestorage.web.servlet;

import com.crudapp.filestorage.model.User;
import com.crudapp.filestorage.service.UserService;
import com.crudapp.filestorage.service.impl.UserServiceImpl;
import com.crudapp.filestorage.web.ApiError;
import com.crudapp.filestorage.dto.CreateUserRequest;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.crudapp.filestorage.util.JsonUtil.writeJson;
import static com.crudapp.filestorage.util.JsonUtil.readBody;

@WebServlet(name = "UserServlet", urlPatterns = {"/api/users", "/api/users/*"})
public class UserServlet extends HttpServlet {
    private final UserService users = new UserServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CreateUserRequest body = readBody(req.getInputStream(), CreateUserRequest.class);
            User u = users.create(body.name());
            writeJson(resp, HttpServletResponse.SC_CREATED, u);
        } catch (Exception e) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError(e.getMessage()));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path != null && path.length() > 1) {
            long id = parseId(path);
            Optional<User> u = users.get(id);
            if (u.isPresent()) {
                writeJson(resp, HttpServletResponse.SC_OK, u.get());
            } else {
                writeJson(resp, HttpServletResponse.SC_NOT_FOUND, new ApiError("User not found"));
            }
            return;
        }
        int offset = parseInt(req.getParameter("offset"), 0);
        int limit  = parseInt(req.getParameter("limit"), 50);
        List<User> list = users.list(offset, limit);
        writeJson(resp, HttpServletResponse.SC_OK, list);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.length() <= 1) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("User id required in path"));
            return;
        }
        long id = parseId(path);
        try {
            CreateUserRequest body = readBody(req.getInputStream(), CreateUserRequest.class);
            User u = users.rename(id, body.name());
            writeJson(resp, HttpServletResponse.SC_OK, u);
        } catch (Exception e) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError(e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.length() <= 1) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("User id required in path"));
            return;
        }
        long id = parseId(path);
        try {
            users.delete(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            writeJson(resp, HttpServletResponse.SC_CONFLICT, new ApiError(e.getMessage()));
        }
    }

    private static long parseId(String path) {
        return Long.parseLong(path.substring(1));
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception ignore) {
            return def;
        }
    }
}

