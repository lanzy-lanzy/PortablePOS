package dev.ml.portablepos.presentation.scanner

import dev.ml.portablepos.domain.model.ScannerMode
import dev.ml.portablepos.presentation.navigation.Screen
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BarcodeScannerNavigationTest {
    @Test
    fun directSaleScanFromDashboardOpensPosAfterScannerCloses() {
        assertTrue(shouldOpenPosAfterScannerPop(ScannerMode.SALE, Screen.Dashboard.route))
    }

    @Test
    fun saleScanStartedFromPosOnlyReturnsToExistingPosScreen() {
        assertFalse(shouldOpenPosAfterScannerPop(ScannerMode.SALE, Screen.POS.route))
    }

    @Test
    fun productRegistrationScanDoesNotOpenPos() {
        assertFalse(shouldOpenPosAfterScannerPop(ScannerMode.PRODUCT_REGISTRATION, Screen.Dashboard.route))
    }
}
