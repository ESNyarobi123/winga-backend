/**
 * WhatsApp connection handler (Baileys).
 * Inaonyesha QR code kwenye terminal ili uscan na WhatsApp (Linked Devices).
 * Baada ya scan, connection iko tayari kutuma messages.
 */

import { makeWASocket, useMultiFileAuthState } from '@whiskeysockets/baileys';
import qrcode from 'qrcode-terminal';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const authFolder = path.join(__dirname, '..', 'auth_info');

// Silent logger (Baileys inahitaji logger)
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

/**
 * Connect to WhatsApp. First run: QR itaonekana kwenye terminal — scan na WhatsApp yako.
 * Baada ya scan, session inahifadhiwa auth_info/ na runs zijazo hazitaki QR tena.
 */
export async function connectWhatsApp() {
  const { state, saveCreds } = await useMultiFileAuthState(authFolder);

  sock = makeWASocket({
    auth: state,
    printQRInTerminal: true, // Baileys pia inaweza ku-print QR kama backup
    logger,
  });

  sock.ev.on('connection.update', (update) => {
    const { connection, qr } = update;

    if (qr) {
      console.log('\n========================================');
      console.log('  SCAN HII QR NA WHATSAPP YAKO');
      console.log('  WhatsApp → Settings → Linked Devices → Link a device');
      console.log('========================================\n');
      qrcode.generate(qr, { small: true });
      console.log('\n');
    }

    if (connection === 'open') {
      console.log('✓ WhatsApp imeungana. Unaweza kutuma OTP sasa.\n');
    }

    if (connection === 'close') {
      const reason = update.lastDisconnect?.error?.message || 'unknown';
      console.warn('WhatsApp connection imefungwa:', reason);
      if (reason.includes('restart') || reason.includes('conflict') || reason.includes('Connection Lost')) {
        console.log('Restart app (npm start) ili kuungana tena au kuonyesha QR mpya.');
      }
    }
  });

  sock.ev.on('creds.update', saveCreds);

  return sock;
}

/**
 * Socket iliyoundwa (null kabla ya kuconnect).
 */
export function getSocket() {
  return sock;
}
