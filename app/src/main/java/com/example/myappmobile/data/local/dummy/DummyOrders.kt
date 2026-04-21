package com.example.myappmobile.data.local.dummy

import com.example.myappmobile.domain.model.*

object DummyOrders {

    val address1 = Address(
        id = "a1",
        fullName = "Eleanor Sterling",
        state = "New York",
        municipality = "New York City",
        neighborhood = "Greenwich Village",
        street = "248 Mercer Street, Apt 4B",
        postalCode = "10012",
        country = "United States",
        isPrimary = true,
    )

    val address2 = Address(
        id = "a2",
        fullName = "Julianne Moore",
        state = "Ile-de-France",
        municipality = "Paris",
        neighborhood = "1st arrondissement",
        street = "24 Rue de l'Atelier",
        postalCode = "75001",
        country = "France",
    )

    val recentOrders = listOf(
        Order(
            id = "o1",
            reference = "AT-88291",
            customerId = "u1",
            customerName = "Baha",
            customerEmail = "baha@flora.com",
            items = listOf(
                OrderItem("oi1", DummyProducts.heirloomTote, quantity = 1, variant = "Camel / Handwoven"),
            ),
            status = OrderStatus.SHIPPED,
            total = 420.0,
            placedDate = "Nov 12",
            estimatedDelivery = "Nov 18",
        ),
        Order(
            id = "o2",
            reference = "AT-88204",
            customerId = "u1",
            customerName = "Baha",
            customerEmail = "baha@flora.com",
            items = listOf(
                OrderItem("oi2", DummyProducts.sculpturalVase, quantity = 1, variant = "Stone / Matte"),
            ),
            status = OrderStatus.DELIVERED,
            total = 340.0,
            placedDate = "Oct 29",
            estimatedDelivery = "Delivered Nov 03",
        ),
        Order(
            id = "o3",
            reference = "AT-88156",
            customerId = "u2",
            customerName = "Julian Thorne",
            items = listOf(
                OrderItem("oi3", DummyProducts.rawLinenThrow, quantity = 2, variant = "Natural Linen"),
            ),
            status = OrderStatus.DELIVERED,
            total = 85.0,
            placedDate = "Oct 15",
            estimatedDelivery = "Delivered Oct 21",
        ),
        Order(
            id = "o4",
            reference = "AT-88312",
            customerId = "u3",
            customerName = "Elena Vance",
            customerEmail = "elena.vance@flora.com",
            items = listOf(
                OrderItem("oi4", DummyProducts.signatureCashmereThrow, quantity = 1, variant = "Dove Grey"),
            ),
            status = OrderStatus.CONFIRMED,
            total = 275.0,
            placedDate = "Dec 04",
            estimatedDelivery = "Dec 11",
        ),
    )

    val orderDetail = Order(
        id = "o10",
        reference = "ATL-8492",
        customerName = "Eleanor Sterling",
        customerLocation = "New York, NY",
        items = listOf(
            OrderItem("oi10", DummyProducts.tonalCeramicVessel, quantity = 1, variant = "Hand-thrown, Sand Finish"),
            OrderItem("oi11", DummyProducts.signatureCashmereThrow, quantity = 1, variant = "Charcoal, Ultra-fine Weave"),
        ),
        status = OrderStatus.CONFIRMED,
        subtotal = 509.0,
        shippingCost = 15.0,
        tax = 40.72,
        total = 564.72,
        shippingMethod = "Concierge White-Glove",
        shippingAddress = address1,
        placedDate = "October 24, 2023",
        estimatedDelivery = "Nov 02, 2023",
        artisanPackaging = "Complimentary",
    )

    val confirmationOrder = Order(
        id = "o11",
        reference = "ATL-78923401",
        status = OrderStatus.CONFIRMED,
        shippingMethod = "Concierge White-Glove",
        shippingAddress = Address(
            fullName = "Julien",
            state = "Ile-de-France",
            municipality = "Paris",
            neighborhood = "1st arrondissement",
            street = "214 Rue de Rivoli",
            postalCode = "75001",
            country = "France",
        ),
        placedDate = "Oct 24",
        estimatedDelivery = "Oct 24 — Oct 28",
    )

    // Seller order ledger
    val sellerOrders = listOf(
        Order(
            id = "so1",
            reference = "ORD-2984-A",
            customerName = "Eleanor Vance",
            status = OrderStatus.PENDING,
            total = 840.0,
            imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
        ),
        Order(
            id = "so2",
            reference = "ORD-2899-C",
            customerName = "Julian Thorne",
            status = OrderStatus.SHIPPED,
            total = 1120.0,
            imageUrl = "https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=200",
        ),
        Order(
            id = "so3",
            reference = "ORD-2882-X",
            customerName = "Sienna Miller",
            status = OrderStatus.DELIVERED,
            total = 2450.0,
            imageUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200",
        ),
        Order(
            id = "so4",
            reference = "ORD-2875-B",
            customerName = "Marcus Aurelius",
            status = OrderStatus.PENDING,
            total = 3100.0,
            imageUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200",
        ),
    )

    val pendingOrders = listOf(
        Order(
            id = "po1",
            reference = "AT-8902",
            customerName = "Clara Hemmington",
            customerLocation = "London, United Kingdom",
            status = OrderStatus.PENDING,
            total = 420.0,
        ),
        Order(
            id = "po2",
            reference = "AT-8901",
            customerName = "Julian Marceau",
            customerLocation = "Paris, France",
            status = OrderStatus.PENDING,
            total = 1150.0,
        ),
    )
}
