# Milk Delivery & Sales Logistics Hub 🥛🚚

A comprehensive Android application designed for dairy farmers and distributors to manage milk sales, delivery routes, and real-time logistics.

## 🚀 Key Features

### 👨‍🌾 Farmer Dashboard
- **Sales Analytics**: Real-time visualization of milk sales volume across different routes using custom bar charts.
- **Order Management**: Track pending, delivered, and cancelled orders with ease.
- **Route Optimizer**: Automatically optimize delivery sequences (sorted by location/address) to reduce travel time.
- **Revenue Tracking**: Monitor expected vs. collected revenue in **Kenyan Shillings (KSh)**.

### 🚛 Driver/Distributor Tools
- **Real-Time Delivery Map**: A dynamic map featuring:
    - **Live Telemetry**: ETA and speed (km/h) tracking.
    - **Traffic Simulation**: Real-time visualization of traffic conditions.
    - **Environmental Effects**: Animated weather/clouds for an immersive experience.
- **Delivery Workflow**: One-tap delivery confirmation with payment method logging (Cash, Mobile Money, etc.).
- **Customer Contact**: Direct dial integration to call customers from the delivery list.

## 🛠 Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room (Offline-first architecture)
- **Concurrency**: Kotlin Coroutines & Flow
- **Architecture**: MVVM (Model-View-ViewModel)

## 📦 Getting Started

### Prerequisites
- [Android Studio Ladybug](https://developer.android.com/studio) or newer.
- Android device or emulator (API 24+).

### Installation
1. **Clone the repository**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/milk-delivery-sales.git
   ```
2. **Open in Android Studio**:
   Select "Open" and navigate to the project folder.
3. **Set up Environment**:
   - Create a `.env` file in the root directory.
   - Add your API keys if necessary (refer to `.env.example`).
4. **Build & Run**:
   Press the **Run** button in Android Studio.

## 💰 Pricing Configuration
The app is currently configured for the Kenyan market:
- **Default Price**: 120 KSh per Litre.
- **Currency**: KSh.

---
Developed as a high-fidelity logistics solution for dairy value chains.
