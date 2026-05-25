# Mobile POS Scanner App Prompt

## Project Goal

Build a complete **Mobile POS Scanner** application using **Kotlin** and **Jetpack Compose**.

The app must be **offline-first** using **Room Database** as the primary backend. The architecture must also be prepared for future Firebase integration, including Firebase Firestore, Firebase Authentication, and Firebase Storage.

The first version must work fully offline before Firebase is implemented.

---

# 1. Tech Stack

Use the following technologies:

- Kotlin
- Jetpack Compose
- Material 3
- Room Database
- Kotlin Coroutines
- Flow / StateFlow
- MVVM Architecture
- Repository Pattern
- Navigation Compose
- CameraX or ML Kit Barcode Scanning
- DataStore Preferences
- Hilt Dependency Injection if suitable
- Firebase-ready architecture

Firebase must not be the first backend. The first working version must use **RoomDB only**.

---

# 2. App Name

App name:

```txt
portablepos
```

Package name:

```txt
dev.ml.portablepos
```

---

# 3. Main App Concept

Create a mobile Point of Sale system for small businesses such as:

- Sari-sari store
- Grocery store
- Motorparts shop
- Pharmacy
- School canteen
- Small retail shop

The system must allow the user to manage products, scan barcodes, process sales, deduct inventory, generate receipts, and view sales reports.

---

# 4. Core Features

## A. Dashboard Screen

Create a modern dashboard that displays:

- Total sales today
- Number of transactions today
- Low stock products
- Total products
- Quick action buttons:
  - New Sale
  - Scan Barcode
  - Add Product
  - Inventory
  - Sales History
  - Reports

The dashboard must be visually appealing, mobile-first, and easy to use.

---

## B. Product Management

Create full CRUD for products.

Each product must have:

- Product ID
- Product name
- Barcode
- Category
- Description
- Cost price
- Selling price
- Stock quantity
- Reorder level
- Unit type, such as pcs, box, bottle, or pack
- Image path
- Future Firebase image URL
- Firebase ID field
- Sync status
- Last synced date
- Created date
- Updated date

Product features:

- Add product manually
- Add product through barcode scanning
- Edit product
- Delete product
- Search product
- Filter by category
- View low stock products
- Update stock manually
- Prevent duplicate barcode

---

## C. Barcode Scanning Support

Implement barcode scanning using **CameraX** or **ML Kit Barcode Scanning**.

Barcode scanner must support:

- Scan barcode during product registration
- Scan barcode during sales transaction
- Detect if barcode already exists
- Add product to cart if barcode exists
- Show option to register new product if barcode is not found
- Flashlight toggle
- Camera permission handling
- Scanning frame UI
- Beep or vibration feedback after successful scan

Barcode workflow:

1. User taps Scan Barcode.
2. App opens scanner screen.
3. App reads barcode.
4. App checks RoomDB for matching product.
5. If found, display product details or add to cart.
6. If not found, navigate to Add Product screen with barcode pre-filled.

---

## D. POS / New Sale Screen

Create a POS sales screen where the cashier can:

- Search products manually
- Scan barcode to add product to cart
- Add product quantity
- Increase or decrease quantity
- Remove item from cart
- View subtotal
- Apply discount
- Input cash received
- Calculate change
- Complete transaction

Cart item must include:

- Product ID
- Product name
- Barcode
- Quantity
- Unit price
- Total price

Transaction rules:

- Do not allow sale if product stock is insufficient.
- Automatically deduct stock after successful sale.
- Save transaction locally using RoomDB.
- Save transaction items separately.
- Generate receipt data.
- Support cash payment first.
- Prepare structure for future payment methods.

---

## E. Receipt Screen

After completing a sale, show a receipt screen.

Receipt must display:

- Store name
- Transaction number
- Date and time
- Cashier name
- List of items
- Quantity
- Unit price
- Total per item
- Subtotal
- Discount
- Grand total
- Cash received
- Change

Add buttons for:

- New Sale
- Save Receipt
- Share Receipt as text
- Print Receipt later-ready

