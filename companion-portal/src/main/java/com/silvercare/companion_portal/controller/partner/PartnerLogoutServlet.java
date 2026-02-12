package com.silvercare.companion_portal.controller.partner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import com.silvercare.companion_portal.model.partner.PartnerAuthDAO;
import com.silvercare.companion_portal.model.partner.PartnerInfo;

@WebServlet("/partner/logout")
public class PartnerLogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = readBearerToken(request);
        if (token == null) {
            response.setStatus(401);
            response.getWriter().write("{\"error\":\"Missing token\"}");
            return;
        }

        PartnerAuthDAO dao = new PartnerAuthDAO();
        dao.deleteToken(token);

        response.setContentType("application/json");
        response.getWriter().write("{\"ok\":true}");
    }

    private String readBearerToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null) return null;
        auth = auth.trim();
        if (!auth.startsWith("Bearer ")) return null;
        return auth.substring("Bearer ".length()).trim();
    }
}
