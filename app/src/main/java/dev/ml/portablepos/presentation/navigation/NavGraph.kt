package dev.ml.portablepos.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import dev.ml.portablepos.presentation.dashboard.DashboardScreen
import dev.ml.portablepos.presentation.inventory.InventoryScreen
import dev.ml.portablepos.presentation.inventory.StockAdjustmentScreen
import dev.ml.portablepos.presentation.pos.CheckoutScreen
import dev.ml.portablepos.presentation.pos.POSScreen
import dev.ml.portablepos.presentation.product.AddEditProductScreen
import dev.ml.portablepos.presentation.product.EditProductScreen
import dev.ml.portablepos.presentation.product.ProductListScreen
import dev.ml.portablepos.presentation.receipt.ReceiptScreen
import dev.ml.portablepos.presentation.reports.ReportsScreen
import dev.ml.portablepos.presentation.saleshistory.SaleDetailScreen
import dev.ml.portablepos.presentation.saleshistory.SalesHistoryScreen
import dev.ml.portablepos.presentation.scanner.BarcodeScannerScreen
import dev.ml.portablepos.presentation.settings.SettingsScreen
import dev.ml.portablepos.presentation.splash.SplashScreen

@Composable
fun NavGraph(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNav = currentRoute in showBottomNavRoutes

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(navController = navController)
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(navController = navController)
            }

            composable(Screen.ProductList.route) {
                ProductListScreen(navController = navController)
            }

            composable(
                route = Screen.AddProduct.route,
                arguments = listOf(
                    navArgument("barcode") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val barcode = backStackEntry.arguments?.getString("barcode") ?: ""
                AddEditProductScreen(
                    navController = navController,
                    barcode = barcode.ifBlank { null }
                )
            }

            composable(
                route = Screen.EditProduct.route,
                arguments = listOf(
                    navArgument("productId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                EditProductScreen(
                    navController = navController,
                    productId = productId
                )
            }

            composable(
                route = Screen.Scanner.route,
                arguments = listOf(
                    navArgument("mode") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val mode = backStackEntry.arguments?.getString("mode") ?: ""
                BarcodeScannerScreen(
                    navController = navController,
                    mode = mode
                )
            }

            composable(Screen.POS.route) {
                POSScreen(navController = navController)
            }

            composable(Screen.Checkout.route) {
                CheckoutScreen(navController = navController)
            }

            composable(
                route = Screen.Receipt.route,
                arguments = listOf(
                    navArgument("saleId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val saleId = backStackEntry.arguments?.getLong("saleId") ?: 0L
                ReceiptScreen(
                    navController = navController,
                    saleId = saleId
                )
            }

            composable(Screen.SalesHistory.route) {
                SalesHistoryScreen(navController = navController)
            }

            composable(
                route = Screen.SaleDetail.route,
                arguments = listOf(
                    navArgument("saleId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val saleId = backStackEntry.arguments?.getLong("saleId") ?: 0L
                SaleDetailScreen(
                    navController = navController,
                    saleId = saleId
                )
            }

            composable(Screen.Inventory.route) {
                InventoryScreen(navController = navController)
            }

            composable(
                route = Screen.StockAdjustment.route,
                arguments = listOf(
                    navArgument("productId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                StockAdjustmentScreen(
                    navController = navController,
                    productId = productId
                )
            }

            composable(Screen.Reports.route) {
                ReportsScreen(navController = navController)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }
        }
    }
}
