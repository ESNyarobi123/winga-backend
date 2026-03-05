/**
 * WhatsApp connection handler (Baileys).
 * Inaonyesha QR code kwenye terminal ili uscan na WhatsApp (Linked Devices).
 * Baileys sasa haitumii printQRInTerminal — tunashughulikia QR kwa connection.update.
 */

import { makeWASocket, useMultiFileAuthState } from '@whiskeysockets/baileys';
import qrcode from 'qrcode-terminal';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const authFolder = path.join(__dirname, '..', 'auth_info');

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
/** Resolves when first QR is shown or connection is open (server inaweza kuanza). */
export const whenReady = new Promise((r) => { resolveReady = r; });

function setupConnectionEvents(sock, saveCreds) {
  sock.ev.on('connection.update', (update) => {
    const { connection, qr } = update;

    if (qr) {
      if (resolveReady) { resolveReady(); resolveReady = null; }
      console.log('\n========================================');
      console.log('  SCAN HII QR NA WHATSAPP YAKO');
      console.log('  WhatsApp → Settings → Linked Devices → Link a device');
      console.log('========================================\n');
      qrcode.generate(qr, { small: true });
      console.log('\n(Subiri scan, QR inaweza kukoma na kutokea tena.)\n');
    }

    if (connection === 'open') {
      if (resolveReady) { resolveReady(); resolveReady = null; }
      console.log('✓ WhatsApp imeungana. Unaweza kutuma OTP sasa.\n');
    }

    if (connection === 'close') {
      const err = update.lastDisconnect?.error;
      const reason = err?.message || 'unknown';
      const statusCode = err?.output?.statusCode;

      console.warn('WhatsApp connection imefungwa:', reason);

      // 401 = logged out (user alifunga session), haitaki retry
      if (statusCode === 401) {
        console.log('Session imefungwa. Futa folder auth_info na npm start ili scan QR mpya.');
        return;
      }

      // Connection Failure / timeout / etc — jaribu tena ili kupata QR
      console.log('Inajaribu kuungana tena ndani ya sekunde 3...');
      setTimeout(() => connectWhatsApp(), 3000);
    }
  });

  sock.ev.on('creds.update', saveCreds);
}

/**
 * Connect to WhatsApp. First run: QR itaonekana kwenye terminal — scan na WhatsApp yako.
 * Kama connection inavunjwa (Connection Failure), inajaribu tena moja kwa moja.
 */
export async function connectWhatsApp() {
  const { state, saveCreds } = await useMultiFileAuthState(authFolder);

  sock = makeWASocket({
    auth: state,
    // printQRInTerminal imefutwa na Baileys — tunashughulikia QR kwa connection.update
    logger,
  });

  setupConnectionEvents(sock, saveCreds);

  return sock;
}

export function getSocket() {
  return sock;
}
