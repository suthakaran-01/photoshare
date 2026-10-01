package com.photoshare;


import org.springframework.test.context.ActiveProfiles;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.photoshare.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GalleryFlowTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean StorageService storage;

    String adminToken;
    String memberToken;
    String otherAdminToken;
    Long eventId;

    @BeforeEach
    void setup() throws Exception {
        when(storage.store(any(), any()))
                .thenReturn(new StorageService.StoredFile("https://img.example/x.jpg", "public-id-x"));

        String suffix = String.valueOf(System.nanoTime());
        adminToken = registerAdmin("admin" + suffix + "@t.com", "Admin@1234");
        otherAdminToken = registerAdmin("other" + suffix + "@t.com", "Admin@1234");
        eventId = createEvent(adminToken, "Test Wedding");
        addMember(adminToken, eventId, "Ravi", "member" + suffix + "@t.com", "Member@1234");
        memberToken = login("member" + suffix + "@t.com", "Member@1234");
    }

    // ---------- Authentication ----------

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@t.com\",\"password\":\"wrongpass1\"}"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mvc.perform(get("/api/events"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithValidTokenSucceeds() throws Exception {
        mvc.perform(get("/api/events")
                .header("Authorization", "Bearer " + adminToken))
           .andExpect(status().isOk());
    }

    // ---------- Photo access controls ----------

    @Test
    void teamMemberCannotCreateEvents() throws Exception {
        mvc.perform(post("/api/events")
                .header("Authorization", "Bearer " + memberToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Hack Event\"}"))
           .andExpect(status().isForbidden());
    }

    @Test
    void otherAdminCannotAccessSomeoneElsesEvent() throws Exception {
        mvc.perform(get("/api/events/" + eventId)
                .header("Authorization", "Bearer " + otherAdminToken))
           .andExpect(status().isForbidden());
    }

    @Test
    void teamMemberSeesOnlyOwnUploadedPhotos() throws Exception {
        Long adminPhotoId = uploadOnePhoto(adminToken, eventId, "admin-photo.jpg");
        Long memberPhotoId = uploadOnePhoto(memberToken, eventId, "member-photo.jpg");

        String body = mvc.perform(get("/api/events/" + eventId + "/photos")
                .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode photos = om.readTree(body);
        assert photos.isArray() && photos.size() == 1 : "Member should see exactly 1 photo, saw " + photos.size();
        assert photos.get(0).get("id").asLong() == memberPhotoId;
    }

    @Test
    void unassignedMemberCannotAccessEventPhotos() throws Exception {
        String outsiderToken = registerMemberViaAdmin(adminToken, otherAdminToken,
                "outsider" + System.nanoTime() + "@t.com", "Outsider@1234");
        // outsiderToken belongs to a member not added to `eventId`
        mvc.perform(get("/api/events/" + eventId + "/photos")
                .header("Authorization", "Bearer " + outsiderToken))
           .andExpect(status().isForbidden());
    }

    // ---------- Gallery publishing ----------

    @Test
    void teamMemberCannotPublishGallery() throws Exception {
        Long photoId = uploadOnePhoto(adminToken, eventId, "p.jpg");
        mvc.perform(post("/api/events/" + eventId + "/gallery/publish")
                .header("Authorization", "Bearer " + memberToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"photoIds\":[" + photoId + "]}"))
           .andExpect(status().isForbidden());
    }

    @Test
    void publishingWithPhotoFromAnotherEventFails() throws Exception {
        mvc.perform(post("/api/events/" + eventId + "/gallery/publish")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"photoIds\":[999999]}"))
           .andExpect(status().isBadRequest());
    }

    // ---------- PIN verification ----------

    @Test
    void fullPublishThenPinVerificationFlow() throws Exception {
        Long photoId = uploadOnePhoto(adminToken, eventId, "p.jpg");

        String publishBody = mvc.perform(post("/api/events/" + eventId + "/gallery/publish")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"photoIds\":[" + photoId + "]}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode publish = om.readTree(publishBody);
        String galleryUrl = publish.get("galleryUrl").asText();
        String pin = publish.get("pin").asText();
        String token = galleryUrl.substring(galleryUrl.indexOf("?t=") + 3);

        // wrong PIN
        mvc.perform(post("/api/public/gallery/" + token + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pin\":\"000000\"}"))
           .andExpect(status().isUnauthorized());

        // no bypassing photos endpoint without verifying
        mvc.perform(get("/api/public/gallery/" + token + "/photos")
                .header("X-Gallery-Access", "fake-token"))
           .andExpect(status().isUnauthorized());

        // correct PIN
        String verifyBody = mvc.perform(post("/api/public/gallery/" + token + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pin\":\"" + pin + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String accessToken = om.readTree(verifyBody).get("accessToken").asText();

        mvc.perform(get("/api/public/gallery/" + token + "/photos")
                .header("X-Gallery-Access", accessToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void unpublishedOrUnknownGalleryReturns404() throws Exception {
        mvc.perform(post("/api/public/gallery/does-not-exist/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pin\":\"123456\"}"))
           .andExpect(status().isNotFound());
    }

    // ---------- Helpers ----------

    private String registerAdmin(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Admin\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(body).get("token").asText();
    }

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(body).get("token").asText();
    }

    private Long createEvent(String token, String name) throws Exception {
        String body = mvc.perform(post("/api/events")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(body).get("id").asLong();
    }

    private void addMember(String adminToken, Long eventId, String name, String email, String tempPass) throws Exception {
        mvc.perform(post("/api/events/" + eventId + "/members")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"temporaryPassword\":\"" + tempPass + "\"}"))
           .andExpect(status().isCreated());
    }

    private String registerMemberViaAdmin(String someAdminToken, String otherAdminToken, String email, String tempPass) throws Exception {
        Long otherEventId = createEvent(otherAdminToken, "Other Event");
        addMember(otherAdminToken, otherEventId, "Outsider", email, tempPass);
        return login(email, tempPass);
    }

    private Long uploadOnePhoto(String token, Long eventId, String filename) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", filename, "image/jpeg", "fake-image-bytes".getBytes());
        String body = mvc.perform(multipart("/api/events/" + eventId + "/photos")
                .file(file)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode uploaded = om.readTree(body).get("uploaded");
        return uploaded.get(0).get("id").asLong();
    }
}