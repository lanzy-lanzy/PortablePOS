# PortablePOS Architecture

## Overview
PortablePOS is an offline-first Mobile Point of Sale Android application built with Kotlin and Jetpack Compose. It follows Clean Architecture principles with MVVM pattern.

## Architecture Layers

### 1. Data Layer (`data/`)
Contains database, repositories, and data sources.

```
data/
  local/
    dao/         - Room Data Access Objects
    entity/      - Room Entity classes
    database/    - Room database configuration
    preferences/ - DataStore preferences
  remote/
    firebase/    - Firebase data sources (placeholder)
    dto/         - Data Transfer Objects (placeholder)
  repository/    - Repository implementations
  mapper/        - Entity ↔ Domain model mappers
```

### 2. Domain Layer (`domain/`)
Contains business logic and models.

```
domain/
  model/        - Pure Kotlin data classes
  repository/   - Repository interfaces
  usecase/      - Business logic use cases
```

### 3. Presentation Layer (`presentation/`)
Contains Compose UI and ViewModels.

```
presentation/
  splash/       - Splash screen
  dashboard/    - Main dashboard
  product/      - Product CRUD screens
  scanner/      - Barcode scanner
  pos/          - POS/cart/checkout
  receipt/      - Receipt display
  saleshistory/ - Transaction history
  inventory/    - Stock management
  reports/      - Sales reports
  settings/     - App settings
  navigation/   - Navigation graph
  components/   - Reusable UI components
```

### 4. DI Layer (`di/`)
Hilt dependency injection modules.

## Key Design Decisions

### Offline-First
- RoomDB is the primary data source
- All features work without internet
- Firebase will be added as a sync layer later

### MVVM + Clean Architecture
- ViewModels expose StateFlow for UI state
- Use cases encapsulate business logic
- Repositories abstract data sources
- UI is stateless and reactive

### Navigation
- Navigation Compose for routing
- 17 screens with typed arguments
- Bottom navigation prepared for future use

## Data Flow
```
UI (Compose) → ViewModel → UseCase → Repository → RoomDB
                   ↓                        ↓
              StateFlow                 Firebase (future)
```

## Tech Stack
- Kotlin
- Jetpack Compose + Material 3
- Room Database
- Hilt DI
- Navigation Compose
- CameraX + ML Kit Barcode Scanning
- DataStore Preferences
- Kotlin Coroutines + Flow
