package com.example.myappmobile.data.repository

import com.example.myappmobile.data.remote.asBooleanOrNull
import com.example.myappmobile.data.remote.element
import com.example.myappmobile.data.remote.objectAt
import com.example.myappmobile.data.remote.string
import com.example.myappmobile.domain.model.SellerApprovalStatus
import com.google.gson.JsonObject

internal fun resolveSellerApprovalStatusOrNull(
    primary: JsonObject?,
    includeGenericPendingFallback: Boolean = false,
): SellerApprovalStatus? {
    if (primary == null) return null

    val candidates = buildList {
        add(primary)
        primary.objectAt("verification", "seller_verification", "verification_request", "application", "request")
            ?.let(::add)
        primary.objectAt("user", "seller")?.let(::add)
        primary.objectAt("store")?.let(::add)
    }

    val rawStatus = candidates.firstNotNullOfOrNull { candidate ->
        candidate.string(
            "status",
            "verification_status",
            "approval_status",
            "seller_status",
            "is_verified",
            "is_approved",
            "verified",
            "approved",
        )
    }

    if (!rawStatus.isNullOrBlank()) {
        return when (rawStatus.trim().lowercase()) {
            "approved", "verified", "active", "true", "1" -> SellerApprovalStatus.APPROVED
            "pending", "submitted", "under_review", "in_review", "awaiting_review" -> SellerApprovalStatus.PENDING
            "rejected", "declined", "denied" -> SellerApprovalStatus.REJECTED
            "not_verified", "unverified", "not-submitted", "not submitted", "false", "0" -> SellerApprovalStatus.NOT_VERIFIED
            else -> null
        }
    }

    return when {
        candidates.any { it.element("approved", "is_approved", "verified", "is_verified")?.asBooleanOrNull() == true } ->
            SellerApprovalStatus.APPROVED
        candidates.any { it.string("approved_at", "verified_at") != null } ->
            SellerApprovalStatus.APPROVED
        candidates.any { it.string("rejection_reason", "reason", "rejected_reason", "admin_note", "note", "comment", "rejected_at") != null } ->
            SellerApprovalStatus.REJECTED
        candidates.any { it.string("submitted_at", "requested_at") != null } ->
            SellerApprovalStatus.PENDING
        includeGenericPendingFallback && candidates.any { it.element("created_at") != null } ->
            SellerApprovalStatus.PENDING
        else -> null
    }
}

internal fun resolveSellerApprovalStatus(
    primary: JsonObject?,
    includeGenericPendingFallback: Boolean = false,
): SellerApprovalStatus {
    return resolveSellerApprovalStatusOrNull(primary, includeGenericPendingFallback)
        ?: SellerApprovalStatus.NOT_VERIFIED
}
