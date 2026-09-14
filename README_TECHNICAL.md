# SmartSaccoApp Technical Documentation

This document provides a deep dive into the technical architecture, data structures, and innovative algorithms implemented in the SmartSaccoApp.

## 1. Architectural Overview
The application follows the **MVVM (Model-View-ViewModel)** architectural pattern, ensuring a clean separation of concerns.

*   **View Layer:** Activity/Fragment classes using **ViewBinding** for type-safe UI interaction.
*   **ViewModel Layer:** Lifecycle-aware components that manage UI-related data and communicate with the Repository.
*   **Repository Layer:** A central `SaccoRepository` that acts as the "Single Source of Truth," coordinating data flow between the local Room database and the Firebase cloud backend.
*   **Data Layer:** Persistent storage using **Room (SQLite)** for offline-first capabilities and **Firebase Firestore** for real-time cloud synchronization.

## 2. Technology Stack
*   **Language:** Java / Kotlin
*   **Local Database:** Room Persistence Library (v2.6.1)
*   **Cloud Backend:** Firebase (Auth, Firestore, Cloud Messaging)
*   **Background Tasks:** WorkManager (for automated loan penalties)
*   **Security:** Android Biometric API (Fingerprint/Face Unlock)
*   **Reporting:** PdfDocument API (for UGX statement generation)
*   **Networking:** Firebase SDK (Real-time sync)

## 3. Database Schema & Entities
The app uses a versioned SQLite database (Current Version: 6) with the following entities:

| Entity | Purpose | Key Fields |
| :--- | :--- | :--- |
| `Member` | User profiles & RBAC | `email`, `role` (ADMIN/MEMBER), `trustScore`, `kycStatus` |
| `Account` | Financial balances | `balance` (UGX), `accountNumber` |
| `Transaction` | Ledger of activities | `type` (DEPOSIT, WITHDRAWAL, etc.), `amount`, `timestamp` |
| `Loan` | Borrowing records | `amount`, `interestRate`, `repaidAmount`, `status`, `riskLevel` |
| `SavingPot` | Collaborative goals | `targetAmount`, `currentAmount`, `status` (OPEN/LOCKED) |
| `Vouch` | Social credit links | `voucherEmail`, `voucheeEmail`, `timestamp` |
| `MarketItem` | P2P marketplace | `price`, `status` (AVAILABLE/ESCROW/SOLD) |
| `AuditLog` | Security & Transparency | `actorEmail`, `action`, `details` |

## 4. Key Innovation Algorithms

### A. Community Trust Score (Social Credit)
The **Trust Score** is a dynamic value (0-1000) that influences loan eligibility.
*   **Base Score:** 500
*   **Incentive:** +50 points for every "Vouch" received from a verified member.
*   **Risk Engine:** `RiskScore = (FinancialRatio * 0.7) + (TrustFactor * 0.3)`. This allows members with lower balances but high community trust to qualify for credit.

### B. Offline SMS Bridge
Designed for low-connectivity environments.
*   **Detection:** Uses `ConnectivityManager` to monitor network state.
*   **Operation:** If offline, the UI highlights "SMS Mode." Actions generate encrypted SMS strings (e.g., `SMARTSACCO SEND 50000 ivan@demo.com`) sent to a designated SACCO gateway.

### C. Automated Loan Lifecycle
Uses `WorkManager` to run a daily `SaccoWorker`:
*   **Penalty Logic:** If `currentTime > loan.dueDate` and `status == APPROVED`, the status is updated to `OVERDUE` and a **5% penalty** is automatically added to the principal.
*   **Auto-Closure:** If `repaidAmount >= (principal + interest)`, status is automatically set to `PAID`.

## 5. Security & Operational Controls
*   **Role-Based Access Control (RBAC):** `MainActivity` dynamically inflates different navigation menus for `ADMIN` (Member Management, Financial Reports, Audit Trail) and `MEMBER` (Savings, Loans, Marketplace).
*   **Biometric Shield:** Sensitive actions (Internal Transfers) require a `BiometricPrompt` before the repository executes the transaction.
*   **Financial Escrow:** Marketplace purchases move funds from the buyer's account into a "locked" state until receipt is confirmed, preventing fraud.

## 6. Implementation Notes
*   **Localization:** All financial strings use `Locale.getDefault()` with `UGX %,.0f` formatting.
*   **Hybrid Sync:** `FirebaseManager` ensures that local Room updates are mirrored to Firestore instantly when online.
*   **Audit Trail:** The `AuditLogDao` captures every significant action (KYC approval, Loan rejection, Large transfers) for non-repudiation.

---
*Document generated on: 2026-09-07*
