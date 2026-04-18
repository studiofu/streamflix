/**
 * W3C Trace Context — traceparent header (https://www.w3.org/TR/trace-context/).
 * Format: {version}-{trace-id}-{parent-id}-{trace-flags}
 */

function randomHexLower(byteLength) {
  const buf = new Uint8Array(byteLength);
  crypto.getRandomValues(buf);
  let out = '';
  for (let i = 0; i < buf.length; i += 1) {
    out += buf[i].toString(16).padStart(2, '0');
  }
  return out;
}

/** Builds a new traceparent value: version 00,128-bit trace id, 64-bit span id, sampled (01). */
export function createTraceparentHeader() {
  const traceId = randomHexLower(16);
  const spanId = randomHexLower(8);
  return `00-${traceId}-${spanId}-01`;
}

const traceparentByOperation = new WeakMap();

/**
 * One stable traceparent per Apollo operation (including ErrorLink retries on the same operation).
 * @param {object} operation — SetContextLink's operation argument
 */
export function getTraceparentForOperation(operation) {
  let value = traceparentByOperation.get(operation);
  if (!value) {
    value = createTraceparentHeader();
    traceparentByOperation.set(operation, value);
  }
  return value;
}
