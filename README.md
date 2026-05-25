# PortablePOS - Mobile Point of Sale Scanner

An offline-first Mobile POS Scanner Android application built with Kotlin and Jetpack Compose.

## Features

- **Dashboard** - Sales summary, low stock alerts, quick actions
- **Product Management** - Full CRUD with barcode scanning
- **Barcode Scanner** - CameraX + ML Kit, flashlight, vibration feedback
- **POS / Checkout** - Cart management, discounts, cash payment
- **Receipts** - Shareable digital receipts
- **Sales History** - Search, filter by date, view details
- **Inventory** - Stock tracking, color-coded status, adjustments
- **Reports** - Daily/weekly/monthly sales, best-sellers, low stock

## Tech Stack

- Kotlin + Jetpack Compose + Material 3
- Room Database (offline-first)
- Hilt DI
- Navigation Compose
- CameraX + ML Kit Barcode Scanning
- DataStore Preferences
- MVVM + Clean Architecture

## Architecture

```
app/
  data/       - Room DB, DAOs, repositories, mappers
  domain/     - Models, repository interfaces, use cases
  presentation/ - Compose screens, ViewModels
  di/         - Hilt modules
```

## Setup

1. Open in Android Studio
2. Sync Gradle
3. Run on device (API 24+)

## Future: Firebase Integration

See `FIREBASE_READY_PLAN.md` for details on adding Firebase sync.

## Testing

See `TESTING_CHECKLIST.md` for the complete testing guide.
