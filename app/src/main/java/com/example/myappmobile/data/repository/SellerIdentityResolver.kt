package com.example.myappmobile.data.repository

import com.example.myappmobile.data.remote.BackendUrlResolver
import com.example.myappmobile.data.remote.asObjectOrNull
import com.example.myappmobile.data.remote.asStringOrNull
import com.example.myappmobile.data.remote.objectAt
import com.example.myappmobile.data.remote.string
import com.example.myappmobile.domain.model.SellerApprovalStatus
import com.google.gson.JsonElement
import com.google.gson.JsonObject

data class SellerIdentity(
    val storeName: String = "",
    val personalName: String = "",
    val profileImageUrl: String = "",
    val bannerImageUrl: String = "",
    val approvalStatus: SellerApprovalStatus = SellerApprovalStatus.UNKNOWN,
)

internal fun resolveSellerIdentity(source: JsonObject?): SellerIdentity {
    if (source == null) return SellerIdentity()

    val store = source.objectAt("store", "shop")
    val seller = source.objectAt("seller", "user", "owner")
    val profile = source.objectAt("profile")
    val verification = source.objectAt("verification", "seller_verification", "verification_request", "application", "request")

    val storeName = firstString(
        source.string("store_name", "shop_name"),
        store?.string("name", "store_name", "shop_name"),
        seller?.string("store_name", "shop_name"),
    )

    val personalName = firstString(
        source.string("owner_name", "seller_name", "full_name"),
        seller?.string("name", "full_name", "owner_name", "seller_name"),
        profile?.string("name", "full_name"),
        source.objectAt("user", "seller")?.string("name", "full_name", "owner_name", "seller_name"),
    )

    val profileImageUrl = normalizeImageUrl(
        firstString(
            source.string("logo", "logo_url", "store_image", "avatar", "avatar_url", "profile_photo_url", "profile_picture", "profile_picture_url", "image"),
            store?.string("logo", "logo_url", "store_image", "image"),
            seller?.string("avatar", "avatar_url", "profile_photo_url", "profile_picture", "profile_picture_url", "image"),
            profile?.string("avatar", "avatar_url", "profile_photo_url", "profile_picture", "profile_picture_url", "image"),
        ),
    )

    val bannerImageUrl = normalizeImageUrl(
        firstString(
            source.string("banner", "banner_url"),
            store?.string("banner", "banner_url", "cover", "cover_url"),
            profile?.string("banner", "banner_url"),
        ),
    )

    val approvalStatus = resolveSellerApprovalStatusOrNull(
        primary = JsonObject().apply {
            source.entrySet().forEach { (key, value) -> add(key, value) }
            store?.entrySet()?.forEach { (key, value) -> if (!has(key)) add(key, value) }
            seller?.entrySet()?.forEach { (key, value) -> if (!has(key)) add(key, value) }
            verification?.entrySet()?.forEach { (key, value) -> if (!has(key)) add(key, value) }
            store?.let { add("store", it) }
            seller?.let { add("seller", it) }
            verification?.let { add("verification", it) }
        },
    ) ?: SellerApprovalStatus.UNKNOWN

    return SellerIdentity(
        storeName = storeName,
        personalName = personalName,
        profileImageUrl = profileImageUrl,
        bannerImageUrl = bannerImageUrl,
        approvalStatus = approvalStatus,
    )
}

internal fun resolveSellerIdentity(source: JsonElement?): SellerIdentity = resolveSellerIdentity(source?.asObjectOrNull())

internal fun normalizeImageUrl(raw: String?): String = BackendUrlResolver.normalizeImageUrl(raw)

private fun firstString(vararg candidates: String?): String = candidates.firstOrNull { !it.isNullOrBlank() }.orEmpty()
