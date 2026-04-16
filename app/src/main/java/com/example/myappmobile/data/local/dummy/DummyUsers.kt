package com.example.myappmobile.data.local.dummy

import com.example.myappmobile.domain.model.Address
import com.example.myappmobile.domain.model.User

object DummyUsers {

    val buyer = User(
        id = "u1",
        fullName = "Baha",
        email = "baha@flora.com",
        phone = "+1 (555) 234-8890",
        avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
        membershipTier = "PREMIUM MEMBER",
        isAuthenticated = true,
    )

    val buyer2 = User(
        id = "u2",
        fullName = "Julianne Moore",
        email = "julianne@flora.com",
        phone = "+1 (555) 100-2020",
        avatarUrl = "https://images.unsplash.com/photo-1494790108755-2616b612b47c?w=200",
        membershipTier = "PREMIUM MEMBER",
        isAuthenticated = true,
    )

    val elena = User(
        id = "u3",
        fullName = "Elena Vance",
        email = "elena.vance@flora.com",
        phone = "+1 (555) 300-4400",
        avatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200",
        membershipTier = "AUTHENTICATED MEMBER",
        isAuthenticated = true,
    )

    val seller = User(
        id = "s1",
        fullName = "Bachir",
        email = "bachir@flora.com",
        phone = "+39 055 234-8890",
        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
        membershipTier = "MASTER CERAMICIST",
        isAuthenticated = true,
        isSeller = true,
    )

    val savedAddresses = listOf(
        Address(
            id = "addr1",
            label = "Home — Primary",
            fullName = "Baha",
            street = "124 West 22nd St, Apt 4B",
            city = "Chelsea",
            postalCode = "NY 10011",
            country = "United States",
            isPrimary = true,
        ),
        Address(
            id = "addr2",
            label = "Office",
            fullName = "Julian Thorne",
            street = "Flora Studios, 88 Hudson Yards",
            city = "New York",
            postalCode = "NY 10001",
            country = "United States",
            isPrimary = false,
        ),
    )
}
