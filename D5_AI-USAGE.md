# JITNONGNOONG — D5 AI Usage Log

**Project:** Mobile Food Delivery Platform  
**Course:** ITCS383 Software Construction and Evolution  
**Group:** JITNONGNOOONG  
**Phase:** D5 AI Usage Report

---

## Summary

This report explains how AI tools were used during the project.  
The entries below record:
- the task area,
- the prompt used,
- what the AI helped produce,
- what was accepted or revised,
- and how the output was verified before use.

AI was used as a support tool for implementation, debugging, testing, and documentation.  
Final technical decisions and verification remained the responsibility of the project team.

---

## Entry 1 — Order Status Debugging in Node Backend

**Date:** April 2026  
**AI Tool:** Codex (GPT-5 based coding assistant)  
**Area:** Backend Debugging / Order Flow

**Prompt Used:**
> "after customer place the order to restaurant, but restaurant cannot confirm order for the next ongoing process, but restaurant can cancle order"

**What Was Accepted:**
- Investigation into the real backend being used by the project.
- Root-cause analysis that traced the failure to the MySQL enum definition in the Node backend.
- Schema fix to add `CONFIRMED` into the `orders.status` enum.

**What Was Rejected/Revised:**
- Early assumption that the Java backend was the active runtime.
- Frontend-only fixes that would not solve the actual database constraint problem.

**Verification:**
- Reviewed the Node backend routes and schema.
- Checked the `orders.status` enum definition in SQL.
- Confirmed that `CANCELLED` worked because it existed in the enum while `CONFIRMED` did not.

---

## Entry 2 — Restaurant Menu CRUD Implementation

**Date:** April 2026  
**AI Tool:** Codex (GPT-5 based coding assistant)  
**Area:** Backend + Frontend Integration

**Prompt Used:**
> "now restaurant can't manage order whether add new menu, edit menu, and delete menu"

**What Was Accepted:**
- The diagnosis that the frontend menu-management page already existed but the Node backend only supported `GET` routes.
- Implementation of missing CRUD routes for menu items.
- Additional category create/delete support to match restaurant management flows.

**What Was Rejected/Revised:**
- Any solution that only edited frontend code.
- Leaving category management as read-only.

**Verification:**
- Reviewed `MenuManagement.tsx` and `restaurant.service.ts` to confirm expected endpoints.
- Added matching Express routes and checked syntax with `node --check`.
- Confirmed the backend response shape matched frontend expectations.

---

## Entry 3 — Customer Order Tracking Enhancement

**Date:** April 2026  
**AI Tool:** Codex (GPT-5 based coding assistant)  
**Area:** Frontend Feature Development

**Prompt Used (summary):**
- Add an order details button to customer orders.
- Implement mock driver tracking map behavior with:
  - live map initialization,
  - mock GPS generation,
  - rider movement simulation,
  - route and markers,
  - ETA calculation,
  - arrival transition.

**What Was Accepted:**
- Keeping the order list view and adding a `View Details` action.
- Adding an order detail dialog instead of navigating to a separate page.
- Mock coordinate generation and periodic rider movement updates.
- ETA and remaining-distance display.

**What Was Rejected/Revised:**
- Replacing the list page entirely with a map-first layout.
- Adding real third-party map integration, because the requirement asked for mock tracking.

**Verification:**
- Updated and ran `OrderTracking.test.tsx`.
- Manually reviewed UI logic for status, ETA, and detail modal behavior.
- Confirmed the feature matched the requested mock-tracking requirements.

---

## Entry 4 — Tracking Map Theme Refinement

**Date:** April 2026  
**AI Tool:** Codex (GPT-5 based coding assistant)  
**Area:** Frontend UI/UX Consistency

**Prompt Used:**
> "Update the mock live tracking map to match the overall theme of the website. Currently, the map is displayed in a dark theme, but the website uses a light theme."

**What Was Accepted:**
- Converting the map card from dark to light theme.
- Updating route, marker, and coordinate-card styling to fit the rest of the UI.
- Keeping all tracking behavior while only changing presentation.

