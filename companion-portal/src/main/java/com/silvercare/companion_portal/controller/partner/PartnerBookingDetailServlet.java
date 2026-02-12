package com.silvercare.companion_portal.controller.partner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.silvercare.companion_portal.model.partner.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/partner/bookings/*")
public class PartnerBookingDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = readBearerToken(request);
        if (token == null) {
            sendJson(response, 401, "{\"error\":\"Missing token\"}");
            return;
        }

        PartnerAuthDAO authDAO = new PartnerAuthDAO();
        Integer partnerId = authDAO.validateToken(token);
        if (partnerId == null) {
            sendJson(response, 401, "{\"error\":\"Invalid or expired token\"}");
            return;
        }

        // Expect: /api/partner/bookings/{detailId}
        String pathInfo = request.getPathInfo(); // e.g. "/4"
        int detailId = parseId(pathInfo);
        if (detailId <= 0) {
            sendJson(response, 400, "{\"error\":\"Invalid detail id\"}");
            return;
        }

        PartnerBookingDAO dao = new PartnerBookingDAO();
        Map<String, Object> detail = dao.getPartnerBookingDetail(partnerId, detailId);

        if (detail == null) {
            sendJson(response, 404, "{\"error\":\"Not found\"}");
            return;
        }

        int customerId = (int) detail.get("customerId");
        List<Map<String, Object>> contacts = dao.getEmergencyContacts(customerId);

        // Build response object:
        Map<String, Object> out = new HashMap<>();
        out.put("detail", detail);

        // medical is inside detail already; but your JS expects {medical:{...}} too:
        Map<String, Object> medical = new HashMap<>();
        medical.put("conditionsCsv", detail.get("conditionsCsv"));
        medical.put("allergiesText", detail.get("allergiesText"));
        out.put("medical", medical);

        out.put("contacts", contacts);

        response.setContentType("application/json");
        response.getWriter().write(toJson(out));
    }

    private int parseId(String pathInfo) {
        if (pathInfo == null) return -1;
        String s = pathInfo.trim();
        if (s.startsWith("/")) s = s.substring(1);
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }

    private String readBearerToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null) return null;
        auth = auth.trim();
        if (!auth.startsWith("Bearer ")) return null;
        return auth.substring("Bearer ".length()).trim();
    }

    private void sendJson(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(json);
    }

    // simple JSON serializer for Map/list (matches your style)
    private String toJson(Object obj) {
        if (obj == null) return "null";

        if (obj instanceof Map<?, ?> m) {
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Object k : m.keySet()) {
                if (i++ > 0) sb.append(",");
                sb.append("\"").append(escape(String.valueOf(k))).append("\":")
                  .append(toJson(m.get(k)));
            }
            sb.append("}");
            return sb.toString();
        }

        if (obj instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }

        if (obj instanceof Number || obj instanceof Boolean) return String.valueOf(obj);

        return "\"" + escape(String.valueOf(obj)) + "\"";
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
