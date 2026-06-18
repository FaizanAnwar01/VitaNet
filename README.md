# VitaNet — Blood Donation Management System

<div align="center">
  <img src="https://img.shields.io/badge/Java-21+-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/Swing-GUI-007396?style=for-the-badge&logo=java&logoColor=white" alt="Swing"/>
</div>

<br/>

VitaNet is a comprehensive, pure Java desktop application built to bridge the gap between blood donors, receivers, and blood bank administrators. Originally conceptualized as a web application (MERN stack), it has been entirely re-architected into a robust Object-Oriented Java application to demonstrate core software engineering principles such as **Design by Contract (DbC)**, **Immutability**, and **Concurrency Control**.

## Features

* **Role-Based Dashboards:** Dedicated, responsive UI dashboards for Admins, Donors, and Receivers using Java Swing `CardLayout`.
* **Medical Business Rules:** Strictly enforced constraints (e.g., Donors must wait a minimum of 90 days between donations; Blood packets expire after 42 days).
* **Concurrency & Thread Safety:** The central blood inventory is protected against race conditions using `ReentrantLock`. It includes an interactive GUI simulation where multiple hospital threads simultaneously race to claim a single blood packet.
* **Real-time Data Validation:** Utilizes Java Regular Expressions (`Pattern` & `Matcher`) to enforce strict formatting for CNIC, Phone numbers, Email, and Passwords dynamically as the user types.
* **Defensive Programming:** Implements pure Immutable models and defensive copying (`Collections.unmodifiableList()`) to prevent Representation Exposure.

## Requirements

* **Java Development Kit (JDK):** Version 21 or higher.
* No external libraries or dependencies are required. The project is built entirely on pure Java SE and Swing.

## How to Compile and Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/vitanet-github.git
   cd vitanet-github
   ```

2. **Compile the project:**
   Execute the following command from the root of the project to compile all Java source files into an `out` directory:
   ```bash
   javac --release 21 -d out src/main/java/com/vitanet/enums/*.java src/main/java/com/vitanet/model/*.java src/main/java/com/vitanet/contract/*.java src/main/java/com/vitanet/service/*.java src/main/java/com/vitanet/util/*.java src/main/java/com/vitanet/data/*.java src/main/java/com/vitanet/ui/*.java src/main/java/com/vitanet/Main.java
   ```

3. **Run the application:**
   Launch the main GUI frame:
   ```bash
   java -cp out com.vitanet.ui.MainFrame
   ```

### Pre-Seeded Accounts
For testing purposes, you can use the pre-seeded admin account:
* **Email:** `admin@vitanet.com`
* **Password:** `Admin123`

## 📂 Repository Structure

```text
vitanet-github/
├── README.md                      # Project documentation
└── src/
    └── main/
        └── java/
            └── com/
                └── vitanet/
                    ├── Main.java             # Terminal-based test suite
                    ├── contract/             # Java Interfaces for Design by Contract
                    ├── data/                 # Thread-safe in-memory data stores
                    ├── enums/                # Constants (BloodType, UserRole)
                    ├── model/                # Core immutable Entities (User, Donor, Packet)
                    ├── service/              # Core business logic implementations
                    ├── ui/                   # Java Swing GUI Panels and Theme system
                    └── util/                 # Regex utilities and input validation
```

