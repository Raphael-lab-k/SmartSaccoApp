# Modernize Dashboard Layout

This plan upgrades the Dashboard to a modern, user-friendly "Command Center" layout focusing on clarity and visual appeal.

## User Review Required

> [!NOTE]
> I will be updating the main Dashboard layout to use a more professional neutral background and a "Quick Actions" grid, which is common in modern FinTech apps.
> I will also add a "Show/Hide" feature for the account balance to improve user privacy.

## Proposed Changes

### UI & Resources

#### [MODIFY] [fragment_dashboard.xml](file:///C:/Users/NinyeTinyefuza/AndroidStudioProjects/SmartSaccoApp/app/src/main/res/layout/fragment_dashboard.xml)
- Change screen background to `neutral_gray`.
- Update the **Balance Card** with a privacy toggle (Eye icon).
- Replace the horizontal button list with a **Quick Actions Grid** (2x2) using circular background containers.
- Improve the **Trust Score** card with better icons and alignment.

#### [MODIFY] [item_dashboard.xml](file:///C:/Users/NinyeTinyefuza/AndroidStudioProjects/SmartSaccoApp/app/src/main/res/layout/item_dashboard.xml)
- Update spacing and font sizes for a cleaner look.
- Prepare for dynamic color-coding of amounts (Green/Red).

#### [NEW] Circular Action Drawable
- Create a background drawable for quick action icons.

---

### Logic

#### [MODIFY] [DashboardFragment.java](file:///C:/Users/NinyeTinyefuza/AndroidStudioProjects/SmartSaccoApp/app/src/main/java/com/example/smartsaccoapp/ui/dashboard/DashboardFragment.java)
- Implement state-based "Show/Hide balance" logic.
- Update the `TransactionAdapter` to set transaction icons based on type (Deposit, Withdrawal, etc.).

## Verification Plan

### Manual Verification
- Deploy to emulator.
- Verify the balance "eye" toggle works.
- Check that the Quick Actions navigate to the correct screens.
- Ensure the transaction list looks clean and displays the correct colors/icons.
