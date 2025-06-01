# Projeto-Integrador III - Grupo G105-8

## SuperID

**SuperID** é um projeto integrador desenvolvido por alunos da PUC-Campinas no curso de Engenharia de Software. O sistema propõe uma solução educativa de gerenciamento de credenciais, focando em funcionalidades práticas de segurança da informação.

### Visão Geral

O SuperID consiste em um **ecossistema de autenticação sem senhas**, baseado em duas frentes principais:

- **Aplicativo Mobile (Android em Kotlin)**: Responsável pela criação de contas, armazenamento seguro de senhas, e escaneamento de QR Codes para autenticação em sites.
- **Integração com Web (via Firebase Functions)**: Permite que sites parceiros ofereçam login via SuperID, utilizando autenticação sem senha.

### Objetivos do Projeto

- Desenvolver um aplicativo nativo Android com Kotlin.
- Utilizar serviços da Google Cloud, especialmente o **Firebase Authentication**, **Firestore**, e **Firebase Functions**.
- Aplicar conceitos práticos de **segurança da informação**, ainda que em nível introdutório.

### Funcionalidades

- Cadastro de usuários com verificação de email.
- Armazenamento de senhas organizadas por categorias (Web, Aplicativos, Acessos Físicos).
- Geração de tokens únicos (accessToken) para cada senha.
- Integração com sites parceiros para **login via QR Code**, sem uso de senha.
- Recuperação de senha mestre via email (se o email tiver sido previamente validado).

### SITE

O site é um protótipo exemplo da marca SuperID que tem como base simular utilizando o sistema real um login com QR CODE de nossos sites parceiros.
A documentação abaixo é o guia para rodar o site.

- Abra todo o conteúdo do website em uma pasta.
- Suba o frontend no Live Server.
- Execute o comando "npm install -g firebase-tools".
- Após isso, execute o comando "firebase login".
- Em seguida adicione o projeto utilizando o comando "firebase use --add seu_projeto".
- Entre na pasta functions com o comando "cd functions".
- Utilize o firebase deploy para conectar todas as funções a nuvem.
- Rode o npm start.


Link para o protótipo no figma: https://www.figma.com/design/1Z5GHg0xvMkk7qxFhQr74p/Aplicativo-SuperID---G105-8?node-id=0-1&t=xX35lx8ZxYUuBUku-1
