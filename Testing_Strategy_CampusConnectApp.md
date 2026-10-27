# Comprehensive Testing Strategy for CampusConnectApp

This document outlines the recommended test suite for **CampusConnectApp**, categorized by testing layers, specific components, and the engineering rationale ("Why?") for each.

---

## 1. Unit Tests (`src/test`) — Business Logic & Validators

### A. Validation & Authentication Rules (`ValidationTest.kt`)
* **What to test:**
  * **Email Domain Validation:** Verify that emails strictly belong to `@unima.ac.mw` and match the user/registration number prefix.
  * **Password Strength:** Verify that passwords require minimum 8 characters, uppercase letters, lowercase letters, numbers, and special symbols.
  * **Registration Number / Staff ID:** Verify valid alphanumeric/hyphen/slash formatting (e.g., `CS-2023-4091`, `EMP-LECT-882`).
* **Why:** Ensures security compliance and prevents malformed data or invalid institutional accounts from being created.

### B. Role-Based Permissions (`AnnouncementPermissionTest.kt`)
* **What to test:**
  * **Posting Permission (`canPost`):** Verify students return `false`, while Lecturers and Administrators return `true`.
  * **Deletion Permission (`canDelete`):** Verify Students cannot delete anything; Lecturers can delete their own/peer announcements but not Administrator announcements; Administrators can delete any announcement.
* **Why:** Enforces strict role-based access control (RBAC) and prevents privilege escalation or unauthorized content removal.

### C. Lost & Found Ownership & Hiding Logic (`LostFoundHideTest.kt`)
* **What to test:**
  * **Ownership Check (`canHide`):** Verify that the "Mark as Resolved" / hide button is active **only** when `currentUserId == item.postedByUid`.
  * **Active Feed Filtering:** Verify that items marked as hidden (`isHidden = true`) are successfully excluded from the active list feed.
* **Why:** Ensures users can only manage their own posts, and resolved/archived items correctly disappear from public view.

---

## 2. Database Tests (`src/test` or Room In-Memory Tests)

### A. Room DAO & Entity Mapping Tests (`RoomDaoTest.kt`)
* **What to test:**
  * **User DAO (`UserDaoTest`):** Test inserting a user, querying login credentials (`login()`), updating profile details, and clearing all users.
  * **Lost & Found DAO (`LostFoundDaoTest`):** Test inserting lost items, caching, and querying active unhidden items.
  * **Type Converters (`ConvertersTest`):** Test serialization and deserialization of `UserRole` and `ItemType` enums.
* **Why:** Guarantees offline-first reliability, ensuring local SQLite database transactions and Room queries execute without throwing SQLite or mapping exceptions.

---

## 3. UI & Composable Tests (`src/androidTest` / Robolectric)

### A. Login & Registration Screen Tests
* **What to test:**
  * Tab switching between **Sign In** and **Register**.
  * Dynamic visibility of the **Staff Access Code** field when selecting Lecturer or Admin roles.
  * Appearance of error directive text when typing invalid email formats or weak passwords.
* **Why:** Validates user experience and ensures interactive UI state transitions work smoothly across devices.

### B. Profile Screen & Avatar Upload Tests
* **What to test:**
  * Toggling between view mode and edit mode (`isEditing`).
  * Profile picture picker dialog invocation and base64 compression handling.
* **Why:** Verifies user profile customization and personal data editing flows.
