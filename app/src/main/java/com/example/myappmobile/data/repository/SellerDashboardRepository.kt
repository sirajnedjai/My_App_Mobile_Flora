package com.example.myappmobile.data.repository

import android.util.Log
import com.example.myappmobile.data.remote.SellerDashboardApiService
import com.example.myappmobile.data.remote.SellerFinanceApiService
import com.example.myappmobile.data.remote.asDoubleOrNull
import com.example.myappmobile.data.remote.asStringOrNull
import com.example.myappmobile.data.remote.asObjectOrNull
import com.example.myappmobile.data.remote.element
import com.example.myappmobile.data.remote.extractDataElement
import com.example.myappmobile.data.remote.int
import com.example.myappmobile.data.remote.objectAt
import com.example.myappmobile.data.remote.requireBody
import com.example.myappmobile.data.remote.string
import com.google.gson.Gson

data class SellerDashboardSummary(
    val storeName: String = "",
    val totalProducts: Int = 0,
    val totalOrders: Int = 0,
    val lowStockProducts: Int = 0,
    val deliveredOrders: Int = 0,
    val pendingProcessingOrders: Int = 0,
    val deliveredRevenue: Double = 0.0,
    val pendingRevenue: Double = 0.0,
    val availableBalance: Double = 0.0,
    val lifetimeEarnings: Double = 0.0,
    val pendingWithdrawals: Double = 0.0,
    val insights: List<String> = emptyList(),
)

class SellerDashboardRepository(
    private val sellerDashboardApiService: SellerDashboardApiService,
    private val sellerFinanceApiService: SellerFinanceApiService,
    private val gson: Gson,
) {

    suspend fun fetchSummary(): Result<SellerDashboardSummary> = runCatching {
        Log.d(TAG, "Fetching seller dashboard summary")
        val dashboardResponse = sellerDashboardApiService.getDashboard().requireBody(gson)
        val financeResponse = sellerFinanceApiService.getFinanceSummary().requireBody(gson)

        val dashboardData = extractDataElement(dashboardResponse.data)?.asObjectOrNull()
        val financeData = extractDataElement(financeResponse.data)?.asObjectOrNull()

        val overview = dashboardData?.objectAt("overview")
        val financialSummary = dashboardData?.objectAt("financial_summary")
        val financeSummary = financeData?.objectAt("summary")
        val insights = dashboardData?.getAsJsonArray("insights")
            ?.mapNotNull { it.asStringOrNull()?.takeIf(String::isNotBlank) }
            .orEmpty()

        SellerDashboardSummary(
            storeName = dashboardData?.string("store_name").orEmpty(),
            totalProducts = overview?.int("total_products") ?: 0,
            totalOrders = overview?.int("total_orders") ?: 0,
            lowStockProducts = overview?.int("low_stock_products") ?: 0,
            deliveredOrders = overview?.int("delivered_orders") ?: 0,
            pendingProcessingOrders = overview?.int("pending_processing_orders") ?: 0,
            deliveredRevenue = financialSummary?.element("delivered_revenue")?.asDoubleOrNull() ?: 0.0,
            pendingRevenue = financialSummary?.element("pending_revenue")?.asDoubleOrNull() ?: 0.0,
            availableBalance = financeSummary?.element("available_balance")?.asDoubleOrNull() ?: 0.0,
            lifetimeEarnings = financeSummary?.element("lifetime_earnings")?.asDoubleOrNull() ?: 0.0,
            pendingWithdrawals = financeSummary?.element("pending_withdrawals")?.asDoubleOrNull() ?: 0.0,
            insights = insights,
        ).also { summary ->
            Log.d(
                TAG,
                "Mapped seller dashboard summary: store=${summary.storeName}, products=${summary.totalProducts}, orders=${summary.totalOrders}, balance=${summary.availableBalance}",
            )
        }
    }

    private companion object {
        const val TAG = "SellerDashboardRepo"
    }
}