**What Was Rejected/Revised:**
- Leaving the map in dark mode because it looked visually inconsistent.
- Overcomplicating the change with animation or structural rewrites.

**Verification:**
- Manual visual inspection against the surrounding customer order page.
- Confirmed no functional tracking logic changed.

---

## Entry 5 — Restaurant Rating System

**Date:** April 2026  
**AI Tool:** Codex (GPT-5 based coding assistant)  
**Area:** Full-Stack Feature Development

**Prompt Used:**
> "Implement restaurant rating system on both side. for customer add star rating right after order was delivered for restaurant side just display the avg star rating."

**What Was Accepted:**
- Full-stack implementation plan:
  - backend review storage,
  - review submission API,
  - aggregate rating recalculation,
  - customer rating UI,
  - restaurant average rating display.
- Review sorting on restaurant detail page (`Most Recent`, `Highest Rated`, `Lowest Rated`).
- Displaying average rating and total reviews on customer restaurant cards and detail pages.
- Displaying average rating on the restaurant dashboard.

**What Was Rejected/Revised:**
- Implementing the full owner review-management dashboard at this stage.
- Rating submission before delivery completion.

**Verification:**
- Added review schema and Node backend routes.
- Ran focused frontend tests:
  - `OrderTracking.test.tsx`
  - `RestaurantDetail.test.tsx`
  - `RestaurantDashboard.test.tsx`
- Confirmed aggregate rating fields update after review creation.

---

## Entry 6 — Rating Submission Failure Fix

**Date:** April 2026  
**AI Tool:** Codex (GPT-5 based coding assistant)  
**Area:** Bug Fixing / Frontend Logic

**Prompt Used:**
> "cannot submit the rating fix it"

**What Was Accepted:**
- The explanation that the mock tracking system could visually mark an order as delivered before the backend order status was truly delivered.
- Restricting the rating button to real backend-completed states only.
- Preventing rating submission based on simulated UI-only delivery state.

**What Was Rejected/Revised:**
- Backend relaxation of the rule to accept ratings for non-delivered orders.
- Keeping the rating button tied to mock status overrides.

**Verification:**
- Reviewed the order-tracking state logic.
- Updated rating eligibility checks to use persisted backend status.
- Re-ran focused customer order tracking tests.

---

## Entry 7 — Rider Contact Customer Support

**Date:** April 2026  
**AI Tool:** Codex (GPT-5 based coding assistant)  
**Area:** Frontend Feature Enhancement / Rider Flow

**Prompt Used:**
> "In the rider delivery page, please add a contact customer function and show the customer details more clearly, because the rider needs that information while delivering and the current page is still not helpful enough."

**What Was Accepted:**
- Adding a `Contact Customer` action in the rider delivery screen.
- Displaying customer contact details in a dialog with safe fallback text when phone data is missing.
- Keeping the Google Maps shortcuts for pickup and delivery navigation in the same workflow.
- Support in the order service for enriching order data with customer contact information when the order payload does not already contain it.

**What Was Rejected/Revised:**
- A fake direct-call integration that would imply the web app can place phone calls itself.
- Depending only on hardcoded customer text in the rider screen instead of using backend-backed order/customer data when available.

**Verification:**
- Reviewed the rider delivery page behavior and order service mapping.
- Updated `RiderDelivery.test.tsx` to cover the contact dialog and delivery flow behavior.
- Confirmed fallback text still appears when customer phone data is unavailable.

---

## Entry 8 — Customer Registration Flow Repair

**Date:** April 2026  
**AI Tool:** Codex (GPT-5 based coding assistant)  
**Area:** Frontend Authentication / API Integration

**Prompt Used:**
> "The customer registration page already exists, but it still does not actually work with the current system, so please make the register customer flow work properly with the backend and handle the main validation and error cases too."

**What Was Accepted:**
- Wiring the registration form to the real `authService.register(...)` API call.
- Trimming first name, last name, email, mobile number, and address before submission.
- Mapping the form into the backend request shape with `role: CUSTOMER`.
- Clear user feedback for success, validation errors, API-provided errors, and unknown failures.

