# Implementation Plan - AI Performance Optimization (v2)

This plan addresses the slowness in diagnosis results by making the email alerting system asynchronous and optimizing the Gemini AI interaction.

## User Review Required

> [!IMPORTANT]
> **Email Bottleneck**: Currently, the `/predict` endpoint waits for the email to be sent via SMTP before returning the result to the app. SMTP handshake and sending can take 2-5 seconds, causing the "scanning" to feel very slow.
> **Background Tasks**: I will move the email sending to a `BackgroundTask` so the app gets the diagnosis result immediately (< 1s).

## Proposed Changes

### [Backend - AI Optimization]

#### [MODIFY] [main.py](file:///D:/Plant_Disease_Pipeline/main.py)
- **FastAPI BackgroundTasks**: Use `BackgroundTasks` to send the emergency email *after* the HTTP response has been sent to the Android app.
- **Remove Blocking Wait**: Ensure the `predict` function does not await the email sending process.
- **Gemini Parameters**: Adjust `generation_config` for Gemini 1.5 Flash to favor speed (lower top_p/top_k if necessary, or just rely on the model's default speed).

---

### [Android - UX Improvements]

#### [MODIFY] [DiagnosisActivity.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/DiagnosisActivity.java)
- **UI State**: Ensure the "Scanning" animation stops immediately when the first response arrives, and the result dashboard is shown instantly.
- **Advice Placeholder**: Update the placeholder text to "Đang kết nối với chuyên gia AI..." while `fetchExpertAdvice` is running.

## Verification Plan

### Automated Tests
- Restart AI backend and perform a diagnosis.
- Observe backend logs to see if "Email sent" appears *after* the app has already received the result.

### Manual Verification
1.  **Diagnosis Speed**: Perform a scan. The disease name and bounding box should appear in less than 1.5 seconds.
2.  **AI Advice**: The advice section should update independently 2-4 seconds later without blocking the main results.
3.  **Email**: Verify the email is still received correctly by the user.
