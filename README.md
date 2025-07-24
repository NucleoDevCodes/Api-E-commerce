# Api-E-commerce

API RESTful de um sistema de e‑commerce, desenvolvida com Spring Boot, que fornece endpoints para gerenciamento de usuários, produtos, carrinho de compras, favoritos, pedidos e pagamentos.

---

## 🚀 Tecnologias

- **Java 17+**  
- **Spring Boot 3.2**  
  - Spring Web  
  - Spring Data JPA  
  - Spring Security  
- **Hibernate (JPA)**  
- **MySQL 8**  
- **Redis** (cache)  
- **Maven Wrapper** (`mvnw`)  
- **Docker & Docker Compose**  
- **Lombok**  
- **JUnit + Spring Security Test**

---

## 🏗️ Estrutura do Projeto
├── Dockerfile
<br>
├── docker-compose.yml
<br>
├── mvnw
<br>
├── mvnw.cmd
<br>
├── pom.xml
<br>
├── src
<br>
│ └── main
<br>
│ ├── java/com/ecommerce
<br>
│ │ ├── ApiecommerceApplication.java 
<br>
│ │ ├── aplication
<br>
│ │ │ ├── records/…
<br>
│ │ │ ├── services/… 
<br>
│ │ │ └── security/… 
<br>
│ │ └── infra
<br>
│ │ ├── controllers/… 
<br>
│ │ └── exceptions/… 
<br>
│ └── resources
<br>
│ └── application-prod.properties 
<br>
├── application.jar
<br>
└── LICENSE
<br>
