---
description: ui
---

# Frontend Blueprint: Freelance Marketplace (Next.js + Tailwind)

## 1. Project Overview
We are building the client-side application for a Tanzanian Freelance Marketplace.
* **Backend:** Spring Boot 3.2 (Already designed).
* **Design Philosophy:**
    * **Public Pages:** Minimalist, clean, and fast (Inspired by **OFM Jobs**). High contrast, clear typography, list-based layout.
    * **Private Dashboards:** Functional, data-rich, and secure (Inspired by **Upwork/Guru**). Focus on managing proposals, chats, and escrow payments.
* **Target Audience:** Tanzanian users (Mobile-first approach is mandatory).

---

## 2. Technical Stack (Strict Requirements)
* **Framework:** Next.js 14+ (App Router).
* **Language:** TypeScript.
* **Styling:** Tailwind CSS.
* **UI Component Library:** `shadcn/ui` (Radix UI) for professional, accessible components (Buttons, Modals, Inputs, Toasts).
* **Icons:** `lucide-react`.
* **State Management:** `Zustand` (for global auth state).
* **Data Fetching:** `TanStack Query` (React Query) v5 (Critical for caching job lists and managing loading states).
* **Form Handling:** `react-hook-form` + `zod` (Validation).
* **HTTP Client:** `Axios` (with interceptors for JWT).

---

## 3. Folder Structure (App Router)

```text
/app
  /(public)           # Marketing & Job Feed (OFM Style)
    /page.tsx         # Hero + Featured Jobs
    /jobs/            # The main Job Board
      page.tsx        # Search + Filter + List
      [id]/page.tsx   # Job Details + "Apply" Modal
    /auth/            # Login/Register (Role Selection)
  
  /(dashboard)        # Protected Area (Upwork Style)
    /client/          # Client Specific Views
      /post-job       # Multi-step form
      /my-jobs        # List of posted jobs
      /proposals/[jobId] # Review applicants & Hire
    /freelancer/      # Freelancer Specific Views
      /my-contracts   # Active Escrow contracts
      /earnings       # Wallet & Withdraw
    /chat/            # Real-time Chat Layout
      page.tsx

  /components
    /ui               # Shadcn components (Button, Card, etc.)
    /shared           # Navbar, Footer, JobCard, StatusBadge
    /forms            # ApplyForm, PostJobForm
  /lib
    axios.ts          # API configuration
    utils.ts          # Currency formatter (TZS), Date formatters
  /hooks              # Custom hooks (useAuth, useSocket)
  /store              # Zustand stores