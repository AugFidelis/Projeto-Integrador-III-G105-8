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

### Protótipo

Link para o protótipo no figma: https://www.figma.com/design/1Z5GHg0xvMkk7qxFhQr74p/Aplicativo-SuperID---G105-8?node-id=0-1&t=xX35lx8ZxYUuBUku-1

### Instalação 

Como instalar:

- Acesse a seção de Releases deste repositório.
- Baixe o arquivo APK disponível na versão mais recente (SuperID-v1.0.0.apk).
- Transfira o APK para seu dispositivo Android ou abra diretamente no celular.
- Permita a instalação de apps de fontes externas, caso solicitado.
- Instale o aplicativo normalmente.Release v1.0.0 - SuperID

### Como Usar

Após instalar o aplicativo, siga os passos abaixo para utilizar o SuperID:

Cadastro e Login:
- Abra o aplicativo e crie uma nova conta com seu email e senha mestre.
- Um email de verificação será enviado. Confirme-o antes de utilizar as funções principais.

Gerenciamento de Senhas:
- Acesse a aba de senhas e selecione uma das categorias: Web, Aplicativos ou Acessos Físicos.
- Adicione uma nova senha com nome, descrição e nível de segurança.
- Cada senha recebe um token de autenticação exclusivo (accessToken).

Autenticação com QR Code:
- Acesse o site parceiro do SuperID.
- Na página de login, escaneie o QR Code usando o app.
- A autenticação será processada automaticamente, sem necessidade de digitar senha.

Recuperação de Senha Mestre:
- Caso esqueça sua senha mestre, utilize a opção de redefinição na tela de login.
- Um link de recuperação será enviado para seu email.

### Participantes

- Augusto Fidélis dos Santos Custódio - 24024320
- Caio Adamo Scomparin - 23028248
- Hugo Daniel Bosada Rodrigues - 23909526
