package dev.ml.portablepos.data.remote.firebase

/**
 * Placeholder for Firebase remote data source.
 * This will be implemented when Firebase is integrated.
 *
 * Future implementation will include:
 * - FirebaseFirestore for data sync
 * - FirebaseAuth for authentication
 * - FirebaseStorage for image upload
 *
 * The app currently uses RoomDB as the primary backend.
 * Firebase will be added as a secondary sync layer.
 */
interface FirebaseDataSource {
    // TODO: Add Firebase sync methods
    // suspend fun syncProducts()
    // suspend fun syncSales()
    // suspend fun uploadImage(imagePath: String): String
}
