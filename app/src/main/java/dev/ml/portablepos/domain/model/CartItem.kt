package dev.ml.portablepos.domain.model

data class CartItem(
    val product: Product,
    var quantity: Int = 1
) {
    val subtotal: Double get() = product.sellingPrice * quantity
}