Printing does not need to be fully implemented yet, but the code must be prepared for Bluetooth printer integration.

---

## F. Sales History

Create a Sales History screen that shows all transactions.

Features:

- List of transactions
- Search by transaction number
- Filter by date
- View transaction details
- View receipt again
- Void or cancel transaction structure, optional

Each transaction must show:

- Transaction number
- Date
- Total amount
- Payment method
- Cashier name

---

## G. Inventory Management

Create an inventory screen that shows:

- All products
- Current stock
- Low stock indicator
- Out of stock indicator
- Reorder level
- Stock adjustment button

Stock adjustment must support:

- Add stock
- Deduct stock
- Reason for adjustment
- Date
- User or cashier who adjusted

Create a stock movement table/entity for tracking inventory changes.

Stock movement types:

- STOCK_IN
- STOCK_OUT
- SALE
- ADJUSTMENT
- RETURN

---

## H. Reports

Create a basic reports screen.

Reports should include:

- Total sales today
- Total sales this week
- Total sales this month
- Best-selling products
- Low stock products
- Number of transactions
- Gross sales
- Total discounts

Use RoomDB queries for report generation.

---

# 5. Room Database Design

Create Room entities for the following:

## ProductEntity

Fields:

- id
- firebaseId
- name
- barcode
- categoryId
- description
- costPrice
- sellingPrice
- stockQuantity
- reorderLevel
- unit
- imagePath
- firebaseImageUrl
- syncStatus
- lastSyncedAt
- createdAt
- updatedAt

## CategoryEntity

Fields:

- id
- firebaseId
- name
- description
- syncStatus
- lastSyncedAt
- createdAt
- updatedAt

## SaleEntity

Fields:

- id
- firebaseId
- transactionNumber
- cashierName
- subtotal
- discount
- totalAmount
- cashReceived
- changeAmount
- paymentMethod
- status
- syncStatus
- lastSyncedAt
- createdAt

## SaleItemEntity

Fields:

- id
- saleId
- productId
- productName
- barcode
- quantity
- unitPrice
- totalPrice

## StockMovementEntity

Fields:

- id
- firebaseId
- productId
- movementType
- quantity
- previousStock
- newStock
- reason
- syncStatus
- lastSyncedAt
- createdAt
- createdBy

## CashierEntity

Fields:

- id
- firebaseId
- fullName
- username
- role
- pinCode
- syncStatus
- lastSyncedAt
- createdAt

Use appropriate foreign keys, indexes, and relationships.

---

# 6. Firebase-Ready Architecture

Even though the first version uses RoomDB only, prepare the project so Firebase can be added later.

Use this structure:

```txt
data/
 ├── local/
 │   ├── dao/
 │   ├── entity/
 │   └── database/
 ├── remote/
 │   ├── firebase/
 │   └── dto/
 ├── repository/
 └── mapper/

domain/
 ├── model/
 ├── repository/
 └── usecase/

presentation/
 ├── dashboard/
 ├── product/
 ├── scanner/
 ├── pos/
 ├── receipt/
 ├── inventory/
 ├── reports/
 └── settings/
```

Create repository interfaces so the app can switch from Room-only to Room + Firebase sync later.

Example:

```kotlin
interface ProductRepository {
    fun getProducts(): Flow<List<Product>>
    suspend fun getProductByBarcode(barcode: String): Product?
    suspend fun addProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
}
```

Prepare future classes:

```txt
ProductRepositoryImpl
LocalProductDataSource
FirebaseProductDataSource
SyncManager
FirebaseSyncWorker
ConflictResolver
```

---

# 7. Sync Status Preparation

Add sync fields to important entities:

- syncStatus
- firebaseId
- lastSyncedAt

Use this enum:

```kotlin
enum class SyncStatus {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE
}
```

Do not fully implement Firebase sync in the first version. Only prepare the architecture.

---

# 8. App Screens

Create the following screens:

