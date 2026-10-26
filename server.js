// Node.js HTTP server for InvestPro Web PWA on port 3000
const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');

// Global error handlers to prevent crashes on socket disconnects/EPIPE
process.on('uncaughtException', (err) => {
  console.warn('[PWA Server] Handled uncaught exception:', err.message);
});

process.on('unhandledRejection', (reason) => {
  console.warn('[PWA Server] Handled unhandled rejection:', reason);
});

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.json': 'application/manifest+json; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.jsx': 'application/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon'
};

function serveFile(res, filePath, contentType, customHeaders = {}) {
  fs.readFile(filePath, (err, data) => {
    if (err) {
      if (!res.headersSent) {
        res.writeHead(500, { 'Content-Type': 'text/plain' });
        res.end('Internal Server Error');
      }
      return;
    }
    if (!res.headersSent) {
      res.writeHead(200, {
        'Content-Type': contentType,
        'Content-Length': Buffer.byteLength(data),
        ...customHeaders
      });
      res.end(data);
    }
  });
}

const server = http.createServer((req, res) => {
  // Catch response write errors (e.g. client disconnects)
  res.on('error', (err) => {
    console.warn('[PWA Server] Response stream warning:', err.message);
  });

  // Enable CORS for PWA testing tools (e.g. pwabuilder.com)
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, HEAD, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', '*');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const url = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  let pathname = decodeURIComponent(url.pathname);

  console.log(`[PWA Server] ${req.method} ${pathname}`);

  // Special PWA routes
  if (pathname === '/manifest.json') {
    const filePath = path.join(PUBLIC_DIR, 'manifest.json');
    if (fs.existsSync(filePath)) {
      serveFile(res, filePath, 'application/manifest+json; charset=utf-8', {
        'Cache-Control': 'public, max-age=3600'
      });
      return;
    }
  }

  if (pathname === '/sw.js') {
    const filePath = path.join(PUBLIC_DIR, 'sw.js');
    if (fs.existsSync(filePath)) {
      serveFile(res, filePath, 'application/javascript; charset=utf-8', {
        'Service-Worker-Allowed': '/',
        'Cache-Control': 'no-cache'
      });
      return;
    }
  }

  // Static file lookup
  let safePath = path.normalize(pathname).replace(/^(\.\.[\/\\])+/, '');
  let targetFile = path.join(PUBLIC_DIR, safePath);

  // If URL matches an actual file in public/
  if (fs.existsSync(targetFile) && fs.statSync(targetFile).isFile()) {
    const ext = path.extname(targetFile).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';
    serveFile(res, targetFile, contentType);
    return;
  }

  // SPA Fallback: Return index.html for all app routes (/admin, /login, /register, etc.)
  const indexPath = path.join(PUBLIC_DIR, 'index.html');
  if (fs.existsSync(indexPath)) {
    serveFile(res, indexPath, 'text/html; charset=utf-8');
    return;
  }

  res.writeHead(404, { 'Content-Type': 'text/plain' });
  res.end('Not Found');
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`[PWA Server] Invest Pro listening on http://0.0.0.0:${PORT}`);
});
