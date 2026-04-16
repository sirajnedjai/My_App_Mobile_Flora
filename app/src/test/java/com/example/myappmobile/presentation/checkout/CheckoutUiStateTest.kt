package com.example.myappmobile.presentation.checkout

import com.example.myappmobile.domain.model.CartItem
import com.example.myappmobile.domain.model.Product
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutUiStateTest {

    @Test
    fun canPlaceOrder_requiresItemsAndAddress() {
        val state = CheckoutUiState(
            fullName = "Elena Vance",
            street = "248 Mercer Street",
            city = "New York",
            postalCode = "10012",
            country = "United States",
            items = listOf(
                CartItem(
                    id = "cart_1",
                    product = Product(
                        id = "p1",
                        name = "Ceramic Bloom Vase",
                        price = 58.0,
                        imageUrl = "https://example.com/vase.jpg",
                        studio = "FLORA Ceramics",
                        category = "Decor",
                    ),
                ),
            ),
        )

        assertTrue(state.canPlaceOrder)
    }

    @Test
    fun canPlaceOrder_isFalseWhenAddressMissing() {
        val state = CheckoutUiState()
        assertFalse(state.canPlaceOrder)
    }
}