1. Splash Screen
2. Store Setup Screen
3. Dashboard Screen
4. Product List Screen
5. Add Product Screen
6. Edit Product Screen
7. Barcode Scanner Screen
8. POS / New Sale Screen
9. Cart Screen
10. Checkout Screen
11. Receipt Screen
12. Sales History Screen
13. Sale Detail Screen
14. Inventory Screen
15. Stock Adjustment Screen
16. Reports Screen
17. Settings Screen

---

# 9. Navigation

Use Navigation Compose.

Suggested routes:

```kotlin
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object ProductList : Screen("product_list")
    object AddProduct : Screen("add_product?barcode={barcode}")
    object EditProduct : Screen("edit_product/{productId}")
    object Scanner : Screen("scanner/{mode}")
    object POS : Screen("pos")
    object Checkout : Screen("checkout")
    object Receipt : Screen("receipt/{saleId}")
    object SalesHistory : Screen("sales_history")
    object Inventory : Screen("inventory")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}
```

Scanner modes:

```kotlin
enum class ScannerMode {
    PRODUCT_REGISTRATION,
    SALE
}
```

---

# 10. UI/UX Design Requirements

Use Jetpack Compose and Material 3.

Design style:

- Modern mobile POS design
- Clean cards
- Rounded corners
- Bottom navigation or drawer navigation
- Floating action buttons
- Search bars
- Product cards
- Sales cart panel
- Clear scan, add, checkout, and print buttons
- Color-coded stock status:
  - Green: available
  - Orange: low stock
  - Red: out of stock

Important UX rules:

- Use large buttons for sales actions.
- Make the barcode scanning flow fast.
- Show a clear cart summary.
- Add confirmation dialog before completing sale.
- Add success dialog after transaction.
- Show error message if stock is insufficient.
- Add loading states.
- Add empty states.
- Add form validation.

---

# 11. Business Logic Rules

Implement these rules:

- Product barcode must be unique.
- Product selling price must be greater than or equal to cost price.
- Stock cannot be negative.
- Sale cannot be completed if cart is empty.
- Sale cannot be completed if cash received is less than total amount.
- Product stock is deducted only after successful transaction.
- Every stock deduction from sale must create a stock movement record.
- Every sale must have a unique transaction number.
- Receipt must be generated after successful sale.
- Low stock products must be detected based on reorder level.

---

# 12. ViewModels

Create ViewModels for:

- DashboardViewModel
- ProductViewModel
- ScannerViewModel
- POSViewModel
- CheckoutViewModel
- ReceiptViewModel
- SalesHistoryViewModel
- InventoryViewModel
- ReportsViewModel
- SettingsViewModel

Each ViewModel must expose UI state using StateFlow.

Example:

```kotlin
data class ProductUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
```

Use sealed classes for UI events where appropriate.

---

# 13. Error Handling

Handle the following errors:

- Camera permission denied
- Barcode scanner failed
- Product not found
- Duplicate barcode
- Insufficient stock
- Empty cart
- Invalid price
- Invalid quantity
- Database errors
- Invalid cash amount

Show user-friendly messages using Snackbar or Dialog.

---

# 14. Sample Data

Create a sample data seeder for testing.

Sample categories:

- Beverages
- Snacks
- Grocery
- Personal Care
- Motorparts
- Medicine

Include sample products with barcode values, stock, and prices.

Also create sample sales records for report testing.

---

# 15. Parallel Agent Workflow

Use multiple specialized agents to work in parallel. Each agent must focus only on its assigned responsibility and must coordinate through shared project rules.

## Agent 1: Project Architect Agent

Responsibility:

- Set up the Android project structure.
- Apply MVVM, Repository Pattern, Clean Architecture style, and modular folder organization.
- Define the package structure.
- Make sure RoomDB is the first backend.
- Prepare Firebase-ready folders but do not implement Firebase first.
- Review if business logic is separated from UI.

Output expected:

- Project structure
- Architecture plan
- Base packages
- Dependency setup
- Navigation foundation

---

## Agent 2: Room Database Agent

Responsibility:

- Create all Room entities.
- Create DAO interfaces.
- Create database class.
- Add TypeConverters if needed.
- Add relationships between Sale and SaleItem.
- Create queries for reports, inventory, product search, and sales history.
- Add indexes for barcode, transaction number, and product name.

