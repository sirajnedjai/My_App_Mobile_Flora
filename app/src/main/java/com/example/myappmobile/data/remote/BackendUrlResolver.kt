package com.example.myappmobile.data.remote

import com.example.myappmobile.BuildConfig
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

object BackendUrlResolver {
    private const val SUPABASE_PUBLIC_BUCKET_URL =
        "https://navgcisapdcirkjrfigp.supabase.co/storage/v1/object/public/gradshop-media/"

    val apiBaseUrl: String = normalizeApiBaseUrl(BuildConfig.FLORA_API_BASE_URL)

    val apiHttpUrl: HttpUrl = apiBaseUrl.toHttpUrl()

    val originHttpUrl: HttpUrl = apiHttpUrl.newBuilder()
        .encodedPath("/")
        .build()

    val originUrl: String = originHttpUrl.toString().removeSuffix("/")

    private val supabaseBucketHttpUrl: HttpUrl = SUPABASE_PUBLIC_BUCKET_URL.toHttpUrl()

    fun normalizeImageUrl(raw: String?): String {
        return resolveImageUrlOrNull(raw).orEmpty()
    }

    fun resolveImageUrlOrNull(raw: String?): String? {
        val trimmed = raw.orEmpty().trim()
        if (trimmed.isBlank()) return null

        if (isLocalImageUri(trimmed)) return trimmed

        if (trimmed.startsWith("https://", ignoreCase = true) || trimmed.startsWith("http://", ignoreCase = true)) {
            val parsed = runCatching { trimmed.toHttpUrl() }.getOrNull() ?: return null
            return when {
                isSupabasePublicUrl(parsed) -> parsed.newBuilder().scheme("https").build().toString()
                isLegacyBackendStorageUrl(parsed) -> buildSupabaseImageUrl(
                    path = normalizeStorageObjectPath(parsed.encodedPath),
                    encodedQuery = parsed.encodedQuery,
                )
                else -> parsed.newBuilder().scheme("https").build().toString()
            }
        }

        return buildSupabaseImageUrl(normalizeStorageObjectPath(trimmed))
    }

    private fun normalizeApiBaseUrl(rawBaseUrl: String): String {
        val trimmed = rawBaseUrl.trim()
        require(trimmed.isNotBlank()) { "FLORA_API_BASE_URL must not be blank." }

        val parsed = trimmed.toHttpUrl()
        val normalizedPath = parsed.encodedPath
            .trimEnd('/')
            .let { currentPath ->
                when {
                    currentPath.endsWith("/api") -> "$currentPath/"
                    currentPath.isBlank() || currentPath == "/" -> "/api/"
                    else -> "$currentPath/api/"
                }
            }

        return parsed.newBuilder()
            .scheme("https")
            .encodedPath(normalizedPath)
            .build()
            .toString()
    }

    private fun normalizeBackendPath(rawPath: String): String {
        val trimmed = rawPath.trim()
        if (trimmed.isBlank()) return "/"

        val normalized = "/${trimmed.trimStart('/')}"
        return when {
            normalized.startsWith("/api/storage/") -> normalized.removePrefix("/api")
            normalized == "/api" -> "/"
            else -> normalized
        }
    }

    private fun isLocalImageUri(value: String): Boolean {
        val normalized = value.lowercase()
        return normalized.startsWith("content://") ||
            normalized.startsWith("file://") ||
            normalized.startsWith("android.resource://")
    }

    private fun isSupabasePublicUrl(url: HttpUrl): Boolean {
        if (url.host != supabaseBucketHttpUrl.host) return false
        val path = url.encodedPath.trimEnd('/')
        val bucketPath = supabaseBucketHttpUrl.encodedPath.trimEnd('/')
        return path.startsWith(bucketPath)
    }

    private fun isLegacyBackendStorageUrl(url: HttpUrl): Boolean {
        if (url.host != originHttpUrl.host) return false
        val path = url.encodedPath
        return path.startsWith("/storage/") || path.startsWith("/api/storage/")
    }

    private fun normalizeStorageObjectPath(rawPath: String): String {
        val trimmed = rawPath.trim()
        if (trimmed.isBlank()) return ""

        return trimmed
            .removePrefix("/api")
            .removePrefix("api/")
            .trimStart('/')
            .removePrefix("storage/")
            .removePrefix("v1/object/public/")
            .removePrefix("public/")
            .removePrefix("gradshop-media/")
            .trimStart('/')
    }

    private fun buildSupabaseImageUrl(
        path: String,
        encodedQuery: String? = null,
    ): String? {
        val normalizedPath = path.trim().trimStart('/')
        if (normalizedPath.isBlank()) return null
        return supabaseBucketHttpUrl.newBuilder()
            .addEncodedPathSegments(normalizedPath)
            .encodedQuery(encodedQuery)
            .build()
            .toString()
    }
}
