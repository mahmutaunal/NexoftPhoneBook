**📱 Nexoft Contacts -- Jetpack Compose Case Study**
====================================================
Modern - Lightweight - Clean Architecture

<p align="center">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Modern%20UI-blue" />
  <img src="https://img.shields.io/badge/Clean%20Architecture-%F0%9F%94%8D-green" />
  <img src="https://img.shields.io/badge/MinSDK-28-orange" />
  <img src="https://img.shields.io/badge/State%20Management-Event%20%2F%20State-purple" />
</p>

This project was developed as a **case study for Nexoft Mobile** and represents a fully production-ready, lightweight, and optimized **Phonebook Application** built with modern Android development tools.

The application follows **Jetpack Compose**, **Clean Architecture**, **MVVM**, and includes smooth UI interactions, device contact integration, robust caching, image optimization, and proper performance enhancements.

---

**🚀 Features**
===============
### **📇 Contact Management**
-   List contacts (grouped alphabetically)
-   View contact details
-   Create / Edit contact
-   Delete contact with confirmation sheet

### **🔍 Advanced Search**
-   Real-time search with debounce
-   Search history (auto-suggest)
-   Interaction patterns matching UX designs exactly
-   Smooth animations and transitions

### **📷 Profile Photo Handling**
-   Select from camera or gallery
-   Image downscaling & compression before upload
-   Coil caching (memory + disk)
-   Upload optimized image to server

### **📱 Device Contacts Integration**
-   Save contact into the device's native contact list
-   Full permission handling for all Android versions (API 28+)
-   Detect if a backend contact exists in device contacts (name + surname + phone matching)
-   Show a dedicated badge for device-saved contacts

### **💾 Data Management**
-   Remote API (Retrofit)
-   Local cache (Room)
-   Offline-first approach
-   Clear mapper separation (DTO ↔ Entity ↔ Domain)

### **🎨 Jetpack Compose UI**
-   Material 3 components
-   Custom swipe actions (Edit / Delete)
-   Success toast message
-   Lottie animations
-   Glow effect on profile photos (Palette API)
-   Responsive, clean, modern UI

---

**🧱 Architecture Overview -- Clean Architecture**
=================================================
This project strictly follows **Clean Architecture** principles:
### **Presentation Layer**
-   Implemented with Jetpack Compose
-   Pure Event → ViewModel → State flow
-   UI reacts only to immutable state

### **Domain Layer**
-   Contains business logic and use cases
-   Repository interfaces hide data sources
-   Fully testable, independent core

### **Data Layer**
-   Retrofit for networking
-   Room for local caching
-   DeviceContactsManager for native contacts API
-   Mappers for data transformations