# it-helpdesk-ticketing-system
Full-stack IT Help Desk Ticketing System built with Spring Boot, PostgreSQL, and Thymeleaf. Features role-based access, ticket CRUD, priority tracking, and status management for streamlined IT support workflows.


# IT Help Desk Ticketing System

A full-stack web application for managing IT support tickets. Built to help small to mid-size teams track, assign, and resolve internal tech issues efficiently.

### **Key Features**
- **Role-Based Access**: Separate dashboards for Users and Technicians/Admins
- **Ticket Management**: Create, view, update, and close support tickets
- **Priority & Status Tracking**: LOW → URGENT priorities, OPEN → CLOSED workflow
- **Real-time Updates**: Track ticket history with timestamps
- **Responsive UI**: Clean HTML/CSS/JS frontend with Thymeleaf

### **Tech Stack**
| Layer | Technology |
| --- | --- |
| Backend | Java 17, Spring Boot 3.2, Spring Data JPA, Spring Security |
| Database | PostgreSQL |
| Frontend | Thymeleaf, HTML5, CSS3, JavaScript |
| Build Tool | Maven |

### **Quick Start**
1. Clone: `git clone https://github.com/yourusername/it-helpdesk-ticketing-system.git`
2. Create PostgreSQL DB: `CREATE DATABASE helpdesk_db;`
3. Update `src/main/resources/application.properties` with your DB credentials
4. Run: `.\gradlew.bat bootRun`
5. Visit: `http://localhost:8082`

### **Roadmap**
- [ ] Email notifications on ticket updates
- [ ] File attachments for tickets
- [ ] Advanced search + filters
- [ ] REST API for mobile app integration

### **License**
MIT