**What Was Rejected/Revised:**
- Sending payment-card fields to the backend registration endpoint when the current API does not support storing them there.
- Keeping registration as a UI-only form without backend submission.

**Verification:**
- Added and reviewed `CustomerRegistration.test.tsx`.
- Confirmed the submitted payload includes the normalized customer fields expected by the API.
- Verified redirect-to-login and error-toast behavior across success and failure scenarios.

---

## Entry 9 — Coverage Test Expansion

**Date:** April 2026  
**AI Tool:** Codex (GPT-5 based coding assistant)  
**Area:** Test Coverage / Preventive Maintenance

**Prompt Used:**
> "Please add more coverage tests for the new features and the important changed modules, because I want better quality evidence and I do not want to submit the project with weak test support."

**What Was Accepted:**
- Writing focused tests around the newly changed feature areas rather than only smoke-testing pages manually.
- Extending frontend coverage for customer registration, rider delivery, restaurant dashboard, restaurant detail, and order tracking flows.
- Keeping coverage work aligned with the preventive maintenance goals recorded in the change requests.
- Using coverage reporting as part of the quality-improvement story for the final deliverables.

**What Was Rejected/Revised:**
- Chasing artificial coverage through trivial assertions that do not protect behavior.
- Rewriting unrelated old modules just to increase the percentage.

**Verification:**
- Used `npm test` and `npm run test:coverage` in the touched packages where relevant.
- Cross-checked the improvement narrative with `D2_CODE_QUALITY.md`, including the reported 91.6% new-code coverage.
- Reviewed that new tests matched actual user-facing flows instead of implementation-only details.

---

## Entry 10 — Application Version / Mobile App Direction

**Date:** April 2026  
**AI Tool:** Codex (GPT-5 based coding assistant)  
**Area:** Adaptive Maintenance / Mobile Application

**Prompt Used:**
> "I also need an application version of this web system, so please support the mobile-app direction too and keep the work aligned with making an app version using Flutter."

**What Was Accepted:**
- Keeping the mobile application as a distinct project track rather than forcing it into the web frontend codebase.
- Aligning the AI log with the formal change request for Flutter/Dart mobile development.
- Referencing the separate mobile-app deliverable and the project documentation that already identifies mobile support as part of the system direction.

**What Was Rejected/Revised:**
- Pretending the mobile application was implemented inside the React web frontend repository.
- Mixing web-only implementation details with mobile-app claims that belong to the separate app work.

**Verification:**
- Confirmed the repository README points to the separate mobile app project.
- Matched the wording to the handover/design documents that mention a mobile application path.

---

## Entry 11 — Test Support and Maintenance

**Date:** April 2026  
**AI Tool:** Codex (GPT-5 based coding assistant)  
**Area:** Testing

**Prompt Used:**
> "After these feature changes and bug fixes, please update the tests too, extend the missing cases, and keep the mocks aligned with the new service methods and UI flow so the test suite still reflects the real behavior."

**What Was Accepted:**
- New and updated test cases for customer order tracking and restaurant detail/dashboard.
- Mock updates for newly added review service methods.
- Focused test execution on changed files instead of running unrelated suites.

**What Was Rejected/Revised:**
- Brittle test expectations tied to irrelevant implementation details.
- Broad test changes to unrelated modules.

**Verification:**
- Ran focused Vitest commands for touched frontend files.
- Reviewed passing results and identified residual warnings separately from real failures.

---

## Overall AI Usage Principles

1. AI was used as a support tool, not as the final authority.
2. All important outputs were reviewed before adoption.
3. Incorrect assumptions were revised when local code or runtime behavior showed otherwise.
4. Verification was required before accepting AI-assisted changes.
5. Prompts and accepted outputs were documented for traceability.

---

## Final Statement

AI tools were used in this project to support implementation, debugging, testing, UI refinement, and documentation.  
The team did not accept AI output blindly; suggestions were checked against the actual codebase, tested where relevant, and revised when they did not match project requirements or runtime behavior.
