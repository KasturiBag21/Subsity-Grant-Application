<div align="center">

# 🌊 SAGAR
### Subsidy & Grant Administration Portal

**A Digital Platform for Transparent, Secure & Efficient Subsidy Management**

<p>
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-Backend-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/MySQL-Database-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/Thymeleaf-Frontend-005F0F?style=for-the-badge" alt="Thymeleaf">
  <img src="https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven">
</p>

</div>

---

## 📌 About the Project

**SAGAR (Subsidy & Grant Administration Portal)** is a web-based application designed to streamline the management of government subsidy and grant programs.

The platform digitizes the subsidy lifecycle, allowing citizens to apply for schemes, submit supporting documents, track application progress, and view disbursement information. Authorized officers can verify applications, review eligibility, process financial approvals, and monitor fund utilization.

The primary goal is to create a **centralized, transparent, and traceable subsidy management system** that improves administrative efficiency and accountability.

## ✨ Key Features

### 👤 Citizen Portal
- Citizen registration, login, and secure authentication.
- Browse available subsidy schemes.
- Submit scheme-specific applications.
- Upload mandatory and optional supporting documents.
- View application details and status.
- Respond to document correction and re-verification requests.
- Track bank verification and disbursement progress where supported.
- Access available application and transaction receipts.

### 📋 Eligibility & Application Management
- Scheme-specific eligibility criteria.
- Live eligibility score calculation.
- Mandatory and optional document management.
- Application validation and status tracking.
- Support for document resubmission and re-verification.

### 🏢 Multi-Level Verification
- **Field Officer:** Reviews applicant information and supporting documents.
- **District Officer:** Performs the next-level application review.
- **Finance Officer:** Reviews financial information, bank verification, and sanction-related details.
- **Disbursement Processing:** Tracks configured milestones and transaction information.

### 💰 Financial Management
- Bank detail submission and verification.
- Sanction and milestone configuration where enabled.
- Milestone-based fund-release tracking.
- Compliance document verification.
- Transaction/UTR reference tracking.
- Overall and scheme-wise fund analytics where supported.

### 🔐 Security & Reliability
- Spring Security authentication and authorization.
- Role-based access control.
- Password hashing with BCrypt.
- Server-side validation of workflow actions.
- Testing for unauthorized access and duplicate operations.

## 🔄 Application Workflow

```text
             CITIZEN REGISTRATION
                      |
                      v
                SCHEME SELECTION
                      |
                      v
           APPLICATION & DOCUMENT UPLOAD
                      |
                      v
             ELIGIBILITY VALIDATION
                      |
                      v
             FIELD OFFICER REVIEW
                      |
           +----------+----------+
           |                     |
           v                     v
    RE-VERIFICATION          FORWARD TO
     IF REQUIRED          DISTRICT OFFICER
           |                     |
           v                     v
    CITIZEN UPDATES         DISTRICT REVIEW
    DOCUMENTS                    |
           |                     v
           +------------> FINANCE REVIEW
                                 |
                                 v
                       BANK VERIFICATION
                                 |
                                 v
                       SANCTION & MILESTONES
                                 |
                                 v
                       COMPLIANCE VERIFICATION
                                 |
                                 v
                         FUND DISBURSEMENT
                                 |
                                 v
                       TRANSACTION GENERATION
                                 |
                                 v
                       RECEIPTS & ANALYTICS
```

*The available actions and transitions depend on the configured application workflow.*

## 🏛️ Supported Subsidy Schemes

The project is configured around the following scheme categories:

| No. | Scheme |
|:---:|---|
| 1 | Education Subsidy |
| 2 | Farmer Support Grant |
| 3 | Housing Assistance Scheme |
| 4 | Women Entrepreneurship Grant |
| 5 | Senior Citizen Assistance |
| 6 | Divyang Disability Support |
| 7 | Rural Skill Development |

Each scheme can define its own eligibility conditions and document requirements. The corresponding upload fields depend on the scheme configuration stored in the database.

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| Programming Language | Java 21 |
| Backend Framework | Spring Boot |
| Web Framework | Spring MVC |
| Security | Spring Security, BCrypt |
| ORM | Hibernate |
| Data Access | Spring Data JPA |
| Frontend | Thymeleaf, HTML, CSS, JavaScript |
| Database | MySQL 8 |
| Build Tool | Maven |
| IDE | IntelliJ IDEA |

## 🏗️ System Architecture

```text
             PRESENTATION LAYER
             Thymeleaf / HTML / CSS
                      |
                      v
              CONTROLLER LAYER
                Spring MVC
                      |
                      v
                SERVICE LAYER
          Eligibility & Business Rules
                      |
                      v
               REPOSITORY LAYER
               Spring Data JPA
                      |
                      v
                DATABASE LAYER
                     MySQL

        SECURITY: Spring Security
        across protected application routes
```

### Architecture Overview