Output expected:

- ProductEntity
- CategoryEntity
- SaleEntity
- SaleItemEntity
- StockMovementEntity
- CashierEntity
- DAO files
- AppDatabase class
- Database seed logic

---

## Agent 3: Domain and Repository Agent

Responsibility:

- Create domain models.
- Create repository interfaces.
- Create repository implementations using RoomDB.
- Create mappers between entities and domain models.
- Create use cases for product, sale, inventory, reports, and scanning.
- Make repositories Firebase-ready by using interfaces and abstraction.

Output expected:

- Domain models
- Repository interfaces
- Repository implementations
- Mapper classes
- Use cases

---

## Agent 4: Barcode Scanner Agent

Responsibility:

- Implement barcode scanning using CameraX or ML Kit.
- Handle camera permission.
- Create scanner UI.
- Add flashlight toggle.
- Add vibration or beep feedback.
- Support scanner modes:
  - Product registration
  - Sale transaction
- Check scanned barcode from RoomDB.
- Navigate properly after scan.

Output expected:

- BarcodeScannerScreen
- ScannerViewModel
- Camera permission handler
- Barcode analyzer
- Scanner mode logic

---

## Agent 5: POS Transaction Agent

Responsibility:

- Build the POS sales flow.
- Add product search.
- Add barcode-to-cart feature.
- Manage cart items.
- Compute subtotal, discount, grand total, cash received, and change.
- Validate stock availability.
- Save SaleEntity and SaleItemEntity.
- Deduct product stock after sale.
- Create stock movement records.

Output expected:

- POS screen
- Cart logic
- Checkout screen
- Sale processing use case
- Stock deduction logic
- Receipt generation logic

---

## Agent 6: UI/UX Designer Agent

Responsibility:

- Improve all Jetpack Compose screens visually.
- Use Material 3 design.
- Create modern POS dashboard cards.
- Create clean product cards.
- Create scanner UI frame.
- Create receipt UI.
- Create reports UI.
- Make the interface mobile-first and cashier-friendly.
- Add loading, empty, success, and error states.

Design rules:

- Use consistent spacing.
- Use rounded cards.
- Use clear visual hierarchy.
- Use readable typography.
- Use large buttons for cashier actions.
- Use color-coded inventory indicators.

Output expected:

- Polished Compose UI screens
- Reusable UI components
- App theme
- Material 3 color scheme
- Better empty and error states

---

## Agent 7: Testing and Debugging Agent

Responsibility:

- Test Room database operations.
- Test product CRUD.
- Test duplicate barcode prevention.
- Test barcode scanning flow.
- Test POS checkout.
- Test stock deduction.
- Test receipt generation.
- Test low stock reports.
- Check for crashes and state bugs.
- Fix compile errors.
- Fix broken navigation routes.
- Fix ViewModel state issues.

Output expected:

- Unit tests where possible
- Manual testing checklist
- Debug report
- Fixed bugs
- Stable build

Testing checklist:

```txt
[ ] App launches successfully
[ ] Dashboard loads data
[ ] Product can be added manually
[ ] Product can be added with barcode
[ ] Duplicate barcode is blocked
[ ] Product can be edited
[ ] Product can be deleted
[ ] Scanner opens with permission handling
[ ] Existing barcode adds product to cart
[ ] Unknown barcode opens Add Product screen
[ ] Cart quantity can increase and decrease
[ ] Checkout blocks insufficient stock
[ ] Checkout blocks invalid cash amount
[ ] Sale completes successfully
[ ] Stock is deducted after sale
[ ] Receipt displays correct data
[ ] Sales history displays transactions
[ ] Reports display correct totals
[ ] App works offline
```

---

## Agent 8: Firebase-Ready Integration Agent

Responsibility:

- Prepare Firebase-ready architecture only.
- Do not replace RoomDB.
- Add TODO files for Firebase Authentication, Firestore, and Storage.
- Create placeholder remote data source interfaces.
- Create sync manager skeleton.
- Add sync status fields.
- Add conflict resolution notes.
- Add WorkManager sync plan for future use.

