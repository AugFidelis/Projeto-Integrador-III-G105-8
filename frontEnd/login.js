// Integrando o que foi usado.
const generateQrCodeBtn = document.getElementById('generateQrCodeBtn');
const qrcodeDisplayArea = document.getElementById('qrcode-display-area');
const qrcodeImg = document.getElementById('qrcode-img');
const countdownTimerDisplay = document.getElementById('countdown-timer');
const loadingQrMsg = document.getElementById('loading-qr-msg');
const errorQrMsg = document.getElementById('error-qr-msg');

// Configurando o firebase
const FIREBASE_PROJECT_ID = 'projeto-integrador-3-g105-8';
const FUNCTIONS_BASE_URL = 'https://us-central1-projeto-integrador-3-g105-8.cloudfunctions.net';
 

const PERFORM_AUTH_URL = `${FUNCTIONS_BASE_URL}/performAuth`;
const PARTNER_API_KEY = 'minha_chave_secreta';
const PARTNER_URL = 'https://meu.site.parceiro.com';

// Definindo contador
let countdownInterval;
const EXPIRATION_TIME_SECONDS = 60; 

// O contador é iniciado aqui.
function startCountdown() {
    let timeLeft = EXPIRATION_TIME_SECONDS;
    countdownTimerDisplay.textContent = `Expira em: ${timeLeft} segundos`;

    countdownInterval = setInterval(() => {
        timeLeft--;
        if (timeLeft >= 0) {
            countdownTimerDisplay.textContent = `Expira em: ${timeLeft} segundos`;
        } else {
            clearInterval(countdownInterval);
            countdownTimerDisplay.textContent = "QR Code expirado!";
            // Ocultar o QR Code e a área
            qrcodeDisplayArea.style.display = 'none';
            // Opcional: Reativar o botão de gerar QR Code para que o usuário possa gerar um novo
            generateQrCodeBtn.disabled = false; 
            generateQrCodeBtn.textContent = 'Gerar Novo QR Code';
        }
    }, 1000);
}

// Adicionar evento de clique ao botão "Gerar QR Code"
generateQrCodeBtn.addEventListener('click', async () => {
    if (countdownInterval) {
        clearInterval(countdownInterval);
    }

    generateQrCodeBtn.disabled = true;
    generateQrCodeBtn.textContent = 'Gerando...';
    qrcodeDisplayArea.style.display = 'none';
    errorQrMsg.style.display = 'none';
    loadingQrMsg.style.display = 'block';
    countdownTimerDisplay.textContent = '';

    try {
        const response = await fetch(PERFORM_AUTH_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                apiKey: PARTNER_API_KEY,
                url: PARTNER_URL
            }),
        });

        loadingQrMsg.style.display = 'none';

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erro na requisição: ${response.status} - ${errorText}`);
        }

        const data = await response.json();
        
        qrcodeImg.src = data.qrBase64;
        qrcodeDisplayArea.style.display = 'block';
        startCountdown();
        generateQrCodeBtn.disabled = false;
        generateQrCodeBtn.textContent = 'Gerar QR Code';

    } catch (error) {
        console.error('Erro ao gerar QR Code:', error);
        loadingQrMsg.style.display = 'none';
        errorQrMsg.textContent = `Falha ao gerar QR Code: ${error.message}`;
        errorQrMsg.style.display = 'block';
        generateQrCodeBtn.disabled = false;
        generateQrCodeBtn.textContent = 'Gerar QR Code';
    }
});