- **Presentation Layer:** Displays citizen and officer interfaces.
- **Controller Layer:** Handles requests, form submissions, and navigation.
- **Service Layer:** Processes eligibility rules and business logic.
- **Repository Layer:** Manages database operations through JPA.
- **Database Layer:** Stores application and scheme-related information.
- **Security Layer:** Controls authentication and access permissions.

## ⚙️ Installation & Setup

### Prerequisites

Install the following software:

- [Java JDK 21](https://adoptium.net/)
- [MySQL](https://dev.mysql.com/downloads/mysql/)
- [Git](https://git-scm.com/)
- IntelliJ IDEA
- Maven, if the Maven Wrapper is unavailable

### 1. Clone the Repository

```bash
git clone <repository-url>
cd <project-folder>
```

If the project is already cloned, update your local copy:

```bash
git checkout master
git pull origin master
```

Coordinate with your team before pushing changes to the `master` branch.

### 2. Create the Database

Start MySQL and execute:

```sql
CREATE DATABASE `government-subsidy-db`;
```

### 3. Configure Database Connection

Open:

`src/main/resources/application.properties`

Verify the database URL, username, password, and server port.

Example configuration:

```properties
spring.application.name=Subsidy-platform-test
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/government-subsidy-db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:root@123}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

**Important:** The fallback database password is intended for the existing local development setup. Configure your own password securely and never commit real credentials to the repository.

For the current PowerShell session:

```powershell
$env:DB_PASSWORD="your-local-mysql-password"
```

### 4. Build the Project

On Windows PowerShell, use the Maven Wrapper if available:

```powershell
.\mvnw.cmd clean package
```

Alternatively:

```powershell
mvn clean package
```

## ▶️ Running the Application

### Option 1: IntelliJ IDEA

1. Open the project in IntelliJ IDEA.
2. Allow Maven dependencies to load.
3. Locate the main Spring Boot application class.
4. Run the main application class, not the JUnit test class.
5. Confirm successful startup in the Run console.

### Option 2: PowerShell

Start MySQL first. Then execute the following command from the project root:

```powershell
.\mvnw.cmd spring-boot:run
```

Or use:

```powershell
mvn spring-boot:run
```

Open the application in your browser:

**http://localhost:8081**

## 🧪 Testing Strategy

The project testing process covers the complete application lifecycle.

- [ ] Application startup and database connectivity.
- [ ] Citizen registration, validation, login, and logout.
- [ ] New application creation.
- [ ] Scheme-specific document upload.
- [ ] Eligibility score calculation.
- [ ] Field Officer verification.
- [ ] Applicant re-verification and document resubmission.
- [ ] District Officer review.
- [ ] Finance review and bank verification.
- [ ] Sanction and milestone configuration.
- [ ] Compliance document verification.
- [ ] Manual and scheduled fund release, where enabled.
- [ ] Transaction reference and receipt generation.
- [ ] Overall and scheme-wise analytics.
- [ ] Unauthorized access and duplicate-action testing.
- [ ] UI responsiveness, navigation, and PDF validation.

**Testing recommendation:** Use a newly registered citizen and a newly created application for the main end-to-end test. Track that application using the same application ID throughout the workflow.

## 🔒 Security Considerations

Security is important because the platform handles citizen information and financial records.

- Enforce role-based access to protected pages and operations.
- Validate important actions on the server, not only in the browser.
- Check application ownership before displaying documents or generating receipts.
- Prevent unauthorized access to other citizens' applications.
- Test repeated approvals and fund-release requests.
- Keep passwords, secrets, and sensitive documents out of source control.
- Use HTTPS, secure configuration, backups, and additional security testing before production deployment.

## 🚀 Future Enhancements

Potential improvements include:

- Integration with authorized government identity systems.
- Integration with treasury and payment services.
- Regional administration and district-level reporting.
- Enhanced audit logs and downloadable reports.
- Automated notifications for application status changes.
- Advanced fraud detection and anomaly monitoring.
- Improved accessibility and mobile responsiveness.
- Expanded automated testing and production monitoring.

*These enhancements should be considered proposed functionality unless they have been implemented and verified.*

## 🤝 Team Collaboration

To maintain a stable shared codebase:

1. Pull the latest agreed version before testing.
2. Use feature branches for development.
3. Test changes locally before sharing them.
4. Discuss changes before pushing directly to `master`.
5. Write meaningful commit messages.
6. Never commit passwords, private documents, or production data.

## 👨‍💻 Project Team

**Project:** SAGAR — Subsidy & Grant Administration Portal

**Repository:** [Subsidy-Application](https://github.com/KasturiBag21/Subsity-Application)

**Developed as a collaborative academic project.**

Mahesh and Akashya

---

<div align="center">
<div align="center">

**SAGAR — From application to accountable fund utilization.**

[Live Website](https://sagar-subsidy-platform.onrender.com) · [GitHub Repository](https://github.com/KasturiBag21/Subsity-Grant-Application)

---

### 🌊 SAGAR
**A structured approach to subsidy administration, verification, and fund tracking.**

*Java · Spring Boot · Thymeleaf · MySQL*

</div>
