// Needed by some browser websocket libraries when bundled (e.g. SockJS/STOMP)
if (typeof globalThis.global === 'undefined') {
  globalThis.global = globalThis
}

if (typeof globalThis.process === 'undefined') {
  globalThis.process = { env: {} }
}
