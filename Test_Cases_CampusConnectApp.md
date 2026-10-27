# Test Case Document - CampusConnectApp

## Section 2: Project Information
* **Project Name:** CampusConnectApp
* **Platform:** Android (Jetpack Compose, Kotlin, Room Database, Firebase Auth & Firestore, Custom Tabs, Push Notifications)
* **Target Audience:** University of Malawi (UNIMA) Students, Lecturers, and Administrators
* **Prepared By:** Development Team / QA

---

## Test Cases

### 1. User Authentication & Login

| Field | Description |
| --- | --- |
| **Test Case ID** | AUTH_LOGIN_001 |
| **Description** | Verify that users can successfully log in using valid `@unima.ac.mw` credentials. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | User account exists in local Room database or Firebase Auth. |
| **Test Data** | Email: `cs-2023-4091@unima.ac.mw`, Password: `SecurePassword1!` |
| **Test Steps** | 1. Open app to Login Screen.<br>2. Enter valid `@unima.ac.mw` email.<br>3. Enter valid password.<br>4. Tap **Sign In** button. |
| **Expected Result** | User successfully authenticates, and app navigates to the main Home screen displaying personalized role view. |
| **Post-conditions** | User session is active and cached locally in Room DB. |
| **Status** | |

| Field | Description |
| --- | --- |
| **Test Case ID** | AUTH_LOGIN_002 |
| **Description** | Verify that login is blocked and error message appears when entering invalid email format. |
| **Test Type** | Negative |
| **Priority** | High |
| **Prerequisites** | App is on Login Screen. |
| **Test Data** | Email: `test@gmail.com`, Password: `SecurePassword1!` |
| **Test Steps** | 1. Enter email without `@unima.ac.mw` domain.<br>2. Enter password.<br>3. Tap **Sign In**. |
| **Expected Result** | Login is blocked, and an error message appears: *"Login requires a valid @unima.ac.mw email address"*. |
| **Post-conditions** | User remains on Login Screen. |
| **Status** | |

| Field | Description |
| --- | --- |
| **Test Case ID** | AUTH_LOGIN_003 |
| **Description** | Verify that login attempts with unregistered credentials are strictly rejected. |
| **Test Type** | Negative |
| **Priority** | High |
| **Prerequisites** | User email does not exist in local Room DB or Firebase Auth. |
| **Test Data** | Email: `nonexistent@unima.ac.mw`, Password: `WrongPassword1!` |
| **Test Steps** | 1. Enter unregistered email and password.<br>2. Tap **Sign In**. |
| **Expected Result** | Login is rejected, displaying error message: *"Account does not exist. Please check your credentials or register first."* |
| **Post-conditions** | User remains on Login Screen. |
| **Status** | |

---

### 2. User Registration & Role-Based Access Control

| Field | Description |
| --- | --- |
| **Test Case ID** | REG_STUDENT_001 |
| **Description** | Verify that students can successfully self-register without a staff access code. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | App is on Register Tab. |
| **Test Data** | Name: `Alex Johnson`, Reg Number: `CS-2023-4091`, Email: `cs-2023-4091@unima.ac.mw`, Password: `SecurePassword1!` |
| **Test Steps** | 1. Select **Student** role.<br>2. Enter Full Name, Department, and Student Reg Number.<br>3. Enter matching `@unima.ac.mw` email and strong password.<br>4. Tap **Register & Sign In**. |
| **Expected Result** | Account is successfully created and registered without requiring a staff access code. User is logged in as Student. |
| **Post-conditions** | User profile is saved to local Room DB and synced to Firestore. |
| **Status** | |

