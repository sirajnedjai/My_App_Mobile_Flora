package com.example.myappmobile.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BackendUrlResolverTest {

    @Test
    fun `returns null for blank image`() {
        assertNull(BackendUrlResolver.resolveImageUrlOrNull(" "))
    }

    @Test
    fun `keeps supabase url as https`() {
        assertEquals(
            "https://navgcisapdcirkjrfigp.supabase.co/storage/v1/object/public/gradshop-media/products/image1.jpg",
            BackendUrlResolver.resolveImageUrlOrNull(
                "http://navgcisapdcirkjrfigp.supabase.co/storage/v1/object/public/gradshop-media/products/image1.jpg",
            ),
        )
    }

    @Test
    fun `converts relative paths to supabase public bucket`() {
        assertEquals(
            "https://navgcisapdcirkjrfigp.supabase.co/storage/v1/object/public/gradshop-media/products/image1.jpg",
            BackendUrlResolver.resolveImageUrlOrNull("products/image1.jpg"),
        )
    }

    @Test
    fun `converts legacy render storage urls to supabase public bucket`() {
        assertEquals(
            "https://navgcisapdcirkjrfigp.supabase.co/storage/v1/object/public/gradshop-media/products/image1.jpg",
            BackendUrlResolver.resolveImageUrlOrNull(
                "https://ecommerce-platform-7dc6.onrender.com/storage/products/image1.jpg",
            ),
        )
    }

    @Test
    fun `keeps local content uri for previews`() {
        assertEquals(
            "content://media/external/images/media/1",
            BackendUrlResolver.resolveImageUrlOrNull("content://media/external/images/media/1"),
        )
    }
}
