package com.example.myappmobile.data.local.dummy

import com.example.myappmobile.domain.model.Collection
import com.example.myappmobile.domain.model.LedgerEntry
import com.example.myappmobile.domain.model.Review
import com.example.myappmobile.domain.model.Store

object DummyStores {

    val floraCeramics = Store(
        id = "s1",
        name = "FLORA Ceramics",
        ownerName = "Sienna Moretti",
        description = "Hand-thrown ceramics inspired by the Wabi-Sabi philosophy.",
        logoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
        bannerUrl = "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800",
        location = "Florence, Italy",
        contactEmail = "sienna@flora.com",
        rating = 4.8f,
        reviewCount = 100,
        practisingSince = "2012",
        totalSales = 842,
        activeProducts = 48,
        monthlyEarnings = 12480.0,
        availableBalance = 12480.0,
        lifetimeEarnings = 84210.0,
        categories = listOf("Ceramics", "Textiles"),
        story = "Based in the sun-soaked valleys of Tuscany, Sienna Moretti's work is a dialogue between raw earth and human touch. Her practice centers on the philosophy of Wabi-Sabi — finding beauty in the imperfect and the ephemeral.\n\nEach ceramic vessel is hand-thrown using local clays, and every textile piece is woven on a traditional floor loom using organic linens dyed with botanical extracts from her own garden. This is slow craft, intended to bring a sense of quietude to the modern home.",
    )

    val collections = listOf(
        Collection(
            id = "c1",
            name = "Wabi-Sabi Ceramics",
            itemCount = 12,
            createdDate = "Created June 2023",
            imageUrl = "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=200",
        ),
        Collection(
            id = "c2",
            name = "Summer Linens",
            itemCount = 8,
            createdDate = "Created May 2023",
            imageUrl = "https://images.unsplash.com/photo-1617952739429-b4f8a52ccad6?w=200",
        ),
        Collection(
            id = "c3",
            name = "Foundry Gold",
            itemCount = 24,
            createdDate = "Created Jan 2023",
            imageUrl = "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=200",
        ),
    )

    val storeReviews = listOf(
        Review(
            id = "r1",
            authorName = "ELENA R. — PATRON",
            rating = 5,
            text = "\"The texture of the linen is unlike anything I've owned. It feels alive. Sienna's attention to detail is evident in every single stitch.\"",
            isVerified = true,
        ),
        Review(
            id = "r2",
            authorName = "JULIAN R. PORTLAND",
            rating = 5,
            text = "\"The vase arrived perfectly packaged in sustainable materials. It now sits as the centerpiece of my dining room, capturing the light beautifully.\"",
            isVerified = true,
        ),
    )

    val ledgerEntries = listOf(
        LedgerEntry(
            id = "le1",
            title = "Ebony & Gold Ceramic Set",
            date = "OCT 24, 2023",
            type = "ORDER #9214",
            amount = 840.0,
            isPositive = true,
            status = "SETTLED",
        ),
        LedgerEntry(
            id = "le2",
            title = "Payout to Chase Savings",
            date = "OCT 21, 2023",
            type = "WITHDRAWAL",
            amount = 2500.0,
            isPositive = false,
            status = "PROCESSED",
        ),
        LedgerEntry(
            id = "le3",
            title = "Linen Textile Restoration Fee",
            date = "OCT 19, 2023",
            type = "COMMISSION",
            amount = 1200.0,
            isPositive = true,
            status = "SETTLED",
        ),
        LedgerEntry(
            id = "le4",
            title = "Raw Material Procurement",
            date = "OCT 15, 2023",
            type = "EXPENSE",
            amount = 312.40,
            isPositive = false,
            status = "PROCESSED",
        ),
    )
}