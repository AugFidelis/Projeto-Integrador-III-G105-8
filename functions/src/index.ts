import {onRequest} from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import * as crypto from "crypto";
import * as QRCode from "qrcode";
import {initializeApp} from "firebase-admin/app";
import {getFirestore, Timestamp} from "firebase-admin/firestore";

initializeApp();
const db = getFirestore();

export const performAuth = onRequest(async (req, res) => {
  res.set('Access-Control-Allow-Origin', '*');
  res.set('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.set('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.status(204).send('');
    return;
  }

  const {apiKey, url} = req.body;

  if (!apiKey || !url) {
    logger.warn("performAuth: Missing apiKey or url in request body.");
    res.status(400).send("Missing apiKey or url");
    return;
  }

  try {
    const partnersRef = db.collection("partners");
    const snapshot = await partnersRef
      .where("url", "==", url)
      .where("apiKey", "==", apiKey)
      .get();

    if (snapshot.empty) {
      logger.warn("performAuth: Unauthorized partner detected.", {apiKey, url});
      res.status(403).send("Unauthorized partner");
      return;
    }

    const loginToken = generateRandomBase64(256);
    const createdAt = Timestamp.now();

    await db.collection("login").doc(loginToken).set({
      apiKey,
      loginToken,
      createdAt,
      attempts: 0,
    });

    logger.info("performAuth: loginToken gerado e salvo.", {loginToken});

    const qrCodeBase64 = await generateQRCodeBase64(loginToken);

    logger.info("performAuth: QR Code Base64 gerado com sucesso.", {length: qrCodeBase64.length});

    res.status(200).send({qrBase64: qrCodeBase64, loginToken: loginToken});

  } catch (error) {
    logger.error("performAuth: Erro durante a execução da função.", error);
    res.status(500).send("Internal server error");
  }
});

export const getLoginStatus = onRequest(async (req, res) => {
  res.set('Access-Control-Allow-Origin', '*');
  res.set('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.set('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.status(204).send('');
    return;
  }

  const {loginToken} = req.body;

  if (!loginToken) {
    logger.warn("getLoginStatus: Missing loginToken in request body.");
    res.status(400).send("Missing loginToken");
    return;
  }

  try {
    const loginDocRef = db.collection("login").doc(loginToken);
    const loginSnap = await loginDocRef.get();

    if (!loginSnap.exists) {
      logger.warn("getLoginStatus: Token not found.", {loginToken});
      res.status(404).send("Token not found");
      return;
    }

    const loginData = loginSnap.data();
    const now = Timestamp.now();
    const created = loginData?.createdAt as Timestamp;
    const diff = now.seconds - created.seconds;

    if (diff > 60 || (loginData?.attempts ?? 0) >= 3) {
      logger.info("getLoginStatus: Token expired or too many attempts. Deleting.", {loginToken});
      await loginDocRef.delete();
      res.status(410).send({status: "expired"});
      return;
    }

    await loginDocRef.update({
      attempts: (loginData?.attempts ?? 0) + 1,
    });

    if (loginData?.user) {
      logger.info("getLoginStatus: Login successful.", {loginToken, uid: loginData.user});
      res.status(200).send({status: "success", uid: loginData.user});
    } else {
      logger.info("getLoginStatus: Login pending.", {loginToken});
      res.status(202).send({status: "pending"});
    }
  } catch (error) {
    logger.error("getLoginStatus: Erro durante a execução da função.", error);
    res.status(500).send("Internal server error");
  }
});

function generateRandomBase64(length: number): string {
  return crypto.randomBytes(Math.ceil(length * 3 / 4)).toString("base64url").slice(0, length);
}

async function generateQRCodeBase64(text: string): Promise<string> {
  logger.info("generateQRCodeBase64: Gerando QR Code para o texto:", {text});
  try {
    const dataUrl = await QRCode.toDataURL(text);
    return dataUrl;
  } catch (qrError) {
    logger.error("generateQRCodeBase64: Erro ao gerar QR Code.", qrError);
    throw qrError;
  }
}
