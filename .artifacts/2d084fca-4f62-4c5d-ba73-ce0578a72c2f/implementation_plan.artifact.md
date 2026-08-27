# Implementation Plan - Fix Startup Crash & UI Overhaul

This plan addresses the crash occurring on the first run when interacting with the scan area and provides a major aesthetic upgrade to the diagnosis interface.

## User Review Required

> [!IMPORTANT]
> **Crash Root Cause**: The crash is likely due to the application attempting to access the `cameraLauncher` or `galleryLauncher` before the necessary permissions have been fully granted or initialized on the first run.
> **UI Transformation**: I will move from a standard linear layout to a more immersive, modern "Card-based" design with better visual hierarchy.

## Proposed Changes

### [Fix Crash on First Run]

#### [MODIFY] [DiagnosisActivity.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/DiagnosisActivity.java)
- Ensure permissions are handled *before* any launcher is triggered.
- Add safety checks to the `ActivityResultLauncher` logic to handle edge cases during initialization.
- Initialize `imageUri` more robustly to avoid `NullPointerException`.

---

### [Diagnosis UI Redesign]

#### [MODIFY] [activity_main.xml](file:///D:/Androi_DATN/app/src/main/res/layout/activity_main.xml)
- **Top Header**: Use a transparent or modern Toolbar that blends with the content.
- **Scanning Area**:
    - Use a larger, more prominent image frame.
    - Add a "Glassmorphism" effect overlay for scanning status.
    - Improve the scanning line animation to look more "High-Tech".
- **Interaction Buttons**:
    - Replace standard buttons with a bottom action bar or floating action buttons (FABs).
    - Use meaningful icons and cleaner typography.
- **Results Dashboard**:
    - Use larger, bold fonts for disease names.
    - Redesign the confidence meter as a more elegant circular indicator.
    - Add "Card" grouping for symptoms and treatment to improve readability.

#### [NEW] Custom Drawables
- Create a `bg_glass_overlay.xml` for the glass effect.
- Update `scanning_line.xml` for a smoother glow effect.

## Verification Plan

### Automated Tests
- Build and run the app to ensure no compilation errors.

### Manual Verification
1.  **First Run Test**: Clear App Data -> Launch App -> Click Scan Area -> Verify no crash and permission prompt appears correctly.
2.  **UI Aesthetic Check**: Perform a diagnosis and verify the new layout looks modern and aligned with current design trends.
3.  **Advice Toggle**: Verify the "Xem tư vấn" button still works smoothly with the new design.
