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

---

## 🔧 Pré-requisitos

- **JDK 17** ou superior  
- **Maven** (ou usar o `mvnw`)  
- **Docker** & **Docker Compose**

---

## ⚙️ Configuração

### 1. Banco de dados

Crie um banco MySQL (8+) e um usuário com permissão de leitura e escrita. Defina as variáveis de ambiente:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://<HOST>:<PORT>/<DB_NAME>
export SPRING_DATASOURCE_USERNAME=<DB_USER>
export SPRING_DATASOURCE_PASSWORD=<DB_PASS>
````

