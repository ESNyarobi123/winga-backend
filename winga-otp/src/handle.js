/**
 * WhatsApp connection handler (Baileys).
 * Inaonyesha QR code kwenye terminal (na kwenye file qr.html) ili uscan na WhatsApp.
 */

import { makeWASocket, useMultiFileAuthState } from '@whiskeysockets/baileys';
import QRCode from 'qrcode';
import path from 'path';
import { fileURLToPath } from 'url';
import fs from 'fs';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const authFolder = path.join(__dirname, '..', 'auth_info');
const qrFilePath = path.join(__dirname, '..', 'qr.html');

const logger = {
  child: () => logger,
  trace: () => {},
  debug: () => {},
  info: () => {},
  warn: () => {},
  error: () => {},
  fatal: () => {},
};

let sock = null;
let resolveReady = null;
export const whenReady = new Promise((r) => { resolveReady = r; });

async function showQR(qr) {
  if (resolveReady) { resolveReady(); resolveReady = null; }

  console.log('\n========================================');
  console.log('  SCAN HII QR NA WHATSAPP YAKO');
  console.log('  WhatsApp → Settings → Linked Devices → Link a device');
  console.log('========================================\n');

  try {
    // Njia ya kwanza: terminal (kama docs ya Baileys)
    const terminalQR = await QRCode.toString(qr, { type: 'terminal', small: true });
    console.log(terminalQR);
  } catch (e) {
    console.log('(QR code:', qr?.substring(0, 30) + '... )');
  }

  try {
    // Njia ya pili: file qr.html — fungua kwenye browser ili uscan
    const dataUrl = await QRCode.toDataURL(qr);
    const html = `<!DOCTYPE html><html><head><meta charset="utf-8"><title>Winga OTP - Scan QR</title></head><body style="text-align:center;font-family:sans-serif;padding:2rem;"><h1>Scan na WhatsApp</h1><p>WhatsApp → Settings → Linked Devices → Link a device</p><img src="${dataUrl}" alt="QR" style="max-width:300px;"/></body></html>`;
    fs.writeFileSync(qrFilePath, html);
    console.log('Pia unaweza kufungua file hii kwenye browser ili uscan:');
    console.log('  ' + qrFilePath);
    console.log('');
  } catch (e) {
    // ignore file error
  }

  console.log('(Subiri scan; QR inaweza kukoma na kutokea tena.)\n');
}

function setupConnectionEvents(sock, saveCreds) {
  sock.ev.on('connection.update', (update) => {
    const { connection, qr } = update;

    if (qr) {
      showQR(qr);
    }

    if (connection === 'open') {
      if (resolveReady) { resolveReady(); resolveReady = null; }
      console.log('✓ WhatsApp imeungana. Unaweza kutuma OTP sasa.\n');
    }

    if (connection === 'connecting') {
      console.log('Inaungana na WhatsApp...');
    }

    if (connection === 'close') {
      const err = update.lastDisconnect?.error;
      const reason = err?.message || 'unknown';
      const statusCode = err?.output?.statusCode;

      console.warn('WhatsApp connection imefungwa:', reason, statusCode != null ? `(statusCode: ${statusCode})` : '');

      if (statusCode === 401) {
        console.log('Session imefungwa. Futa folder auth_info na npm start ili scan QR mpya.');
        return;
      }

      // Connection Failure / timeout — jaribu tena (inaweza kutoa QR)
      console.log('Inajaribu kuungana tena ndani ya sekunde 5...');
      console.log('Kama inaendelea: futa folder auth_info, kisha npm start tena.');
      setTimeout(() => connectWhatsApp(), 5000);
    }
  });

  sock.ev.on('creds.update', saveCreds);
}

/**
 * Connect to WhatsApp. First run: QR itaonekana (terminal + file qr.html).
 */
export async function connectWhatsApp() {
  const { state, saveCreds } = await useMultiFileAuthState(authFolder);

  sock = makeWASocket({
    auth: state,
    logger,
    getMessage: async () => undefined,
  });

  setupConnectionEvents(sock, saveCreds);

  return sock;
}

export function getSocket() {
  return sock;
}
