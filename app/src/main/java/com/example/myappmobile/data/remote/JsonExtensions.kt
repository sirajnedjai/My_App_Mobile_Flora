package com.example.myappmobile.data.remote

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

fun JsonElement?.asObjectOrNull(): JsonObject? =
    if (this != null && isJsonObject) asJsonObject else null

fun JsonElement?.asArrayOrNull(): JsonArray? =
    if (this != null && isJsonArray) asJsonArray else null

fun JsonElement?.asStringOrNull(): String? = when {
    this == null || isJsonNull -> null
    isJsonPrimitive -> asJsonPrimitive.asString
    else -> toString()
}

fun JsonElement?.asBooleanOrNull(): Boolean? = when {
    this == null || isJsonNull -> null
    isJsonPrimitive && asJsonPrimitive.isBoolean -> asBoolean
    isJsonPrimitive -> asStringOrNull()?.toBooleanStrictOrNull()
    else -> null
}

fun JsonElement?.asIntOrNull(): Int? = when {
    this == null || isJsonNull -> null
    isJsonPrimitive && asJsonPrimitive.isNumber -> asInt
    isJsonPrimitive -> asStringOrNull()?.toDoubleOrNull()?.toInt()
    else -> null
}

fun JsonElement?.asDoubleOrNull(): Double? = when {
    this == null || isJsonNull -> null
    isJsonPrimitive && asJsonPrimitive.isNumber -> asDouble
    isJsonPrimitive -> asStringOrNull()?.toDoubleOrNull()
    else -> null
}

fun JsonObject.string(vararg keys: String): String? = keys.firstNotNullOfOrNull { key ->
    get(key).asStringOrNull()?.takeIf { it.isNotBlank() }
}

fun JsonObject.boolean(vararg keys: String): Boolean? = keys.firstNotNullOfOrNull { key ->
    get(key).asBooleanOrNull()
}

fun JsonObject.int(vararg keys: String): Int? = keys.firstNotNullOfOrNull { key ->
    get(key).asIntOrNull()
}

fun JsonObject.double(vararg keys: String): Double? = keys.firstNotNullOfOrNull { key ->
    get(key).asDoubleOrNull()
}

fun JsonObject.element(vararg keys: String): JsonElement? = keys.firstNotNullOfOrNull { key ->
    get(key)?.takeIf { !it.isJsonNull }
}

fun JsonObject.objectAt(vararg keys: String): JsonObject? = keys.firstNotNullOfOrNull { key ->
    get(key).asObjectOrNull()
}

fun JsonObject.arrayAt(vararg keys: String): JsonArray? = keys.firstNotNullOfOrNull { key ->
    get(key).asArrayOrNull()
}

fun jsonPrimitive(value: String): JsonElement = JsonPrimitive(value)
