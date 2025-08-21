# PahanaEduSystem

This is a Java web application for PahanaEdu Bookshop for managing inventory, billing, and customers from there end
## 📋 Overview

PahanaEdu Bookshop System is a web-based . The application follows a modern MVC (Model-View-Controller) architecture and is built using Java technologies.

## 🚀 Features

- **Customer Management**: Handle user roles and permissions
- **Dashboard**: Overview of system metrics and activities
- **Item Management**: Manage inventory and products
- **Billing**: Generate invoices and track sales
- **Bill Management**: Manage orders and order details
- **Responsive Design**: Works on desktop and mobile devices

## 🛠️ Tech Stack

- **Backend**: Java 24, Maven, Mysql
- **Frontend**: HTML, CSS, JavaScript, Bootstrap
- **Testing**: JUnit 4
- **Build Tool**: Maven

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) 24 or later
- Apache Maven 3.6.0 or later
- A modern web browser (Chrome, Firefox, Safari, Edge)

### Installation

1. Clone the repository:
   ```bash
   git clone <https://github.com/MushTheProgrammer/Pahana-Edu-Web.git>
   ```

2. Navigate to the project directory:
   ```bash
   cd PahanaEduSystem
   ```

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Deploy the generated WAR file to your preferred Java application server (e.g., Tomcat, WildFly, etc.)

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/org/pahanaedu/
│   │   ├── controller/    # Request handlers
│   │   ├── dao/           # Data Access Objects
│   │   ├── model/         # Data models
│   │   ├── service/       # Business logic
│   │   └── util/          # Utility classes
│   └── webapp/            # Web application resources
│       ├── assets/        # Static assets (images, etc.)
│       ├── js/            # JavaScript files
│       └── WEB-INF/       # Web application configuration
└── test/                  # Test files
```

## 🧪 Testing

Run the test suite using Maven:

```bash
mvn test
```

## 📄 License

This project is licensed under the terms of the MIT License. See the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📧 Contact

For any inquiries, please open an issue in the repository.
