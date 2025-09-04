package com.crudapp.filestorage.web.servlet;

import com.crudapp.filestorage.web.ApiError;
import com.crudapp.filestorage.dto.EventCreateRequest;
import com.crudapp.filestorage.dto.EventUpdateRequest;
import com.crudapp.filestorage.model.Event;
import com.crudapp.filestorage.service.EventService;
import com.crudapp.filestorage.service.impl.EventServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import static com.crudapp.filestorage.util.JsonUtil.*;

@WebServlet(name = "EventServlet", urlPatterns = "/api/events/*")
public class EventServlet extends HttpServlet {
    private final EventService events = new EventServiceImpl();

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception ignore) { return def; }
    }
    private static Long pathId(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || "/".equals(path)) return null;
        String[] parts = path.split("/");
        if (parts.length < 2 || parts[1].isBlank()) return null;
        try { return Long.parseLong(parts[1]); } catch (NumberFormatException e) { return -1L; }
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long idInPath = pathId(req);
        if (idInPath != null) {
            if (idInPath <= 0) {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("Invalid id in path"));
                return;
            }
            try {
                Event e = events.get(idInPath);
                writeJson(resp, HttpServletResponse.SC_OK, e);
            } catch (IllegalArgumentException ex) {
                writeJson(resp, HttpServletResponse.SC_NOT_FOUND, new ApiError(ex.getMessage()));
            }
            return;
        }

        int offset = parseInt(req.getParameter("offset"), 0);
        int limit  = parseInt(req.getParameter("limit"), 50);
        String userId = req.getParameter("userId");
        String fileId = req.getParameter("fileId");

        if (userId != null && !userId.isBlank()) {
            long uid = Long.parseLong(userId);
            writeJson(resp, HttpServletResponse.SC_OK, events.forUser(uid, offset, limit));
            return;
        }
        if (fileId != null && !fileId.isBlank()) {
            long fid = Long.parseLong(fileId);
            writeJson(resp, HttpServletResponse.SC_OK, events.forFile(fid, offset, limit));
            return;
        }

        writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("Specify userId or fileId"));
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long idInPath = pathId(req);
        if (idInPath != null) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("POST /api/events without id"));
            return;
        }

        EventCreateRequest body = readBody(req.getInputStream(), EventCreateRequest.class);
        if (body == null || body.getUserId() == null || body.getFileId() == null || body.getEventType() == null) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("userId, fileId and eventType are required"));
            return;
        }

        try {
            Event created = events.create(body.getUserId(), body.getFileId(), body.getEventType());
            writeJson(resp, HttpServletResponse.SC_CREATED, created);
        } catch (IllegalArgumentException ex) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError(ex.getMessage()));
        } catch (Exception ex) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiError("Failed to create event"));
        }
    }

    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long idInPath = pathId(req);
        if (idInPath == null || idInPath <= 0) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("Event id is required in path"));
            return;
        }

        EventUpdateRequest body = readBody(req.getInputStream(), EventUpdateRequest.class);
        if (body == null || body.getEventType() == null || body.getEventType().isBlank()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("eventType is required"));
            return;
        }

        try {
            Event updated = events.updateType(idInPath, body.getEventType());
            writeJson(resp, HttpServletResponse.SC_OK, updated);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg != null && msg.startsWith("Event not found")) {
                writeJson(resp, HttpServletResponse.SC_NOT_FOUND, new ApiError(msg));
            } else {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError(msg));
            }
        } catch (Exception ex) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiError("Failed to update event"));
        }
    }

    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long idInPath = pathId(req);
        if (idInPath != null) {
            if (idInPath <= 0) {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("Invalid id"));
                return;
            }
            boolean ok = events.delete(idInPath);
            if (ok) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                writeJson(resp, HttpServletResponse.SC_NOT_FOUND, new ApiError("Event not found: " + idInPath));
            }
            return;
        }

        String userId = req.getParameter("userId");
        String fileId = req.getParameter("fileId");
        if (userId != null && !userId.isBlank()) {
            long n = events.purgeForUser(Long.parseLong(userId));
            writeJson(resp, HttpServletResponse.SC_OK, java.util.Map.of("deleted", n));
            return;
        }
        if (fileId != null && !fileId.isBlank()) {
            long n = events.purgeForFile(Long.parseLong(fileId));
            writeJson(resp, HttpServletResponse.SC_OK, java.util.Map.of("deleted", n));
            return;
        }
        writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("Specify id in path or userId/fileId param"));
    }
}