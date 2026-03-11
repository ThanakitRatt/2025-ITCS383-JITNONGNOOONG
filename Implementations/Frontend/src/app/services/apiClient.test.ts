import { describe, it, expect, vi, afterEach } from 'vitest';
import axios from 'axios';
import apiClient, { handleApiError } from './apiClient';

function makeAxiosError(props: Record<string, unknown>): Error {
  return Object.assign(new Error('test'), { isAxiosError: true, ...props });
}

describe('handleApiError', () => {
  it('returns message from server response data', () => {
    const err = makeAxiosError({ response: { data: { message: 'Not found' } } });
    expect(handleApiError(err)).toBe('Not found');
  });

  it('returns error field when message is absent', () => {
    const err = makeAxiosError({ response: { data: { error: 'Bad request' } } });
    expect(handleApiError(err)).toBe('Bad request');
  });

  it('returns fallback when response data has no message or error field', () => {
    const err = makeAxiosError({ response: { data: {} } });
    expect(handleApiError(err)).toBe('An error occurred');
  });

  it('returns no-response message when request was sent but no response received', () => {
    const err = makeAxiosError({ request: {} });
    expect(handleApiError(err)).toContain('No response');
  });

  it('returns Error.message for plain Error instances', () => {
    expect(handleApiError(new Error('connection reset'))).toBe('connection reset');
  });

  it('returns unexpected error fallback for unknown types', () => {
    expect(handleApiError(null)).toBe('An unexpected error occurred');
  });
});

describe('apiClient interceptors', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('request interceptor error handler rethrows the error', () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const handlers = (apiClient.interceptors.request as any).handlers;
    const errorHandler = handlers[handlers.length - 1].rejected;
    const err = new Error('request setup failed');
    expect(() => errorHandler(err)).toThrow('request setup failed');
  });

  it('response interceptor rethrows non-401 errors', async () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const handlers = (apiClient.interceptors.response as any).handlers;
    const errorHandler = handlers[handlers.length - 1].rejected;
    const err = makeAxiosError({ response: { status: 500 }, config: {} });
    await expect(errorHandler(err)).rejects.toThrow();
  });

  it('response interceptor rethrows error after failed token refresh', async () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const handlers = (apiClient.interceptors.response as any).handlers;
    const errorHandler = handlers[handlers.length - 1].rejected;

    vi.spyOn(Storage.prototype, 'getItem').mockImplementation((key: string) => {
      if (key === 'refreshToken') return 'some-refresh-token';
      return null;
    });
    vi.spyOn(axios, 'post').mockRejectedValue(new Error('refresh failed'));
    Object.defineProperty(globalThis, 'location', {
      value: { href: '' },
      configurable: true,
      writable: true,
    });

    const err = makeAxiosError({ response: { status: 401 }, config: { _retry: false } });
    await expect(errorHandler(err)).rejects.toThrow();
  });
});
