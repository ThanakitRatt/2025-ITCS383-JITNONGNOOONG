import { describe, it, expect, vi, beforeEach } from 'vitest';
import type { AxiosResponse } from 'axios';

vi.mock('./apiClient', () => ({
  default: {
    post: vi.fn(),
  },
}));

import apiClient from './apiClient';
import {
  login,
  verifyOtp,
  register,
  refreshToken,
  logout,
  resendOtp,
  isAuthenticated,
  getCurrentUser,
} from './auth.service';

function fakeResponse<T>(data: T): AxiosResponse<T> {
  return { data } as AxiosResponse<T>;
}

beforeEach(() => {
  vi.resetAllMocks();
  localStorage.clear();
});

// ========== login ==========
describe('login', () => {
  it('returns response data on success', async () => {
    const mockResp = { success: true, message: 'OTP sent', data: { message: 'OTP sent', user: { id: 1, email: 'a@b.com', name: 'A', role: 'CUSTOMER' } } };
    vi.mocked(apiClient.post).mockResolvedValueOnce(fakeResponse(mockResp));

    const result = await login({ email: 'a@b.com', password: 'pw' });

    expect(result).toEqual(mockResp);
    expect(apiClient.post).toHaveBeenCalledOnce();
  });

  it('falls back to mock user when backend is unavailable', async () => {
    vi.mocked(apiClient.post).mockRejectedValueOnce(Object.assign(new Error('Network Error'), { message: 'Network Error' }));

    const result = await login({ email: 'customer@foodexpress.com', password: 'any' });

    expect(result.success).toBe(true);
    expect(result.data.user.role).toBe('CUSTOMER');
    expect(result.data.user.email).toBe('customer@foodexpress.com');
  });

  it('rethrows error for unknown email in mock mode', async () => {
    vi.mocked(apiClient.post).mockRejectedValueOnce(new Error('Network Error'));

    await expect(login({ email: 'unknown@example.com', password: 'pw' })).rejects.toThrow();
  });
});

// ========== verifyOtp ==========
describe('verifyOtp', () => {
  it('saves tokens to localStorage on real backend success', async () => {
    const mockResp = {
      success: true,
      message: 'Login successful',
      data: {
        token: 'token123',
        refreshToken: 'refresh123',
        message: 'Login successful',
        user: { id: 1, email: 'a@b.com', name: 'A', role: 'CUSTOMER' },
      },
    };
    vi.mocked(apiClient.post).mockResolvedValueOnce(fakeResponse(mockResp));

    const result = await verifyOtp({ email: 'a@b.com', otp: '123456' });

    expect(result).toEqual(mockResp);
    expect(localStorage.getItem('authToken')).toBe('token123');
    expect(localStorage.getItem('userRole')).toBe('CUSTOMER');
  });

  it('falls back to mock OTP for known demo user with 6-digit OTP', async () => {
    vi.mocked(apiClient.post).mockRejectedValueOnce(new Error('Network Error'));

    const result = await verifyOtp({ email: 'admin@foodexpress.com', otp: '123456' });

    expect(result.success).toBe(true);
    expect(result.data.user.role).toBe('ADMIN');
    expect(localStorage.getItem('userRole')).toBe('ADMIN');
  });

  it('rethrows error for unknown email in mock mode', async () => {
    vi.mocked(apiClient.post).mockRejectedValueOnce(new Error('Network Error'));

    await expect(verifyOtp({ email: 'unknown@example.com', otp: '123456' })).rejects.toThrow();
  });

  it('rethrows error when mock OTP is not 6 digits long', async () => {
    vi.mocked(apiClient.post).mockRejectedValueOnce(new Error('Network Error'));

    await expect(verifyOtp({ email: 'admin@foodexpress.com', otp: '12345' })).rejects.toThrow();
  });
});

