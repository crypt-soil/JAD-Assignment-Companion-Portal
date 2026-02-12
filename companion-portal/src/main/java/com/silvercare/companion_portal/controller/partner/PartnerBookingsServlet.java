package com.silvercare.companion_portal.controller.partner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.silvercare.companion_portal.model.partner.*;

@WebServlet("/api/partner/bookings")
public class PartnerBookingsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = readBearerToken(request);
        if (token == null) {
            response.setStatus(401);
            response.getWriter().write("{\"error\":\"Missing token\"}");
            return;
        }

        PartnerAuthDAO authDAO = new PartnerAuthDAO();
        Integer partnerId = authDAO.validateToken(token);

        if (partnerId == null) {
            response.setStatus(401);
            response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
            return;
        }

        PartnerBookingDAO bookingDAO = new PartnerBookingDAO();
        List<Map<String, Object>> bookings =
                bookingDAO.getPartnerBookings(partnerId);

        response.setContentType("application/json");
        response.getWriter().write(toJson(bookings));
    }

    private String readBearerToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null) return null;
        auth = auth.trim();
        if (!auth.startsWith("Bearer ")) return null;
        return auth.substring("Bearer ".length()).trim();
    }

    private String toJson(List<Map<String, Object>> list) {

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < list.size(); i++) {

            Map<String, Object> row = list.get(i);

            sb.append("{");

            int fieldCount = 0;

            for (Map.Entry<String, Object> entry : row.entrySet()) {

                if (fieldCount > 0) sb.append(",");

                sb.append("\"")
                  .append(entry.getKey())
                  .append("\":");

                Object value = entry.getValue();

                if (value == null) {
                    sb.append("null");
                } else if (value instanceof Number) {
                    sb.append(value);
                } else {
                    sb.append("\"")
                      .append(escape(value.toString()))
                      .append("\"");
                }

                fieldCount++;
            }

            sb.append("}");

            if (i < list.size() - 1) sb.append(",");
        }

        sb.append("]");
        return sb.toString();
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