| Field | Description |
| --- | --- |
| **Test Case ID** | REG_STAFF_002 |
| **Description** | Verify that lecturer/admin registration is rejected when entering an invalid staff access code. |
| **Test Type** | Negative |
| **Priority** | High |
| **Prerequisites** | App is on Register Tab. |
| **Test Data** | Role: Lecturer, Staff ID: `EMP-LECT-882`, Code: `WRONG-CODE` |
| **Test Steps** | 1. Select **Lecturer** or **Admin** role.<br>2. Fill in valid staff details.<br>3. Enter incorrect staff access code.<br>4. Tap **Register & Sign In**. |
| **Expected Result** | Registration is rejected, and an error message appears stating that the Staff Access Code is invalid. |
| **Post-conditions** | User remains on Register Tab. |
| **Status** | |

| Field | Description |
| --- | --- |
| **Test Case ID** | REG_STAFF_003 |
| **Description** | Verify that lecturers can successfully register using the valid Staff Access Code. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | App is on Register Tab. |
| **Test Data** | Role: Lecturer, Code: `UNIMA-STAFF-2025` |
| **Test Steps** | 1. Select **Lecturer** role.<br>2. Fill in staff details.<br>3. Enter correct Staff Access Code (`UNIMA-STAFF-2025`).<br>4. Tap **Register & Sign In**. |
| **Expected Result** | Registration succeeds, granting lecturer privileges (e.g., ability to post announcements). |
| **Post-conditions** | User profile created with Lecturer role. |
| **Status** | |

---

### 3. Password Reset via Email

| Field | Description |
| --- | --- |
| **Test Case ID** | PWD_RESET_001 |
| **Description** | Verify that password reset email is successfully dispatched for existing registered users. |
| **Test Type** | Positive |
| **Priority** | Medium |
| **Prerequisites** | User is registered in the database. |
| **Test Data** | Email: `cs-2023-4091@unima.ac.mw` |
| **Test Steps** | 1. Tap **"Forgot Password? Reset via Email"**.<br>2. Enter registered `@unima.ac.mw` email in dialog.<br>3. Tap **Send Reset Email**. |
| **Expected Result** | System verifies email exists in database and Firebase Auth successfully dispatches password reset email to inbox. |
| **Post-conditions** | Reset email sent; confirmation Toast displayed. |
| **Status** | |

| Field | Description |
| --- | --- |
| **Test Case ID** | PWD_RESET_002 |
| **Description** | Verify that password reset requests for unregistered emails are rejected. |
| **Test Type** | Negative |
| **Priority** | Medium |
| **Prerequisites** | App is on Sign In Screen. |
| **Test Data** | Email: `unknown@unima.ac.mw` |
| **Test Steps** | 1. Tap **"Forgot Password? Reset via Email"**.<br>2. Enter unregistered email.<br>3. Tap **Send Reset Email**. |
| **Expected Result** | System rejects request because email does not exist in database, showing error message. |
| **Post-conditions** | User remains on reset dialog. |
| **Status** | |

---

### 4. Announcements & Push Notifications

| Field | Description |
| --- | --- |
| **Test Case ID** | ANN_TOOLTIP_001 |
| **Description** | Verify that hovering over an announcement displays full message via tooltip and clicking expands it. |
| **Test Type** | Positive |
| **Priority** | Low |
| **Prerequisites** | Announcements exist on Home Screen. |
| **Test Data** | None |
| **Test Steps** | 1. Hover mouse pointer over an announcement card.<br>2. Click on announcement card or expand arrow. |
| **Expected Result** | Hovering displays full message in a tooltip; clicking expands the card or opens full view dialog. |
| **Post-conditions** | Card state toggles between expanded and collapsed. |
| **Status** | |

| Field | Description |
| --- | --- |
| **Test Case ID** | ANN_POST_002 |
| **Description** | Verify that lecturers/admins can post announcements and authorized users can delete them. |
| **Test Type** | Positive |
| **Priority** | Medium |
| **Prerequisites** | Logged in as Lecturer or Administrator. |
| **Test Data** | Title: `Exam Schedule`, Content: `Exams begin next Monday.` |
| **Test Steps** | 1. Tap **Post Announcement**.<br>2. Fill in Title and Content.<br>3. Tap **Post**.<br>4. Log in as Administrator to verify delete permission. |
| **Expected Result** | Announcement is successfully broadcast to Firestore and displayed on feed. Administrator can delete any announcement; Lecturers can delete non-admin announcements. |
| **Post-conditions** | Announcement saved to Firestore and local Room DB cache. |
| **Status** | |

