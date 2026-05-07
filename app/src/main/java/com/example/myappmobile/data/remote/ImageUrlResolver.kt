package com.example.myappmobile.data.remote

object ImageUrlResolver {
    fun resolveOrNull(raw: String?): String? = BackendUrlResolver.resolveImageUrlOrNull(raw)

    fun normalize(raw: String?): String = resolveOrNull(raw).orEmpty()
}
