package com.crudapp.filestorage.web.servlet;
import com.crudapp.filestorage.dto.FileRenameRequest;
import com.crudapp.filestorage.model.FileEntity;
import com.crudapp.filestorage.service.EventService;
import com.crudapp.filestorage.service.FileService;
import com.crudapp.filestorage.service.impl.EventServiceImpl;
import com.crudapp.filestorage.service.impl.FileServiceImpl;

import com.crudapp.filestorage.web.ApiError;
import com.crudapp.filestorage.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static com.crudapp.filestorage.util.JsonUtil.*;


@WebServlet(name = "FileServlet", urlPatterns = {"/api/files", "/api/files/*"})
@MultipartConfig(fileSizeThreshold = 512 * 1024, maxFileSize = 50L * 1024 * 1024, maxRequestSize = 60L * 1024 * 1024)
public class FileServlet extends HttpServlet {
    private final FileService files = new FileServiceImpl();
    private final EventService events = new EventServiceImpl();

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            Part part = req.getPart("file");
            if (part == null || part.getSize() == 0) {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("Part 'file' is required"));
                return;
            }
            String original = part.getSubmittedFileName();
            try (InputStream in = part.getInputStream()) {
                FileEntity fe = files.upload(original, in);
                Long userId = parseLong(req.getParameter("userId"));
                if (userId != null) {
                    events.record(userId, fe.getId(), "UPLOAD");
                }
                writeJson(resp, HttpServletResponse.SC_CREATED, fe);
            }
        } catch (Exception e) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError(e.getMessage()));
        }
    }
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("File id is required in path"));
        }
        String[] parts = path.split("/");
        if (parts.length < 2 || parts[1].isBlank()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("Invalid path; expected /api/files/{id}"));
        }

        long id = -1L;
        try {
            id = Long.parseLong(parts[1]);
        }
        catch (NumberFormatException e) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("Invalid id: " + parts[1]));
        }
        var reqDto = JsonUtil.readBody(req.getInputStream(), FileRenameRequest.class);
        if (reqDto == null || reqDto.getName() == null || reqDto.getName().trim().isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("Field 'name' is required"));
        }
        try {
            FileEntity updated = new FileServiceImpl().rename(id, reqDto.getName());
            writeJson(resp, HttpServletResponse.SC_OK, updated);
        } catch (IllegalArgumentException ex) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError(ex.getMessage()));
        } catch (Exception ex) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiError("Failed to rename file"));
        }
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            int offset = parseInt(req.getParameter("offset"), 0);
            int limit  = parseInt(req.getParameter("limit"), 50);
            writeJson(resp, HttpServletResponse.SC_OK, files.list(offset, limit));
            return;
        }

        if (path.endsWith("/content")) {
            long id = Long.parseLong(path.substring(1, path.length() - "/content".length()));
            try {
                byte[] bytes = files.download(id);
                Long userId = parseLong(req.getParameter("userId"));
                if (userId != null) {
                    events.record(userId, id, "DOWNLOAD");
                }
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/octet-stream");
                resp.setHeader("Content-Disposition", "attachment; filename=\"file-" + id + "\"");
                resp.getOutputStream().write(bytes);
            } catch (Exception e) {
                writeJson(resp, HttpServletResponse.SC_NOT_FOUND, new ApiError(e.getMessage()));
            }
            return;
        }

        long id = Long.parseLong(path.substring(1));
        Optional<FileEntity> fe = files.get(id);
        if (fe.isPresent()) {
            writeJson(resp, HttpServletResponse.SC_OK, fe.get());
        } else {
            writeJson(resp, HttpServletResponse.SC_NOT_FOUND, new ApiError("File not found"));
        }
    }

    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.length() <= 1) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiError("File id required in path"));
            return;
        }
        long id = Long.parseLong(path.substring(1));
        try {
            files.delete(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            writeJson(resp, HttpServletResponse.SC_CONFLICT, new ApiError(e.getMessage()));
        }
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception ignore) { return def; }
    }
    private static Long parseLong(String s) {
        try { return (s == null || s.isBlank()) ? null : Long.parseLong(s); } catch (Exception e) { return null; }
    }
}
