import { getAuthHeaders } from "../hooks/useAuth";

const API_BASE = import.meta.env.VITE_API_URL || "http://localhost:8080";

export type ApiResponse<T> = {
  success: boolean;
  message?: string;
  data?: T;
};

export async function api<T>(path: string, options?: RequestInit): Promise<ApiResponse<T>> {
  const url = path.startsWith("http") ? path : `${API_BASE}/api${path.startsWith("/") ? path : `/${path}`}`;
  const res = await fetch(url, {
    ...options,
    headers: { "Content-Type": "application/json", ...getAuthHeaders(), ...options?.headers },
  });
  const json = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(json.message || res.statusText);
  return json as ApiResponse<T>;
}

export async function getDashboardOverview() {
  return api<{
    activeJobs: number;
    applicationsToday: number;
    applicationsThisMonth: number;
    hiresMade: number;
    responseRatePercent: number;
    revenue: number;
    pendingModerationCount: number;
    applicationsOverTime: { date: string; count: number }[];
    topCategories: { categoryName: string; count: number }[];
  }>("/admin/dashboard/overview");
}

export async function getStats() {
  return api<{
    totalUsers: number;
    totalClients: number;
    totalFreelancers: number;
    openJobs: number;
    totalJobs: number;
    activeContracts: number;
    completedContracts: number;
    disputedContracts: number;
    totalPlatformRevenue: number;
  }>("/admin/stats");
}

export type UserRow = {
  id: number;
  fullName?: string;
  email?: string;
  phoneNumber?: string;
  role?: string;
  isVerified?: boolean;
  isActive?: boolean;
  createdAt?: string;
};

export async function getUsers(page = 0, size = 20) {
  return api<{ content: UserRow[]; totalElements: number }>(`/admin/users?page=${page}&size=${size}`);
}

export async function createUser(body: { email: string; fullName: string; password: string; role: string; phoneNumber?: string }) {
  return api<UserRow>("/admin/users", { method: "POST", body: JSON.stringify(body) });
}

export async function updateUser(id: number, body: Partial<{ fullName: string; email: string; password: string; role: string; phoneNumber: string; isVerified: boolean; isActive: boolean }>) {
  return api<UserRow>(`/admin/users/${id}`, { method: "PUT", body: JSON.stringify(body) });
}

export async function deleteUser(id: number) {
  return api(`/admin/users/${id}`, { method: "DELETE" });
}

export async function getModerationJobs(page = 0, size = 20) {
  return api<{ content: unknown[]; totalElements: number }>(`/admin/jobs/moderation?page=${page}&size=${size}`);
}

export async function moderateJob(id: number, status: string, rejectReason?: string) {
  return api(`/admin/jobs/${id}/moderate`, {
    method: "PATCH",
    body: JSON.stringify({ status, rejectReason: rejectReason || null }),
  });
}

export async function getCategories() {
  return api<{ id: number; name: string; slug: string; sortOrder: number }[]>("/admin/categories");
}

export async function createCategory(name: string, slug: string, sortOrder?: number) {
  return api("/admin/categories", {
    method: "POST",
    body: JSON.stringify({ name, slug, sortOrder: sortOrder ?? 0 }),
  });
}

export async function updateCategory(id: number, name: string, slug: string, sortOrder?: number) {
  return api(`/admin/categories/${id}`, {
    method: "PUT",
    body: JSON.stringify({ name, slug, sortOrder: sortOrder ?? 0 }),
  });
}

export async function deleteCategory(id: number) {
  return api(`/admin/categories/${id}`, { method: "DELETE" });
}

export async function getDisputes(page = 0, size = 20) {
  return api<{ content: unknown[] }>(`/admin/disputes?page=${page}&size=${size}`);
}

// ─── Bids (Proposals) moderation ─────────────────────────────────────────────
export type ProposalRow = {
  id: number;
  jobId: number;
  jobTitle: string;
  bidAmount: number;
  coverLetter?: string;
  status: string;
  moderationStatus: string;
  priceBreakdown?: { providerFee: number; platformCommission: number; totalToClient: number };
  createdAt?: string;
};

export async function getProposalsForModeration(page = 0, size = 20) {
  return api<{ content: ProposalRow[]; totalElements: number }>(
    `/admin/proposals?moderationStatus=PENDING_APPROVAL&page=${page}&size=${size}`
  );
}

export async function moderateProposal(id: number, moderationStatus: "APPROVED" | "REJECTED") {
  return api<ProposalRow>(`/admin/proposals/${id}/moderate?moderationStatus=${moderationStatus}`, {
    method: "PATCH",
  });
}
