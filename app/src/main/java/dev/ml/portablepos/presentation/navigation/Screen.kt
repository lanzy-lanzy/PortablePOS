package dev.ml.portablepos.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object ProductList : Screen("product_list")
    object AddProduct : Screen("add_product?barcode={barcode}") {
        fun createRoute(barcode: String? = null) = "add_product?barcode=${barcode ?: ""}"
    }
    object EditProduct : Screen("edit_product/{productId}") {
        fun createRoute(productId: Long) = "edit_product/$productId"
    }
    object Scanner : Screen("scanner/{mode}") {
        fun createRoute(mode: String) = "scanner/$mode"
    }
    object POS : Screen("pos")
    object Checkout : Screen("checkout")
    object Receipt : Screen("receipt/{saleId}") {
        fun createRoute(saleId: Long) = "receipt/$saleId"
    }
    object SalesHistory : Screen("sales_history")
    object SaleDetail : Screen("sale_detail/{saleId}") {
        fun createRoute(saleId: Long) = "sale_detail/$saleId"
    }
    object Inventory : Screen("inventory")
    object StockAdjustment : Screen("stock_adjustment/{productId}") {
        fun createRoute(productId: Long) = "stock_adjustment/$productId"
    }
    object Reports : Screen("reports")
    object Return : Screen("return/{saleId}") {
        fun createRoute(saleId: Long) = "return/$saleId"
    }
    object ReturnHistory : Screen("return_history")
    object Settings : Screen("settings")
}
