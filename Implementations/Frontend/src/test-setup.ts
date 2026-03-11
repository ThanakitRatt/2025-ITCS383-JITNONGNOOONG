import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Polyfill ResizeObserver used by Radix UI components
globalThis.ResizeObserver = class ResizeObserver {
  observe(): void { /* noop polyfill */ }
  unobserve(): void { /* noop polyfill */ }
  disconnect(): void { /* noop polyfill */ }
};

// Polyfill matchMedia used by some Radix UI components
Object.defineProperty(globalThis, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});