| Field | Description |
| --- | --- |
| **Test Case ID** | NOTIF_PUSH_001 |
| **Description** | Verify that posting a new announcement triggers a real-time system push notification popup. |
| **Test Type** | Positive |
| **Priority** | Medium |
| **Prerequisites** | App is installed and notification permission is granted. |
| **Test Data** | None |
| **Test Steps** | 1. Post a new announcement from an admin/lecturer account.<br>2. Observe device notification tray/popup on active user device. |
| **Expected Result** | Android system push notification popup appears instantly with announcement title and text preview. |
| **Post-conditions** | Notification displayed in notification shade. |
| **Status** | |

---

### 5. Lost & Found Board & Search / Resolution

| Field | Description |
| --- | --- |
| **Test Case ID** | LF_POST_001 |
| **Description** | Verify that users can post lost/found items with search filtering and photo capture/gallery. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | User is logged in. |
| **Test Data** | Title: `Water Bottle`, Location: `Great Hall` |
| **Test Steps** | 1. Navigate to Lost & Found screen.<br>2. Use search bar to filter items.<br>3. Tap Floating Action Button (`+`).<br>4. Fill in details and attach photo, then tap **Post**. |
| **Expected Result** | Item appears instantly in filtered search feed with clear photo display and full-screen zoom capability. |
| **Post-conditions** | Item stored in Firestore and local Room DB. |
| **Status** | |

| Field | Description |
| --- | --- |
| **Test Case ID** | LF_RESOLVE_002 |
| **Description** | Verify that only the original poster can see the "Mark as Resolved & Archive / Hide" button and hide items. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | User has posted an item. |
| **Test Data** | None |
| **Test Steps** | 1. Log in as the user who posted the item -> Verify **"Mark as Resolved & Archive / Hide"** button is visible.<br>2. Log in as another user -> Verify resolution button is **not** visible.<br>3. Click Mark as Resolved as original poster. |
| **Expected Result** | Item is archived/hidden (`isHidden = true`) and immediately removed from the active public lost & found feed without permanent deletion. |
| **Post-conditions** | Item status updated to hidden in database. |
| **Status** | |

---

### 6. Campus Services & Web Links

| Field | Description |
| --- | --- |
| **Test Case ID** | CAMPUS_SRV_001 |
| **Description** | Verify role-based services filtering and instant Custom Tabs web loading. |
| **Test Type** | Positive |
| **Priority** | Medium |
| **Prerequisites** | User is logged in with specific role (Student/Lecturer/Admin). |
| **Test Data** | None |
| **Test Steps** | 1. Navigate to Services screen.<br>2. Filter by category or search service.<br>3. Tap on a UNIMA portal service link. |
| **Expected Result** | Appropriate role-specific services are displayed, and tapping a service opens it instantly using AndroidX Custom Tabs. |
| **Post-conditions** | Browser custom tab opened. |
| **Status** | |

---

### 7. Campus Information & Emergency Contacts

| Field | Description |
| --- | --- |
| **Test Case ID** | CAMPUS_INFO_001 |
| **Description** | Verify that campus emergency contacts, medical center, registrar, and library details are displayed correctly. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | User is logged in and navigates to Campus Info screen. |
| **Test Data** | None |
| **Test Steps** | 1. Navigate to Campus Info tab.<br>2. Review emergency hotlines and office details.<br>3. Tap the **Open Interactive UNIMA Campus Map** button. |
| **Expected Result** | Campus information list is displayed accurately, and tapping map navigation opens the campus map screen. |
| **Post-conditions** | Campus map screen rendered. |
| **Status** | |

