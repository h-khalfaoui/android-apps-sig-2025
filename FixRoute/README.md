FixRoute 🚨
FixRoute is an Android application for location-based claim management, specifically designed to report road-related issues such as potholes, cracks, or other defects. It allows users to submit claims with images and detailed descriptions. Administrators can view these claims and then accept or reject them. Users also have the ability to preview accepted claims. Additionally, administrators receive notifications for each new claim submitted.

Team Members
Moutamanni Abdourrahmane
Ben aata Saad Eddine
Bellali Hafsa
Assila Hajar

Supervision
Supervised by Mrs. Khalfaoui Hafida

📦 Project Structure
FixRoute/
├── app/
│ ├── src/
│ │ ├── main/
│ │ │ ├── java/
│ │ │ │ └── com/
│ │ │ │ └── example/
│ │ │ │ └── fixroute/
│ │ │ │ ├── activities/
│ │ │ │ │ ├── MainActivity.java
│ │ │ │ │ ├── LoginActivity.java
│ │ │ │ │ ├── SignUpActivity.java
│ │ │ │ │ ├── HomeActivity.java
│ │ │ │ │ ├── AdminDashboardActivity.java
│ │ │ │ │ ├── ViewClaimsActivity.java
│ │ │ │ │ ├── ClaimActivity.java
│ │ │ │ │ └── FixRouteDetailsActivity.java
│ │ │ │ ├── database/
│ │ │ │ │ └── SQLiteHelper.java
│ │ │ │ ├── models/
│ │ │ │ │ └── FixRouteData.java
│ │ │ │ └── utils/
│ │ │ │ └── NotificationHelper.java
│ │ │ ├── res/
│ │ │ │ ├── layout/
│ │ │ │ ├── drawable/
│ │ │ │ ├── values/
│ │ │ │ └── mipmap/
│ │ │ └── AndroidManifest.xml
│ │ └── test/
│ └── build.gradle
└── README.md

🌟 Features
🗺️ User (HomeActivity)
Display accepted claims on a map.
Submit new claims with image and description.
Preview accepted claims.
Secure logout.

👨‍💼 Administrator (AdminDashboardActivity)
View pending claims.
Display claims on the map.
Receive notifications for each new claim.
Secure logout.

🛠️ Prerequisites
Android Studio (Arctic Fox or newer)
Java 11+
Google Maps API (API key required)

🚀 Installation
Configure Google Maps API key:

Replace AIzxxxxxxxxxxxxxxxxxxxxxx in AndroidManifest.xml with your API key.
Launch the application:

Import the project into Android Studio.
Run the application on an emulator or physical device.

📚 User Guide
🔑 Authentication
MainActivity: Home page
LoginActivity: User login
SignUpActivity: User registration
Administrator Account:
Email: admin@alert.com
Password: admin

📍 Claims
HomeActivity: Interactive map to send claims.
ClaimActivity: Create claims with images and description.
AdminDashboardActivity: Claim management for administrators.
ViewClaimsActivity: Detailed list of claims for validation.

📂 Data Structure
Claims Table:
id: Unique identifier
location: GPS coordinates
description: Text describing the issue
image: Associated image (Blob)
status: Claim status (pending, accepted, rejected)

📬 Notifications
Administrators receive a notification for each new claim sent by users.
Uses NotificationHelper.java to send notifications locally.