Output expected:

- Firebase-ready folders
- Remote DTO classes if needed
- Firebase data source interfaces
- SyncManager skeleton
- FIREBASE_READY_PLAN.md

Important rule:

Do not make Firebase required for the app to run. The app must still work offline using RoomDB only.

---

## Agent 9: Documentation Agent

Responsibility:

- Create README.md.
- Explain setup instructions.
- Explain app architecture.
- Explain RoomDB-first design.
- Explain future Firebase integration.
- Explain barcode scanner flow.
- Explain POS transaction flow.
- Add screenshots section placeholder.
- Add developer notes.

Output expected:

- README.md
- FIREBASE_READY_PLAN.md
- TESTING_CHECKLIST.md
- ARCHITECTURE.md

---

## Agent 10: Code Review and Refactor Agent

Responsibility:

- Review generated code.
- Remove duplicated logic.
- Ensure business logic is not inside Composables.
- Ensure ViewModels manage state properly.
- Ensure repositories are used correctly.
- Ensure naming is consistent.
- Ensure code is beginner-friendly and maintainable.
- Suggest improvements but do not break working features.

Output expected:

- Refactored code
- Code review notes
- Cleaner structure
- Improved naming
- Reduced duplication

---

# 16. Agent Collaboration Rules

All agents must follow these rules:

1. Work in parallel but do not overwrite each other’s files without checking responsibility.
2. RoomDB is the first and required backend.
3. Firebase must only be prepared, not required.
4. UI code must not contain database logic.
5. Business logic must be placed in ViewModels, UseCases, and Repositories.
6. Every feature must support offline usage.
7. Every important function must handle errors.
8. Every screen must have loading and empty states where applicable.
9. Every transaction must preserve inventory accuracy.
10. The final app must compile and run.

---

# 17. Suggested Parallel Execution Plan

Run the agents in this order while allowing parallel work:

## Phase 1: Foundation

Agents working together:

- Project Architect Agent
- Room Database Agent
- Domain and Repository Agent

Goal:

Create the base architecture, database, entities, repositories, and use cases.

---

## Phase 2: Core Features

Agents working together:

- Product Management work from Domain and Repository Agent
- Barcode Scanner Agent
- POS Transaction Agent
- UI/UX Designer Agent

Goal:

Build product management, scanning, cart, checkout, and receipt features.

---

## Phase 3: Reports and Inventory

Agents working together:

- Room Database Agent
- POS Transaction Agent
- UI/UX Designer Agent
- Testing and Debugging Agent

Goal:

Build inventory tracking, stock movement, sales history, and reports.

---

## Phase 4: Firebase-Ready Preparation

Agents working together:

- Firebase-Ready Integration Agent
- Documentation Agent
- Code Review and Refactor Agent

Goal:

Prepare future Firebase integration without breaking offline-first RoomDB behavior.

---

## Phase 5: Testing, Debugging, and Polish

Agents working together:

- Testing and Debugging Agent
- UI/UX Designer Agent
- Code Review and Refactor Agent
- Documentation Agent

Goal:

Fix errors, polish UI, complete documentation, and ensure stable build.

---

# 18. Final Output Expected

Generate complete, working Kotlin Jetpack Compose code.

The app must be able to:

- Add products
- Scan barcode
- Search products
- Add product to cart
- Complete sale
- Deduct inventory
- Save transaction
- View receipt
- View sales history
- View reports
- Work offline using RoomDB
- Be ready for Firebase database integration later

Also generate:

- README.md
- ARCHITECTURE.md
- TESTING_CHECKLIST.md
- FIREBASE_READY_PLAN.md

---

# 19. Important Final Instruction

Do not implement Firebase first.

Build this in the following priority:

1. Offline RoomDB version
2. Product management
3. Barcode scanning
4. POS cart and checkout
5. Receipt and sales history
6. Inventory and reports
7. Firebase-ready preparation
8. UI polish
9. Testing and debugging
10. Documentation

The application must be stable, clean, offline-first, and easy to extend.
