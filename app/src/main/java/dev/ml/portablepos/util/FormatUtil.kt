package dev.ml.portablepos.util
import dev.ml.portablepos.util.formatAmount

import java.util.Locale

fun formatAmount(amount: Double): String {
    return "₱${String.format(Locale.US, "%,.2f", amount)}"
}
