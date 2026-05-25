package dev.ml.portablepos.presentation.product

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun EditProductScreen(
    navController: NavHostController,
    productId: Long,
    viewModel: AddEditProductViewModel = hiltViewModel()
) {
    AddEditProductScreen(
        navController = navController,
        productId = productId,
        viewModel = viewModel
    )
}
