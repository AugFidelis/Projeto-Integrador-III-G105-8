// Adiciona um ouvinte de evento para o formulário de login
document.getElementById('loginForm').addEventListener('submit', async (event) => {
    event.preventDefault(); // Impede o comportamento padrão do formulário
  
    // Captura os dados de email e senha inseridos pelo usuário
    const dadosLogin = {
      email: document.getElementById('email').value,
      senha: document.getElementById('senha').value
    };
  
    try {
      // Envia uma requisição POST para o servidor 
      const response = await fetch('http://localhost:3000/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json' // Informa que o corpo da requisição é JSON
        },
        body: JSON.stringify(dadosLogin) // Converte o objeto de login em JSON
      });
  
      // Verifica a resposta do servidor
      if (response.ok) {
        // Se login for bem-sucedido, mostra um alerta e redireciona para a página de boas-vindas
        alert('Login realizado com sucesso!');
        window.location.href = 'bemvindo.html';
      } else {
        // Se houver erro no login, mostra a mensagem de erro retornada
        const errorMsg = await response.text();
        alert('Erro no login: ' + errorMsg);
      }
    } catch (error) {
      console.error('Erro:', error);
      alert('Erro ao tentar fazer login.');
    }
  });
  