# Photo Sharing Platform

Full-stack photo-sharing app for event teams: Admins create events and
manage team members, Team Members upload photos, Admins curate and
publish a PIN-protected gallery, and Customers view it with just a link
and PIN - no account needed.

**Live app:** https://photoshare-5nmr.onrender.com
**Repository:** https://github.com/suthakaran-01/photoshare

## Demo Credentials
- Admin: admin@suthakaran-photoshare.com / DemoAdmin@2026
- Team Member: member@suthakaran-photoshare.com / DemoMember@2026
- Sample Gallery: https://photoshare-5nmr.onrender.com/gallery.html?t=DcPKdpzLpyyAGZ3fITyd3XP8 - PIN: 276817

## Tech Stack
- Backend: Java 25, Spring Boot 4.1.1, Spring Security + JWT
- Database: MySQL (Aiven, cloud-hosted)
- Storage: Cloudinary (photo files; only metadata is stored in MySQL)
- Frontend: HTML, Bootstrap 5, vanilla JavaScript
- Testing: JUnit 5, MockMvc, H2 (in-memory, isolated from production data)
- Deployment: Docker, Render (app), Aiven (database)

## Architecture
Browser -> Spring Boot (REST API + static frontend) -> MySQL (metadata)
                                                      -> Cloudinary (photos)

Layered backend: Controller -> Service -> Repository.
EventAccessService centralizes all event ownership/membership checks.
StorageService is the only class that talks to Cloudinary.

## Database Design
- users (id, name, email, password_hash, role)
- events (id, name, event_date, created_by)
- event_members (event_id, user_id) - who's assigned to which event
- photos (id, event_id, uploaded_by, filename, storage_url, file_size)
- galleries (id, event_id, share_token, pin_hash, published)
- gallery_photos (gallery_id, photo_id) - which photos are published

## Security
- JWT-based stateless authentication, BCrypt-hashed passwords
- Role-based authorization (ADMIN / TEAM_MEMBER) via @PreAuthorize
- Per-event ownership checks (EventAccessService) - a user can't access
  another admin's event or an event they're not assigned to
- Gallery PINs are BCrypt-hashed, never stored in plain text
- Brute-force protection on PIN verification (5 attempts / 15 min)
- File type and size validation on photo uploads

## Local Setup
1. Install Java 25, Maven, MySQL
2. Create a database: CREATE DATABASE photoshare;
3. Create src/main/resources/application-local.properties with your
   local MySQL password and Cloudinary credentials (see application.properties
   for the expected keys)
4. Run with: -Dspring.profiles.active=local
5. Visit http://localhost:8080

## Environment Variables (for deployment)
DATABASE_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET,
CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET,
APP_BASE_URL

## Testing
Run: mvn test
11 tests covering authentication, role-based access control,
event ownership checks, gallery publishing, and PIN verification
(including brute-force limiting). Tests run against an isolated
in-memory H2 database (application-test.properties, @ActiveProfiles("test")).

## Known Limitations
- Render's free tier sleeps after inactivity; first request after idle
  can take 30-60 seconds
- PIN brute-force limiting is in-memory (per-instance), not shared
  across multiple server instances
- Photo URLs on Cloudinary are unguessable but not access-controlled
  once known (no signed URLs)
- No email invitations for team members - admin sets a temporary password