| Field | Description |
| --- | --- |
| **Test Case ID** | CAMPUS_INFO_002 |
| **Description** | Verify direct dialing, email app launching, and website viewing from campus contact cards. |
| **Test Type** | Positive |
| **Priority** | Medium |
| **Prerequisites** | User is on Campus Info screen. |
| **Test Data** | Phone: `+265991894726`, Email: `security@unima.ac.mw` |
| **Test Steps** | 1. Tap **Call** button on Security Emergency card.<br>2. Tap **Email** button.<br>3. Tap **Website** button. |
| **Expected Result** | Phone dialer opens with number pre-filled, email client opens with address, and website launches via Custom Tabs. |
| **Post-conditions** | Respective external intent launched. |
| **Status** | |

---

### 8. User Profile & Avatar Customization

| Field | Description |
| --- | --- |
| **Test Case ID** | PROF_UPDATE_001 |
| **Description** | Verify that users can update their profile details and change their profile picture via camera/gallery. |
| **Test Type** | Positive |
| **Priority** | Medium |
| **Prerequisites** | User is on Profile Screen. |
| **Test Data** | Name: `Dr. Alex Johnson`, Department: `Computer Science` |
| **Test Steps** | 1. Tap **Edit** icon.<br>2. Tap profile avatar to choose photo from Camera or Gallery.<br>3. Edit name or department.<br>4. Tap **Save Changes**. |
| **Expected Result** | Profile picture and details update instantly, and changes sync to both local Room DB and Cloud Firestore. |
| **Post-conditions** | Updated profile saved in Room DB and Firestore. |
| **Status** | |

---

### 9. Admin Feedback Inbox

| Field | Description |
| --- | --- |
| **Test Case ID** | ADMIN_FEEDBACK_001 |
| **Description** | Verify that Administrators can view user feedback inbox while students/lecturers can send feedback. |
| **Test Type** | Positive |
| **Priority** | Medium |
| **Prerequisites** | Logged in as Admin vs Student. |
| **Test Data** | Feedback message: `System looks great!` |
| **Test Steps** | 1. Log in as Student/Lecturer -> Verify **"Send Feedback to Admin"** button is visible.<br>2. Log in as Administrator -> Verify **"View Feedback from Users"** button is visible and displays user feedback list. |
| **Expected Result** | Role-appropriate feedback action is displayed correctly on Profile screen. |
| **Post-conditions** | Feedback successfully submitted or viewed. |
| **Status** | |

---

## Whole-App System Test (End-to-End E2E Test)

| Field | Description |
| --- | --- |
| **Test Case ID** | E2E_JOURNEY_001 |
| **Description** | Verify that the entire application functions seamlessly across all integrated modules as a cohesive system. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | Fresh app installation / clean state. |
| **Test Data** | Staff Access Code: `UNIMA-STAFF-2025` |
| **Test Steps** | 1. **Registration & Auth:** Open app, register a new Lecturer account using staff access code. Verify login.<br>2. **Profile & Avatar:** Navigate to Profile, edit details, and upload a profile picture from gallery.<br>3. **Announcements:** Post a new campus announcement. Verify push notification triggers.<br>4. **Services & Info:** Navigate to Campus Services and Campus Info, search services, test phone dialing and Custom Tabs web loading.<br>5. **Lost & Found:** Switch to Lost & Found, post a lost item with a photo, verify it appears in feed, and test the "Mark as Resolved & Archive / Hide" feature.<br>6. **Logout:** Sign out of the app and verify return to Login screen. |
| **Expected Result** | **The entire application functions seamlessly as a cohesive system.** All modules (Auth, Room DB, Firestore sync, Camera/Gallery, Notifications, Lost & Found, Services, Campus Info, and Profile management) execute successfully without crashes or data inconsistencies. |
| **Post-conditions** | App returns to clean logged-out state. |
| **Status** | |
