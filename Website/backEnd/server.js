//Imports
const express = require('express');
const cors = require('cors');
const admin =require('firebase-admin');
require('dotenv').config();

// Inicializa o app
const app = express();
const PORT = process.env.PORT || 3000;

// Middleware para receber JSON
app.use(cors());
app.use(express.json());

// Inicializa o Firebase Admin
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const usuariosCollection = db.collection('usuarios');

// Rota para cadastro
app.post('/cadastrar', async (req, res) => {
  const { email, senha, telefone } = req.body;

  try {
    // Salva no Firestore
    await usuariosCollection.add({
      email,
      senha,
      telefone
    });
    res.status(201).send('Usuário cadastrado com sucesso!');
  } catch (error) {
    console.error('Erro ao cadastrar usuário:', error);
    res.status(500).send('Erro ao cadastrar usuário.');
  }
});

// Rota para login
app.post('/login', async (req, res) => {
  const { email, senha } = req.body;

  try {
    // Busca no Firestore
    const snapshot = await usuariosCollection.where('email', '==', email).where('senha', '==', senha).get();

    if (snapshot.empty) {
      return res.status(401).send('Email ou senha incorretos!');
    }

    res.status(200).send('Login realizado com sucesso!');
  } catch (error) {
    console.error('Erro no login:', error);
    res.status(500).send('Erro ao realizar login.');
  }
});

// Inicia o servidor
app.listen(PORT, () => {
  console.log(`Servidor rodando na porta ${PORT}`);
});