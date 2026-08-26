# Implementation Plan - AI Speed Optimization & Reliability

This plan addresses the slowness and 503 errors in the AI backend by using the correct model name and adding robust retry logic. Finally, all pending changes will be committed to Git.

## User Review Required

> [!IMPORTANT]
> **Model Version**: The current code uses `gemini-3.5-flash`, which does not exist. I will update this to `gemini-1.5-flash`, which is the fastest stable version available.
> **Git Commit**: I will commit all changes in the `D:/Androi_DATN` directory, including the recently added Admin Panel and Notification fixes.

## Proposed Changes

### [AI Backend Optimization]

#### [MODIFY] [main.py](file:///D:/Plant_Disease_Pipeline/main.py)
- **Model Update**: Change all instances of `gemini-3.5-flash` to `gemini-1.5-flash`.
- **Retry Logic**: Wrap Gemini AI calls in a retry loop (3 attempts) with exponential backoff to handle 503 "High Demand" errors.
- **Safety**: Add a 15-second timeout to prevent the application from hanging indefinitely on slow responses.

---

### [Version Control]

#### [EXECUTE] Git Commit & Push
- `git add .` to stage all changes (Admin Dashboard, Library CRUD, Tip Management, Notification Fixes).
- `git commit -m "Enhance: Admin Panel, Notification deep-linking, Avatar Sync, and AI Speed Optimization"`

## Verification Plan

### Automated Tests
- Restart the Python backend and monitor logs for successful model initialization.

### Manual Verification
1.  **Speed Check**: Ask a question in the AI Chat and verify the response returns within a few seconds.
2.  **Error Handling**: If a 503 occurs, verify the backend retries automatically (logs will show "Retrying...").
3.  **VCS**: Run `git status` to ensure a clean working directory.
