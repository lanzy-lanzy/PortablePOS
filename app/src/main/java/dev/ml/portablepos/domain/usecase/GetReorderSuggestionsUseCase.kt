package dev.ml.portablepos.domain.usecase

import dev.ml.portablepos.domain.model.Product
import dev.ml.portablepos.domain.repository.ProductRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class ReorderSuggestion(
    val product: Product,
    val suggestedOrderQuantity: Int,
    val reason: String
)

class GetReorderSuggestionsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(): List<ReorderSuggestion> {
        val lowStockProducts = productRepository.getLowStockProducts().first()
        val outOfStockProducts = productRepository.getOutOfStockProducts().first()

        val suggestions = mutableListOf<ReorderSuggestion>()

        for (product in outOfStockProducts) {
            val orderQty = maxOf(product.reorderLevel * 2, 10)
            suggestions.add(
                ReorderSuggestion(
                    product = product,
                    suggestedOrderQuantity = orderQty,
                    reason = "Out of stock"
                )
            )
        }

        for (product in lowStockProducts) {
            val deficit = product.reorderLevel - product.stockQuantity
            if (deficit > 0) {
                val orderQty = maxOf(deficit + product.reorderLevel, 10)
                suggestions.add(
                    ReorderSuggestion(
                        product = product,
                        suggestedOrderQuantity = orderQty,
                        reason = "Low stock (${product.stockQuantity} remaining, reorder at ${product.reorderLevel})"
                    )
                )
            }
        }

        return suggestions.sortedBy { it.product.stockQuantity }
    }
}