// ========== register ==========
describe('register', () => {
  it('saves tokens and returns response on success', async () => {
    const mockResp = {
      success: true,
      message: 'Registered',
      data: {
        token: 'tok',
        refreshToken: 'ref',
        message: 'Registered',
        user: { id: 5, email: 'new@example.com', name: 'New', role: 'CUSTOMER' },
      },
    };
    vi.mocked(apiClient.post).mockResolvedValueOnce(fakeResponse(mockResp));

    const result = await register({
      name: 'New',
      email: 'new@example.com',
      password: 'secret',
      phoneNumber: '+66-12-345-6789',
      role: 'CUSTOMER',
    });

    expect(result).toEqual(mockResp);
    expect(localStorage.getItem('authToken')).toBe('tok');
    expect(localStorage.getItem('userId')).toBe('5');
  });
});

// ========== refreshToken ==========
describe('refreshToken', () => {
  it('throws when no refresh token in localStorage', async () => {
    await expect(refreshToken()).rejects.toThrow('No refresh token available');
  });

  it('updates authToken on success', async () => {
    localStorage.setItem('refreshToken', 'ref123');
    const mockResp = { success: true, message: 'Refreshed', data: { token: 'newtoken', message: 'ok', user: { id: 1, email: 'a@b.com', name: 'A', role: 'CUSTOMER' } } };
    vi.mocked(apiClient.post).mockResolvedValueOnce(fakeResponse(mockResp));

    const result = await refreshToken();

    expect(result).toEqual(mockResp);
    expect(localStorage.getItem('authToken')).toBe('newtoken');
  });
});

// ========== logout ==========
describe('logout', () => {
  it('clears all auth data from localStorage after call', async () => {
    localStorage.setItem('authToken', 'tok');
    localStorage.setItem('refreshToken', 'ref');
    localStorage.setItem('userId', '1');
    localStorage.setItem('userRole', 'CUSTOMER');
    vi.mocked(apiClient.post).mockResolvedValueOnce(fakeResponse({}));

    await logout();

    expect(localStorage.getItem('authToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(localStorage.getItem('userId')).toBeNull();
    expect(localStorage.getItem('userRole')).toBeNull();
  });

  it('still clears localStorage even if the API call fails', async () => {
    localStorage.setItem('authToken', 'tok');
    vi.mocked(apiClient.post).mockRejectedValueOnce(new Error('server error'));

    try {
      await logout();
    } catch {
      // error expected — finally block still clears storage
    }

    expect(localStorage.getItem('authToken')).toBeNull();
  });
});

// ========== resendOtp ==========
describe('resendOtp', () => {
  it('calls API with the email', async () => {
    vi.mocked(apiClient.post).mockResolvedValueOnce(fakeResponse({}));

    await resendOtp('a@b.com');

    expect(apiClient.post).toHaveBeenCalledOnce();
  });
});

// ========== isAuthenticated ==========
describe('isAuthenticated', () => {
  it('returns false when no token', () => {
    expect(isAuthenticated()).toBe(false);
  });

  it('returns true when token exists', () => {
    localStorage.setItem('authToken', 'sometoken');
    expect(isAuthenticated()).toBe(true);
  });
});

// ========== getCurrentUser ==========
describe('getCurrentUser', () => {
  it('returns null when no user in localStorage', () => {
    expect(getCurrentUser()).toBeNull();
  });

  it('returns user object when all fields present', () => {
    localStorage.setItem('userId', '42');
    localStorage.setItem('userEmail', 'user@example.com');
    localStorage.setItem('userName', 'User Name');
    localStorage.setItem('userRole', 'RIDER');

    const user = getCurrentUser();

    expect(user).not.toBeNull();
    expect(user?.id).toBe(42);
    expect(user?.email).toBe('user@example.com');
    expect(user?.role).toBe('RIDER');
  });

  it('returns null when some fields are missing', () => {
    localStorage.setItem('userId', '42');
    // email, name, role missing
    expect(getCurrentUser()).toBeNull();
  });
});
