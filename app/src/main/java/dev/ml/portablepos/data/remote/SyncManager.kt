package dev.ml.portablepos.data.remote

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Placeholder SyncManager for future Firebase synchronization.
 *
 * When Firebase is integrated, this manager will:
 * 1. Check pending sync status from RoomDB entities
 * 2. Upload new/changed data to Firebase Firestore
 * 3. Download remote changes and merge with local data
 * 4. Handle conflict resolution using timestamp-based strategy
 * 5. Update sync status fields after successful sync
 *
 * The app currently works fully offline using RoomDB.
 * Firebase sync will be an additional feature.
 */
@Singleton
class SyncManager @Inject constructor() {

    // TODO: Implement Firebase sync logic
    // fun syncAll(): Flow<SyncProgress>
    // suspend fun syncProducts()
    // suspend fun syncSales()
    // suspend fun syncStockMovements()
}
