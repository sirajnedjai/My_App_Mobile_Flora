package com.example.myappmobile.data.remote

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class LaravelApiResponse<T>(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null,
    @SerializedName("errors") val errors: Map<String, List<String>>? = null,
)

data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
)

data class RegisterRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("role") val role: String,
    @SerializedName("is_seller") val isSeller: Boolean,
    @SerializedName("store_name") val storeName: String? = null,
    @SerializedName("store_address") val storeAddress: String? = null,
    @SerializedName("postal_code") val postalCode: String? = null,
)

data class ChangePasswordRequestDto(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("password") val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
)

data class UpdateProfileRequestDto(
    @SerializedName("name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("address") val address: String? = null,
    @SerializedName("store_name") val storeName: String? = null,
)

data class UserDto(
    @SerializedName(value = "id", alternate = ["_id", "user_id"]) val id: JsonElement? = null,
    @SerializedName(value = "name", alternate = ["full_name", "fullname"]) val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName(value = "phone", alternate = ["phone_number"]) val phone: String? = null,
    @SerializedName(value = "avatar", alternate = ["avatar_url", "avatarUrl", "profile_photo_url", "profile_picture", "profile_picture_url", "avatar_image", "image", "photo"]) val avatarUrl: String? = null,
    @SerializedName(value = "role", alternate = ["user_type", "account_type"]) val role: String? = null,
    @SerializedName("is_seller") val isSeller: Boolean? = null,
    @SerializedName(value = "address", alternate = ["street_address"]) val address: String? = null,
    @SerializedName(value = "store_name", alternate = ["shop_name"]) val storeName: String? = null,
    @SerializedName(value = "is_verified", alternate = ["verified"]) val isVerified: Boolean? = null,
    @SerializedName(value = "approved", alternate = ["is_approved"]) val approved: Boolean? = null,
    @SerializedName("verification_status") val verificationStatus: String? = null,
    @SerializedName("store") val store: JsonElement? = null,
    @SerializedName("seller") val seller: JsonElement? = null,
    @SerializedName("profile") val profile: JsonElement? = null,
)

data class AuthPayloadDto(
    @SerializedName(value = "token", alternate = ["access_token", "accessToken", "plainTextToken"]) val token: String? = null,
    @SerializedName("user") val user: UserDto? = null,
)

data class ProductDto(
    @SerializedName(value = "id", alternate = ["_id", "product_id"]) val id: JsonElement? = null,
    @SerializedName(value = "name", alternate = ["title"]) val name: String? = null,
    @SerializedName(value = "description", alternate = ["details", "story"]) val description: String? = null,
    @SerializedName(value = "price", alternate = ["amount", "sale_price"]) val price: JsonElement? = null,
    @SerializedName(value = "image", alternate = ["image_path", "product_image", "store_image"]) val image: String? = null,
    @SerializedName(value = "image_url", alternate = ["imageUrl", "thumbnail", "photo", "thumbnail_url"]) val imageUrl: String? = null,
    @SerializedName("images") val images: JsonElement? = null,
    @SerializedName("category") val category: JsonElement? = null,
    @SerializedName(value = "store_id", alternate = ["seller_id"]) val storeId: JsonElement? = null,
    @SerializedName("store") val store: JsonElement? = null,
    @SerializedName("seller") val seller: JsonElement? = null,
    @SerializedName(value = "stock", alternate = ["stock_count", "quantity"]) val stock: JsonElement? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName(value = "is_favorite", alternate = ["favorited", "in_favorites"]) val isFavorite: Boolean? = null,
    @SerializedName(value = "is_featured", alternate = ["featured"]) val isFeatured: Boolean? = null,
    @SerializedName(value = "is_new_arrival", alternate = ["new_arrival"]) val isNewArrival: Boolean? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("reviews") val reviews: JsonElement? = null,
)

data class StoreDto(
    @SerializedName(value = "id", alternate = ["_id", "store_id", "seller_id"]) val id: JsonElement? = null,
    @SerializedName(value = "name", alternate = ["store_name", "shop_name"]) val name: String? = null,
    @SerializedName(value = "owner_name", alternate = ["seller_name"]) val ownerName: String? = null,
    @SerializedName(value = "description", alternate = ["bio"]) val description: String? = null,
    @SerializedName(value = "logo", alternate = ["logo_url", "store_image", "avatar", "avatar_url", "profile_picture", "profile_picture_url", "image"]) val logoUrl: String? = null,
    @SerializedName(value = "banner", alternate = ["banner_url"]) val bannerUrl: String? = null,
)

data class CartItemDto(
    @SerializedName(value = "id", alternate = ["cart_item_id"]) val id: JsonElement? = null,
    @SerializedName(value = "product_id", alternate = ["id_product"]) val productId: JsonElement? = null,
    @SerializedName("product") val product: JsonElement? = null,
    @SerializedName("quantity") val quantity: JsonElement? = null,
    @SerializedName(value = "variant", alternate = ["selected_variant"]) val variant: String? = null,
    @SerializedName(value = "size", alternate = ["selected_size"]) val size: String? = null,
)

data class CartDto(
    @SerializedName(value = "id", alternate = ["cart_id"]) val id: JsonElement? = null,
    @SerializedName("items") val items: List<CartItemDto>? = null,
    @SerializedName("cart_items") val cartItems: List<CartItemDto>? = null,
)

data class AddToCartRequestDto(
    @SerializedName("product_id") val productId: String,
    @SerializedName("quantity") val quantity: Int = 1,
)

data class UpdateCartItemRequestDto(
    @SerializedName("quantity") val quantity: Int,
)

data class CheckoutItemRequestDto(
    @SerializedName("product_id") val productId: String,
    @SerializedName("quantity") val quantity: Int,
)

data class CheckoutRequestDto(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("country") val country: String,
    @SerializedName("state") val state: String,
    @SerializedName("municipality") val municipality: String,
    @SerializedName("neighborhood") val neighborhood: String,
    @SerializedName("street_address") val streetAddress: String,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("shipping_method") val shippingMethod: String,
    @SerializedName("payment_method") val paymentMethod: String,
)

data class FavoriteDto(
    @SerializedName(value = "id", alternate = ["favorite_id"]) val id: JsonElement? = null,
    @SerializedName(value = "product_id", alternate = ["id"]) val productId: JsonElement? = null,
    @SerializedName("product") val product: JsonElement? = null,
)

data class FavoriteRequestDto(
    @SerializedName("product_id") val productId: String,
)

data class OrderDto(
    @SerializedName(value = "id", alternate = ["order_id"]) val id: JsonElement? = null,
    @SerializedName(value = "reference", alternate = ["reference_number", "order_number"]) val reference: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName(value = "total", alternate = ["grand_total"]) val total: JsonElement? = null,
    @SerializedName("items") val items: JsonElement? = null,
    @SerializedName(value = "tracking_number", alternate = ["tracking_no", "awb_number"]) val trackingNumber: String? = null,
    @SerializedName(value = "carrier", alternate = ["shipping_carrier", "courier"]) val carrier: String? = null,
    @SerializedName(value = "shipment_status", alternate = ["shipping_status", "delivery_status", "tracking_status", "fulfillment_status"]) val shipmentStatus: String? = null,
)

data class SellerOrderStatusUpdateRequestDto(
    @SerializedName("status") val status: String,
)

data class SellerProductUpsertPayload(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double,
    @SerializedName("category") val category: String,
    @SerializedName("stock") val stock: Int,
    @SerializedName("status") val status: String,
    val imageUri: String? = null,
)

data class ReviewDto(
    @SerializedName(value = "id", alternate = ["review_id"]) val id: JsonElement? = null,
    @SerializedName(value = "author_name", alternate = ["user_name", "name"]) val authorName: String? = null,
    @SerializedName("rating") val rating: JsonElement? = null,
    @SerializedName(value = "comment", alternate = ["review", "text", "body"]) val text: String? = null,
    @SerializedName(value = "verified", alternate = ["is_verified"]) val isVerified: Boolean? = null,
    @SerializedName(value = "created_at", alternate = ["date"]) val createdAt: String? = null,
)

data class SellerVerificationStatusDto(
    @SerializedName(value = "status", alternate = ["verification_status", "approval_status", "seller_status", "delivery_status"])
    val status: String? = null,
    @SerializedName(value = "full_name", alternate = ["name", "owner_name"])
    val fullName: String? = null,
    @SerializedName(value = "phone", alternate = ["phone_number"])
    val phone: String? = null,
    @SerializedName(value = "national_id", alternate = ["document_number", "national_id_number", "id_number"])
    val nationalId: String? = null,
    @SerializedName(value = "address", alternate = ["store_address"])
    val address: String? = null,
    @SerializedName(value = "store_name", alternate = ["shop_name"])
    val storeName: String? = null,
    @SerializedName(value = "description", alternate = ["bio", "about"])
    val description: String? = null,
    @SerializedName(value = "document_url", alternate = ["document", "document_file", "verification_document", "document_image"])
    val documentUrl: String? = null,
    @SerializedName(value = "rejection_reason", alternate = ["reason", "rejected_reason", "admin_note", "note", "comment"])
    val rejectionReason: String? = null,
    @SerializedName(value = "submitted_at", alternate = ["created_at", "requested_at"])
    val submittedAt: String? = null,
    @SerializedName(value = "reviewed_at", alternate = ["updated_at", "approved_at", "rejected_at"])
    val reviewedAt: String? = null,
)
