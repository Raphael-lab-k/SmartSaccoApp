# Implement User Authentication (Login & Signup)

This plan outlines the steps to add basic User Authentication to the Smart Sacco app, providing a secure entry point for members.

## User Review Required

> [!IMPORTANT]
> The initial implementation will use **local validation** and a mock authentication service. This allows us to build the UI and navigation flow quickly. We can later integrate a real backend like **Firebase** or a **REST API**.

## Open Questions

- Would you like to use **Firebase Authentication** for a production-ready solution, or should we stick to a local mock implementation for now?
- Do you have specific branding (colors/logo) you'd like to use for the Login screen?

## Proposed Changes

### UI Components

#### [NEW] [activity_login.xml](file:///C:/Users/NinyeTinyefuza/AndroidStudioProjects/SmartSaccoApp/app/src/main/res/layout/activity_login.xml)
A clean login screen with fields for Email/Phone and Password, and a button to go to the Signup screen.

#### [NEW] [activity_signup.xml](file:///C:/Users/NinyeTinyefuza/AndroidStudioProjects/SmartSaccoApp/app/src/main/res/layout/activity_signup.xml)
A registration screen for new members to enter their Name, Email, Phone, and Password.

### Logic & Navigation

#### [NEW] [LoginActivity.java](file:///C:/Users/NinyeTinyefuza/AndroidStudioProjects/SmartSaccoApp/app/src/main/java/com/example/smartsaccoapp/ui/auth/LoginActivity.java)
Handles login input validation and navigation to the main dashboard upon success.

#### [NEW] [SignupActivity.java](file:///C:/Users/NinyeTinyefuza/AndroidStudioProjects/SmartSaccoApp/app/src/main/java/com/example/smartsaccoapp/ui/auth/SignupActivity.java)
Handles user registration logic.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/NinyeTinyefuza/AndroidStudioProjects/SmartSaccoApp/app/src/main/AndroidManifest.xml)
Register the new activities and set `LoginActivity` as the launcher activity to ensure users log in first.

#### [NEW] [AuthManager.java](file:///C:/Users/NinyeTinyefuza/AndroidStudioProjects/SmartSaccoApp/app/src/main/java/com/example/smartsaccoapp/data/AuthManager.java)
A simple helper class to manage user session state (e.g., using `SharedPreferences`).

---

## Verification Plan

### Automated Tests
- We will add unit tests for `AuthManager` to verify session persistence.

### Manual Verification
1. Launch the app and verify it opens the Login screen.
2. Test validation (empty fields, invalid email format).
3. Verify navigation from Login to Signup and back.
4. Verify that "logging in" redirects to the `MainActivity` dashboard.
