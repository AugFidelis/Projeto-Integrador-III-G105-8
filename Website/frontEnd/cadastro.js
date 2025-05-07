// Espera a página carregar tudo antes de rodar o JS
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('formCadastro');
  
    form.addEventListener('submit', function(event) {
      event.preventDefault(); // evita que o form recarregue a página
  
      const dadosCadastro = {
        email: document.getElementById('email').value,
        senha: document.getElementById('senha').value,
        telefone: document.getElementById('telefone').value
      };
  
      fetch('http://localhost:3000/cadastrar', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosCadastro)
      })
      .then(response => {
        if (response.ok) {
          alert('Usuário cadastrado com sucesso!');
          window.location.href = 'index.html'; // redireciona para a página de login
        } else {
          alert('Erro ao cadastrar usuário.');
        }
      })
      .catch(error => {
        console.error('Erro:', error);
        alert('Erro ao conectar com o servidor.');
      });
    });
  });