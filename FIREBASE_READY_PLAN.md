# Firebase Integration Plan

## Overview
This document outlines the steps needed to integrate Firebase into the PortablePOS app.
The app is currently fully offline-first using RoomDB. Firebase will be added as a sync layer.

## Prerequisites
1. Add Firebase to the project using Firebase Console
2. Add `google-services.json` to `app/` directory
3. Add Firebase dependencies to `build.gradle.kts`

## Steps to Add Firebase

### 1. Add Dependencies
Add to `gradle/libs.versions.toml`:
- Firebase BoM (`com.google.firebase:firebase-bom`)
- Firebase Firestore (`com.google.firebase:firebase-firestore-ktx`)
- Firebase Authentication (`com.google.firebase:firebase-auth-ktx`)
- Firebase Storage (`com.google.firebase:firebase-storage-ktx`)

Add to `app/build.gradle.kts`:
- `google-services` plugin
- Firebase dependency implementations

### 2. Implement Remote Data Sources
Files to implement in `data/remote/firebase/`:
- `FirebaseProductDataSource.kt`
- `FirebaseSaleDataSource.kt`
- `FirebaseCategoryDataSource.kt`
- `FirebaseStockMovementDataSource.kt`

### 3. Implement Sync Logic
Files to implement in `data/remote/`:
- `SyncManager.kt` - Orchestrate sync operations
- `FirebaseSyncWorker.kt` - WorkManager periodic sync
- `ConflictResolver.kt` - Handle sync conflicts (last-write-wins)

### 4. Update Repository Implementations
Modify `data/repository/` implementations to:
- Check network availability
- Write to RoomDB first
- Queue sync operations with sync status
- Return data from RoomDB (single source of truth)

### 5. Add Authentication
- Implement Firebase Auth with email/password or phone
- Add login screen
- Store auth state in DataStore

### 6. Add Image Upload
- Use Firebase Storage for product images
- Store Firebase image URL in product entity

## Firebase Entity Mapping
| Room Entity | Firestore Collection | Sync Direction |
|---|---|---|
| ProductEntity | products | Bidirectional |
| CategoryEntity | categories | Bidirectional |
| SaleEntity | sales | Local → Remote |
| SaleItemEntity | sale_items | Local → Remote |
| StockMovementEntity | stock_movements | Local → Remote |
| CashierEntity | cashiers | Remote → Local |

## Sync Strategy
- **Offline-first**: Always read/write to RoomDB first
- **Queue changes**: Mark synced entities with PENDING_* status
- **Periodic sync**: Use WorkManager to sync every 15 minutes
- **Manual sync**: Add sync button in Settings
- **Conflict resolution**: Last-write-wins with timestamp comparison
