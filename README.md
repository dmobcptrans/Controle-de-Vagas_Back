# 🚌🚦 Projeto: PetroCarga – Mobilidade Inteligente

> Uma plataforma inovadora desenvolvida para otimizar a gestão de vagas de carga e descarga na cidade de Petrópolis.
> Conectando empresas, motoristas e a CPTrans, o PetroCarga digitaliza o processo de agendamento e monitoramento de vagas, promovendo mais eficiência e organização no trânsito urbano. 🚗💨

---

## 🗺️ Índice

* [🚀 Sobre o Projeto](#-sobre-o-projeto)
* [🧰 Tecnologias Utilizadas](#-tecnologias-utilizadas)
* [🏗️ Arquitetura do Sistema](#️-arquitetura-do-sistema)
* [🔐 Segurança](#-segurança)
* [🔔 Notificações](#-notificações)
* [⚙️ Instalação e Execução](#️-instalação-e-execução)
* [📖 Swagger](#-swagger)

---

## 🚀 Sobre o Projeto

O **PetroCarga** é um projeto desenvolvido por **residentes STEM do Serratec**, em parceria com a **CPTrans**, com o objetivo de resolver um problema real enfrentado pela cidade de **Petrópolis**:
o gerenciamento manual e ineficiente das **vagas de carga e descarga**.

Através dessa plataforma, é possível:

* 📅 Realizar o agendamento digital de vagas
* 📊 Monitorar utilização em tempo real
* 👤 Gerenciar usuários e permissões
* 🔔 Receber notificações em tempo real e push
* 📧 Enviar emails automáticos (ativação, recuperação de senha, etc)

> 💡 Uma solução moderna para uma cidade mais organizada e inteligente.

---

## 🧰 Tecnologias Utilizadas

### ⚙️ Backend

* ☕ Java 21
* 🌱 Spring Boot
* 🔐 Spring Security + JWT
* 🐘 PostgreSQL 17
* 🔄 Flyway

### 🌐 Infraestrutura

* 🐳 Docker & Docker Compose
* 🌐 Nginx (reverse proxy)
* 🔗 Ngrok (testes)

### 📡 Comunicação

* 🔔 SSE (Server-Sent Events) – notificações em tempo real
* 🔔 Firebase – notificações push
* 📧 SMTP / API – envio de emails

### 🧪 Ferramentas

* 📬 Postman
* 🧰 Maven

---

## 🏗️ Arquitetura do Sistema

A aplicação segue uma arquitetura baseada em:

* Clean Architecture
* Domain-Driven Design (DDD)
* Ports & Adapters

### 📂 Estrutura

```bash
application/        # Casos de uso
domain/             # Regra de negócio
infrastructure/     # Integrações e detalhes técnicos
interfaces/         # Controllers REST
shared/             # Código compartilhado
```

### 🔄 Fluxo

```text
Controller → Application → Domain → Infrastructure
```

---

## 🔐 Segurança

A API utiliza autenticação **JWT (stateless)**.

O token pode ser enviado de duas formas:

### 🔹 Header

```http
Authorization: Bearer <token>
```

### 🔹 Cookie

```http
auth-token=<token>
```

---

### 🛡️ Proteções adicionais

* Rate limiting (Nginx)
* Limite de conexões simultâneas
* Headers de segurança HTTP
* Proteção contra brute force
* Controle de CORS

---

## 🔔 Notificações

O sistema suporta múltiplos canais de notificação:

### 📡 Tempo real (SSE)

* Conexão persistente
* Autenticada via JWT
* Heartbeat automático
* Limite de conexões por usuário

---

### 📲 Push Notifications

* Integração com Firebase
* Envio para dispositivos cadastrados

---

### 📧 Emails

* Envio de:

  * Código de ativação
  * Recuperação de senha

---

## ⚙️ Instalação e Execução

### 🧩 Pré-requisitos

* ☕ Java 21
* 🐘 PostgreSQL 17
* 🐳 Docker (opcional)

---

### 🗂️ Configuração

#### 🔹 Banco de dados

Crie um banco chamado:

```bash
petrocarga
```

---

#### 🔹 Variáveis de ambiente

⚠️ O arquivo `.env` é utilizado **apenas no ambiente Docker**.

Algumas variáveis devem ser configuradas diretamente nos arquivos `application.yml` / `application-*.yml`, como:

* JWT secret
* Configurações de email
* Firebase
* URLs do frontend

---

## ▶️ Execução

### 🔹 Local

```bash
mvn spring-boot:run
```

Endpoints:

```bash
http://localhost:8080/petrocarga/*
```

---

### 🔹 Docker

```bash
docker-compose up --build
```

Endpoints:

```bash
https://localhost/petrocarga/*
```

---

### 🔹 Ngrok (ambiente de teste)

```bash
ngrok http 443
```

---

## 📖 Swagger

A documentação da API pode ser acessada em:

* Local:

```bash
http://localhost:8080/petrocarga/swagger-ui/index.html
```

* Docker:

```bash
https://localhost/petrocarga/swagger-ui/index.html
```

---

## 📌 Observações

* O ngrok é utilizado apenas para testes
* Em produção, recomenda-se uso de domínio próprio e proxy seguro ou serviços de nuvem
* Algumas funcionalidades dependem de configurações externas (email, Firebase)

---

## 📜 Licença

MIT
