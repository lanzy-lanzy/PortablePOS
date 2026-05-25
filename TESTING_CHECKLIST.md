# Testing Checklist

## App Launch
- [X] App launches successfully
- [X] Splash screen shows app branding
- [X] Navigation to dashboard works

## Dashboard
- [ ] Dashboard loads data
- [ ] Total sales today displays correctly
- [ ] Transaction count shows
- [ ] Low stock count updates
- [ ] Total products count
- [ ] Quick action buttons navigate correctly

## Product Management
- [ ] Product can be added manually
- [ ] Product can be added with barcode
- [ ] Duplicate barcode is blocked
- [ ] Product can be edited
- [ ] Product can be deleted
- [ ] Product search works
- [ ] Category filter works
- [ ] Form validation shows errors

## Barcode Scanner
- [ ] Scanner opens with permission handling
- [ ] Camera permission request works
- [ ] Permission denied handled gracefully
- [ ] Barcode scanning works
- [ ] Flashlight toggle works
- [ ] Vibration feedback on scan
- [ ] Existing barcode adds product to cart (SALE mode)
- [ ] Unknown barcode shows dialog (SALE mode)
- [ ] Existing barcode shows error (PRODUCT_REGISTRATION mode)
- [ ] Unknown barcode navigates to Add Product (PRODUCT_REGISTRATION mode)

## POS / Cart
- [ ] Product search works
- [ ] Products can be added to cart
- [ ] Quantity can be increased/decreased
- [ ] Items can be removed from cart
- [ ] Subtotal calculates correctly
- [ ] Discount applies correctly
- [ ] Grand total is correct

## Checkout
- [ ] Checkout blocks empty cart
- [ ] Checkout blocks insufficient stock
- [ ] Checkout blocks invalid cash amount
- [ ] Change amount calculates correctly
- [ ] Sale completes successfully
- [ ] Stock is deducted after sale
- [ ] Stock movement records created

## Receipt
- [ ] Receipt displays correct data
- [ ] Store name shows
- [ ] Transaction number shows
- [ ] Date/time shows
- [ ] Items list shows
- [ ] Totals are correct
- [ ] Cash/change shows
- [ ] Share button works
- [ ] New Sale button navigates correctly

## Sales History
- [ ] Transaction list displays
- [ ] Search by transaction number works
- [ ] Date filter works
- [ ] Transaction details load
- [ ] Receipt can be viewed again

## Inventory
- [ ] Product list shows with stock info
- [ ] Low stock filter works
- [ ] Out of stock filter works
- [ ] Stock color indicators work (green/orange/red)
- [ ] Stock adjustment works

## Stock Adjustment
- [ ] Current stock displays
- [ ] STOCK_IN adjustment works
- [ ] STOCK_OUT adjustment works
- [ ] ADJUSTMENT works
- [ ] Negative stock is blocked
- [ ] Reason is recorded
- [ ] Stock movement record created

## Reports
- [ ] Today's sales displays
- [ ] This week sales displays
- [ ] This month sales displays
- [ ] Transaction count
- [ ] Gross sales
- [ ] Total discounts
- [ ] Low stock products list

## Settings
- [ ] Store name can be changed
- [ ] Cashier name can be changed
- [ ] Settings persist after app restart

## Offline
- [ ] App works in airplane mode
- [ ] All data loads from RoomDB
- [ ] CRUD operations work offline
