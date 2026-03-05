/**
 * Winga-otp: WhatsApp OTP sender (Baileys).
 * Run: npm start → QR code itatokea → scan na WhatsApp → kisha server inaweza kutuma OTP.
 */

import express from 'express';
import { connectWhatsApp, getSocket, whenReady } from './handle.js';

const app = express();
app.use(express.json());

function getJid(phone) {
  const p = String(phone).replace(/\D/g, '');
  if (!p) return null;
  return p.includes('@') ? p : p + '@s.whatsapp.net';
}

app.post('/send-otp', async (req, res) => {
  const { phone, code, expiryMinutes = 10, appName = 'Winga' } = req.body || {};
  console.log('POST /send-otp received:', { phone, code: code ? '****' : null });
  if (!phone || !code) {
    return res.status(400).json({ ok: false, error: 'phone and code required' });
  }

  const jid = getJid(phone);
  if (!jid) {
    console.error('Invalid phone for WhatsApp:', phone);
    return res.status(400).json({ ok: false, error: 'Invalid phone number' });
  }

  const sock = getSocket();
  if (!sock) {
    console.error('WhatsApp not connected — cannot send to', jid);
    return res.status(503).json({
      ok: false,
      error: 'WhatsApp haijaungani. Scan QR kwanza (npm start), kisha subiri "WhatsApp imeungana".',
    });
  }

  const text = `${appName} — Your verification code: *${code}*\n\nThis code expires in ${expiryMinutes} minutes. Do not share it.`;
  try {
    await sock.sendMessage(jid, { text });
    console.log('WhatsApp OTP sent to', jid);
    res.json({ ok: true, sent: true });
  } catch (e) {
    console.error('Send OTP failed to', jid, ':', e.message);
    res.status(500).json({ ok: false, error: e.message });
  }
});

app.get('/health', (req, res) => {
  res.json({
    ok: true,
    whatsapp: getSocket() ? 'connected' : 'disconnected',
  });
});

const PORT = process.env.PORT || 3100;

console.log('Winga-otp: Inaanza...');
console.log('Kama QR haionekani na "Connection Failure" inaendelea: futa folder auth_info, kisha npm start tena.\n');

// Kwanza connect WhatsApp. QR itaonekana (au baada ya retry). Server inaanza mara baada ya kuona QR/open au baada ya 20s.
connectWhatsApp()
  .then(() => Promise.race([whenReady, new Promise((r) => setTimeout(r, 20000))]))
  .then(() => {
    app.listen(PORT, () => {
      console.log(`Winga-otp server: http://localhost:${PORT}`);
      console.log('POST /send-otp { "phone": "255712345678", "code": "123456" }');
    });
  })
  .catch((err) => {
    console.error('WhatsApp connect failed:', err);
    process.exit(1);
  });
