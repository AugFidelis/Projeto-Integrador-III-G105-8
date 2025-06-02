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

let loginTokenGlobal = null; // Armazena o token atual
let statusInterval = null;   // Intervalo para polling

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
            // Limpa polling de status também
            if (statusInterval) clearInterval(statusInterval);
        }
    }, 1000);
}

// NOVA FUNÇÃO: Checar status periodicamente
function startLoginStatusPolling(loginToken) {
    if (statusInterval) clearInterval(statusInterval);

    statusInterval = setInterval(async () => {
        try {
            const response = await fetch('https://us-central1-projeto-integrador-3-g105-8.cloudfunctions.net/getLoginStatus', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ loginToken })
            });

            if (response.status === 200) {
                // Login SUCCESS!
                clearInterval(statusInterval);
                window.location.href = "success.html";
            } else if (response.status === 410) {
                // Token expirado
                clearInterval(statusInterval);
                countdownTimerDisplay.textContent = "QR Code expirado!";
                qrcodeDisplayArea.style.display = 'none';
            }
            // Se for 202 ("pending"), não faz nada
        } catch (e) {
            console.error("Erro ao checar status do login:", e);
        }
    }, 2000); // Checa a cada 2 segundos
}

// Evento de clique no botão "Gerar QR Code"
generateQrCodeBtn.addEventListener('click', async () => {
    if (countdownInterval) {
        clearInterval(countdownInterval);
    }
    if (statusInterval) {
        clearInterval(statusInterval);
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

        loginTokenGlobal = data.loginToken; // Salva o token
        startLoginStatusPolling(loginTokenGlobal); // Começa a escutar

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