package com.winga.domain.enums;

/**
 * OFM-style roles:
 * SUPER_ADMIN = Platform owner (full system).
 * ADMIN = Same as SUPER_ADMIN for backward compatibility.
 * EMPLOYER_ADMIN = Recruiter/Agency (dashboard per company).
 * MODERATOR = Jobs + Applications only (approve/reject jobs, flag applicants).
 * CLIENT = Employer (post jobs, manage applications).
 * FREELANCER = Job seeker (apply, track applications).
 */
public enum Role {
    SUPER_ADMIN,
    ADMIN,
    EMPLOYER_ADMIN,
    MODERATOR,
    CLIENT,      // Employer / Recruiter
    FREELANCER   // Job seeker
}
