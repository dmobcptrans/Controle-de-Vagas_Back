# Relatório Técnico de Segurança do Backend

## 1. Introdução

Este documento descreve as medidas de segurança implementadas no backend da aplicação, com o objetivo de garantir a **confidencialidade, integridade e disponibilidade das informações** manipuladas pelo sistema.

A arquitetura de segurança adotada segue boas práticas modernas de desenvolvimento de APIs e recomendações amplamente utilizadas na indústria para proteção de aplicações web.

O backend da aplicação é responsável por:

- Autenticação e autorização de usuários
- Processamento das regras de negócio
- Comunicação com o banco de dados
- Proteção de dados sensíveis
- Gerenciamento de notificações e serviços internos

Este documento apresenta os principais mecanismos utilizados para garantir a segurança da aplicação.

---

# 2. Arquitetura de Segurança da Aplicação

A aplicação utiliza uma arquitetura baseada em **API REST**, onde o backend atua como intermediário entre o cliente (frontend) e os serviços de dados.

A arquitetura foi projetada com múltiplas camadas de segurança, incluindo:

- Autenticação baseada em token
- Criptografia de dados sensíveis
- Validação e sanitização de entradas
- Controle de acesso por usuário autenticado
- Integridade de dados no banco
- Comunicação criptografada via HTTPS

Essas camadas reduzem significativamente riscos de acesso indevido e exploração de vulnerabilidades.

---

# 3. Autenticação de Usuários

A autenticação da aplicação garante que apenas usuários válidos possam acessar funcionalidades protegidas do sistema.

O processo ocorre da seguinte forma:

1. O usuário envia suas credenciais (email e senha).
2. O backend valida as informações fornecidas.
3. Caso válidas, é gerado um **token de autenticação**.
4. Esse token deve ser enviado em requisições subsequentes para acessar rotas protegidas.

Esse mecanismo permite identificar o usuário autenticado em cada requisição e aplicar regras de autorização adequadas.

---

# 4. Proteção de Senhas

As senhas dos usuários nunca são armazenadas em formato de texto puro no banco de dados.

Para proteção das credenciais é utilizada a biblioteca **bcrypt**, responsável por gerar um **hash criptográfico seguro da senha**.

## bcrypt

O bcrypt é um algoritmo de hashing adaptativo projetado especificamente para armazenamento seguro de senhas.

Características:

- Geração automática de **salt criptográfico**
- Resistência a ataques de **força bruta**
- Possibilidade de ajuste do custo computacional (cost factor)

Fluxo de armazenamento de senha:

1. O usuário define uma senha.
2. A senha é processada pelo algoritmo bcrypt.
3. O hash resultante é armazenado no banco de dados.

Durante o login, a senha fornecida é novamente processada e comparada com o hash armazenado.

---

# 5. Criptografia de Dados Sensíveis

Algumas informações armazenadas no sistema são classificadas como **dados sensíveis**, incluindo:

- CPF
- Número da CNH
- Telefones
- Outros dados pessoais identificáveis

Esses dados são criptografados antes de serem armazenados no banco de dados.

## AES-256-GCM

Para a criptografia de dados sensíveis é utilizado o algoritmo **AES-256-GCM (Advanced Encryption Standard - Galois/Counter Mode)**.

Características:

- Algoritmo de criptografia simétrica amplamente utilizado
- Chave de 256 bits
- Modo GCM que oferece **confidencialidade e autenticação dos dados**
- Alta performance e forte segurança criptográfica

Processo de armazenamento:

1. O dado sensível é recebido pela aplicação.
2. Ele é criptografado utilizando AES-256-GCM.
3. O valor criptografado é armazenado no banco de dados.

Esse mecanismo garante que, mesmo em caso de acesso indevido ao banco de dados, os dados não possam ser interpretados sem a chave criptográfica.

---

# 6. Hash de Dados com SHA-256

Para determinadas operações de segurança e verificação de integridade de dados, é utilizado o algoritmo **SHA-256 (Secure Hash Algorithm 256 bits)**.

Características:

- Função de hash criptográfico
- Produz uma saída de 256 bits
- Amplamente utilizado em sistemas de segurança
- Alta resistência a colisões conhecidas

Usos comuns dentro da aplicação incluem:

- Geração de identificadores seguros
- Verificação de integridade de dados
- Processos de validação de informações sensíveis

---

# 7. Validação de Dados de Entrada

Todas as requisições recebidas pela API passam por processos de **validação de dados** antes de serem processadas.

As validações incluem:

- Verificação de tipos de dados
- Campos obrigatórios
- Limitação de tamanho de campos
- Validação de formatos específicos (CPF, telefone, etc.)

Essas medidas ajudam a prevenir:

- Injeção de código malicioso
- Dados inconsistentes no banco
- Exploração de vulnerabilidades da aplicação

---

# 8. Proteção contra SQL Injection

O acesso ao banco de dados é realizado por meio de um **ORM (Object Relational Mapping)**.

Essa abordagem evita a construção manual de queries SQL e utiliza **consultas parametrizadas**, reduzindo significativamente riscos de ataques de **SQL Injection**.

---

# 9. Controle de Acesso

A aplicação implementa um sistema de **controle de acesso baseado no usuário autenticado**.

Cada requisição autenticada contém informações que permitem identificar o usuário responsável pela operação.

Isso garante que:

- Usuários só possam acessar seus próprios dados
- Operações sensíveis sejam protegidas
- Ações possam ser auditadas posteriormente

---

# 10. Comunicação Segura

Toda comunicação entre cliente e servidor ocorre através do protocolo **HTTPS**, que utiliza criptografia baseada em **TLS (Transport Layer Security)**.

Isso garante:

- Criptografia dos dados em trânsito
- Proteção contra interceptação de informações
- Integridade das requisições

---

# 11. Integridade de Dados no Banco

O banco de dados possui **restrições de integridade** para garantir consistência das informações.

Exemplo:

- Tokens de notificação possuem restrição de unicidade para evitar duplicidade de registros.

Essas regras são implementadas através de **constraints no banco de dados**.

---

# 12. Boas Práticas de Segurança Aplicadas

Entre as boas práticas implementadas no backend destacam-se:

- Hash seguro de senhas utilizando bcrypt
- Criptografia de dados sensíveis com AES-256-GCM
- Uso de SHA-256 para integridade e hashing
- Validação rigorosa de dados de entrada
- Prevenção contra SQL Injection através de ORM
- Controle de acesso baseado em autenticação
- Comunicação segura via HTTPS
- Restrições de integridade no banco de dados

---

# 13. Conclusão

A arquitetura de segurança do backend foi projetada para proteger informações sensíveis e garantir a confiabilidade da aplicação.

A combinação de técnicas como **hash seguro de senhas, criptografia de dados, autenticação robusta e validação de entradas** reduz significativamente riscos de vulnerabilidades e acesso indevido às informações.

Essas práticas seguem padrões amplamente utilizados na indústria de desenvolvimento de software e contribuem para a construção de um sistema seguro e confiável.