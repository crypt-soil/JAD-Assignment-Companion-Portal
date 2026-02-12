package com.silvercare.companion_portal.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
/*
 * Lois Poh 
 * 2429478
 */
@RestController
@RequestMapping("/partner")
public class PartnerWebController {

    @Value("${silvercare.api.base-url}")
    private String baseUrl;

    @Value("${silvercare.api.partner-endpoint}")
    private String partnerEndpoint;

    private final RestTemplate restTemplate = new RestTemplate();

    // ===================== AUTH (ENDPOINTS ONLY) =====================

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email,
                                   @RequestParam String password,
                                   HttpSession session) { // Current user's session (using the stored token)

        String url = baseUrl + partnerEndpoint + "/login";

        HttpHeaders headers = new HttpHeaders();
     // Sets the request ContentType to application/x-www-form-urlencoded to send the login details as form fields
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); 
     // Creates the form payload map
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("email", email);
        form.add("password", password);

        try {
        	// Send POST request to the API login endpoint with (form + headers)
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, new HttpEntity<>(form, headers), Map.class); 

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object tokenObj = response.getBody().get("token"); // Extract "token" field from API response body
                if (tokenObj != null) {
                    String token = tokenObj.toString();
                    session.setAttribute("token", token); // Store token inside session so future requests can reuse it
                    return ResponseEntity.ok(Map.of("token", token)); // Return token as JSON response to frontend
                }
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid_login"));

        } catch (HttpStatusCodeException e) {
            System.out.println("[SPRING] login API status: " + e.getStatusCode());
            System.out.println("[SPRING] login API body: " + e.getResponseBodyAsString());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "login_failed"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "portal_error"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ===================== DATA (ENDPOINTS ONLY) =====================
    
    // Helper method to get token from either:
    // 1) Authorization header "Bearer <token>"
    // 2) Session attribute "token"
    private String resolveToken(HttpSession session, String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return (String) session.getAttribute("token");
    }

    @GetMapping("/bookings/data")
    public ResponseEntity<?> bookingsData(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpSession session
    ) {
        String token = resolveToken(session, authorization);
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "unauthorized"));
        }

        String url = baseUrl + partnerEndpoint + "/bookings";

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
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "portal_error"));
        }
    }

    @GetMapping("/bookings/{id}/data")
    public ResponseEntity<?> bookingDetailData(
            @PathVariable("id") int bookingId,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpSession session
    ) {
        String token = resolveToken(session, authorization);
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "unauthorized"));
        }

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
