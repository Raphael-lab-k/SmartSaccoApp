# Implementation Plan - Low-Risk Auto-Approval System

This plan implements an **Auto-Approval** engine for low-risk loans, streamlining the credit process for reliable members while maintaining safety guardrails.

## Proposed Changes

### Data & Logic Layer

#### [MODIFY] [SaccoRepository.kt](file:///C:/Users/NinyeTinyefuza/AndroidStudioProjects/SmartSaccoApp/app/src/main/java/com/example/smartsaccoapp/data/SaccoRepository.kt) & [SaccoRepository.java](file:///C:/Users/NinyeTinyefuza/AndroidStudioProjects/SmartSaccoApp/app/src/main/java/com/example/smartsaccoapp/data/SaccoRepository.java)
- Update `insertLoan` to accept a `LoanCallback`.
- Add **Auto-Approval Logic** inside `insertLoan`:
    - Conditions for Auto-Approval:
        - `riskLevel == "LOW"`
        - `amount <= 1,000,000` (UGX)
        - Member `kycStatus == "VERIFIED"`
    - If conditions are met:
        - Set `loan.status = "APPROVED"`.
        - Perform immediate disbursement (add amount to account balance).
        - Record `LOAN_DISBURSEMENT` transaction and `AUTO_APPROVE` audit log.
    - If conditions are NOT met:
        - Set `loan.status = "PENDING"`.
- Sync the final loan state to Firebase.

### UI Layer - Loan Application

#### [MODIFY] [LoanApplicationFragment.java](file:///C:/Users/NinyeTinyefuza/AndroidStudioProjects/SmartSaccoApp/app/src/main/java/com/example/smartsaccoapp/ui/loans/LoanApplicationFragment.java)
- Update the submit button logic to handle the new repository callback.
- Display a specific "Auto-Approved" success message (e.g., using a green Snackbar or Toast) if the loan was instantly approved.
- Redirect to the Dashboard to show the updated balance if auto-approved.

## Verification Plan

### Automated Tests
- Test the auto-approval branch in `insertLoan`:
    - Verify balance increases immediately for a LOW risk loan under 1M.
    - Verify a 2M loan still goes to PENDING even if risk is LOW.
    - Verify an unverified member (PENDING KYC) never gets auto-approved.

### Manual Verification
1.  Log in as a verified member with a high trust score.
2.  Apply for a small loan (e.g., UGX 500,000).
3.  Confirm the "Auto-Approved" notification and check the dashboard for the new balance.
4.  Apply for a large loan (e.g., UGX 5,000,000).
5.  Confirm it stays as "PENDING" and requires admin intervention.
