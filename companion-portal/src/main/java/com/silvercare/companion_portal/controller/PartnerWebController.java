package com.silvercare.companion_portal.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Controller
@RequestMapping("/partner")
public class PartnerWebController {

    @Value("${silvercare.api.base-url}")
    private String baseUrl;

    @Value("${silvercare.api.partner-endpoint}")
    private String partnerEndpoint;

    private final RestTemplate restTemplate = new RestTemplate();

    // ===================== PAGES (static HTML, no thymeleaf) =====================

    @GetMapping("/login")
    public String loginPage() {
        return "partner/login"; // partner/login.html (no th:* inside)
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session) {

        String url = baseUrl + partnerEndpoint + "/login";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("email", email);
        form.add("password", password);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object tokenObj = response.getBody().get("token");
                if (tokenObj != null) {
                    session.setAttribute("token", tokenObj.toString());
                    return "redirect:/partner/bookings";
                }
            }

            // invalid login
            return "redirect:/partner/login?error=Invalid+login";

        } catch (HttpStatusCodeException e) {
            // backend returned 4xx/5xx
            System.out.println("[SPRING] login API status: " + e.getStatusCode());
            System.out.println("[SPRING] login API body: " + e.getResponseBodyAsString());
            return "redirect:/partner/login?error=Login+failed";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/partner/login?error=Login+failed";
        }
    }

    @GetMapping("/bookings")
    public String bookingsPage(HttpSession session) {
        // page access guard
        if (session.getAttribute("token") == null) return "redirect:/partner/login";
        return "partner/bookings"; // partner/bookings.html (JS will fetch data)
    }

    @GetMapping("/bookings/{id}")
    public String bookingViewPage(@PathVariable("id") int bookingId, HttpSession session) {
        if (session.getAttribute("token") == null) return "redirect:/partner/login";
        // If you are doing JS detail page, return static template:
        return "partner/booking_view"; // optional page
        // If you still use thymeleaf here, it will break your "remove thymeleaf" goal.
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/partner/login";
    }

    // ===================== DATA (proxy endpoints for JS, avoids CORS) =====================

    @GetMapping("/bookings/data")
    @ResponseBody
    public ResponseEntity<?> bookingsData(HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "unauthorized"));

        String url = baseUrl + partnerEndpoint + "/bookings";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        try {
            ResponseEntity<Object> response =
                    restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class);

            return ResponseEntity.ok(response.getBody());

        } catch (HttpStatusCodeException e) {
            System.out.println("[SPRING] bookings API status: " + e.getStatusCode());
            System.out.println("[SPRING] bookings API body: " + e.getResponseBodyAsString());

            // if token invalid, clear it
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) session.removeAttribute("token");

            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", "api_error", "status", e.getStatusCode().value()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "portal_error"));
        }
    }

    @GetMapping("/bookings/{id}/data")
    @ResponseBody
    public ResponseEntity<?> bookingDetailData(@PathVariable("id") int bookingId, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "unauthorized"));

        String url = baseUrl + partnerEndpoint + "/bookings/" + bookingId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        try {
            ResponseEntity<Object> response =
                    restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class);

            return ResponseEntity.ok(response.getBody());

        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) session.removeAttribute("token");
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", "api_error", "status", e.getStatusCode().value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "portal_error"));
        }
    }
}
