// Service Worker Registration for InvestPro PWA
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    const swUrl = '/sw.js';
    navigator.serviceWorker
      .register(swUrl)
      .then((registration) => {
        console.log('[InvestPro PWA] Service Worker registered successfully with scope:', registration.scope);
      })
      .catch((error) => {
        console.error('[InvestPro PWA] Service Worker registration failed:', error);
      });
  });
}
