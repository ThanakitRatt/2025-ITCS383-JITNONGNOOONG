/**
 * Authentication Service
 * 
 * Handles all authentication-related API calls
 */

import apiClient from './apiClient';
import { API_ENDPOINTS } from '../config/api.config';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phoneNumber: string;
  role: 'CUSTOMER' | 'RIDER' | 'RESTAURANT' | 'ADMIN';
  address?: string;
  latitude?: number;
  longitude?: number;
}

export interface OtpRequest {
  email: string;
  otp: string;
}

export interface UserDTO {
  id: number;
  email: string;
  name: string;
  role: string;
  phoneNumber?: string;
}

export interface LoginResponse {
  success: boolean;
  data: {
    token?: string;
    refreshToken?: string;
    user: UserDTO;
    message: string;
  };
  message: string;
}

// Demo accounts shared between login and verifyOtp mock fallbacks
const MOCK_USERS: Record<string, { name: string; role: string; id: number }> = {
  'customer@foodexpress.com':   { name: 'Demo Customer',       role: 'CUSTOMER',   id: 100 },
  'sarah@foodexpress.com':      { name: 'Sarah Wilson',         role: 'CUSTOMER',   id: 101 },
  'restaurant@foodexpress.com': { name: 'Bangkok Street Food',  role: 'RESTAURANT', id: 2 },
  'sushi@foodexpress.com':      { name: 'Sushi Master',         role: 'RESTAURANT', id: 6 },
  'rider@foodexpress.com':      { name: 'Demo Rider',           role: 'RIDER',      id: 200 },
  'admin@foodexpress.com':      { name: 'Demo Admin',           role: 'ADMIN',      id: 300 },
};

/**
 * Helper function to save authentication data to localStorage
 */
const saveAuthData = (token?: string, refreshToken?: string, user?: UserDTO): void => {
  if (token) {
    localStorage.setItem('authToken', token);
  }
  if (refreshToken) {
    localStorage.setItem('refreshToken', refreshToken);
  }
  if (user) {
    localStorage.setItem('userId', user.id.toString());
    localStorage.setItem('userRole', user.role);
    localStorage.setItem('userEmail', user.email);
    localStorage.setItem('userName', user.name);
  }
};

/**
 * Login with email and password (triggers OTP)
 * 
 * TEMPORARY: Mock authentication when backend is unavailable
 */
export const login = async (credentials: LoginRequest): Promise<LoginResponse> => {
  try {
    const response = await apiClient.post(API_ENDPOINTS.AUTH.LOGIN, credentials);
    return response.data;
  } catch (error: any) {
    // Fallback to mock authentication if backend is unavailable
    console.warn('[AUTH] Backend unavailable, using mock authentication', error.message);
    
    const mockUser = MOCK_USERS[credentials.email];
    
    if (mockUser) {
      return {
        success: true,
        message: 'Login successful (mock mode)',
        data: {
          message: 'OTP sent to your email',
          user: {
            id: mockUser.id,
            email: credentials.email,
            name: mockUser.name,
            role: mockUser.role,
            phoneNumber: '+66-12-345-6789'
          }
        }
      };
    }
    
    throw error;
  }
};

/**
 * Verify OTP and get JWT tokens
 * 
 * TEMPORARY: Mock OTP verification when backend is unavailable
 */
export const verifyOtp = async (otpData: OtpRequest): Promise<LoginResponse> => {
  try {
    const response = await apiClient.post(API_ENDPOINTS.AUTH.OTP, otpData);
    
    // Save tokens and user info to localStorage
    if (response.data.success && response.data.data) {
      const { token, refreshToken, user } = response.data.data;
      saveAuthData(token, refreshToken, user);
    }
    
    return response.data;
  } catch (error: any) {
    // Fallback to mock OTP verification if backend is unavailable
    console.warn('[AUTH] Backend unavailable, using mock OTP verification', error.message);
    
    // Accept any 6-digit OTP in mock mode
    if (otpData.otp && otpData.otp.length === 6) {
      const mockUser = MOCK_USERS[otpData.email];
      
      if (mockUser) {
        const mockToken = 'mock_token_' + Date.now();
        const userId = mockUser.id;
        
        // Save mock tokens
        const mockUserData: UserDTO = {
          id: userId,
          email: otpData.email,
          name: mockUser.name,
          role: mockUser.role,
          phoneNumber: '+66-12-345-6789'
        };
        saveAuthData(mockToken, mockToken, mockUserData);
        
        return {
          success: true,
          message: 'Login successful (mock mode)',
          data: {
            token: mockToken,
            refreshToken: mockToken,
            message: 'Login successful',
            user: {
              id: userId,
              email: otpData.email,
              name: mockUser.name,
              role: mockUser.role,
              phoneNumber: '+66-12-345-6789'
            }
          }
        };
      }
    }
    
    throw error;
  }
};

/**
 * Register a new user
 */
export const register = async (userData: RegisterRequest): Promise<LoginResponse> => {
  const response = await apiClient.post(API_ENDPOINTS.AUTH.REGISTER, userData);
  
  // Save tokens and user info to localStorage
  if (response.data.success && response.data.data) {
    const { token, refreshToken, user } = response.data.data;
    saveAuthData(token, refreshToken, user);
  }
  
  return response.data;
};

/**
 * Refresh access token
 */
export const refreshToken = async (): Promise<LoginResponse> => {
  const refreshToken = localStorage.getItem('refreshToken');
  
  if (!refreshToken) {
    throw new Error('No refresh token available');
  }
  
  const response = await apiClient.post(API_ENDPOINTS.AUTH.REFRESH, { refreshToken });
  
  if (response.data.success && response.data.data?.token) {
    localStorage.setItem('authToken', response.data.data.token);
  }
  
  return response.data;
};

/**
 * Logout user
 */
export const logout = async (): Promise<void> => {
  const token = localStorage.getItem('authToken');
  
  try {
    await apiClient.post(API_ENDPOINTS.AUTH.LOGOUT, { token });
  } finally {
    // Clear all auth data from localStorage
    localStorage.removeItem('authToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userName');
  }
};

/**
 * Resend OTP
 */
export const resendOtp = async (email: string): Promise<void> => {
  await apiClient.post(`${API_ENDPOINTS.AUTH.OTP}/resend`, { email });
};

/**
 * Check if user is authenticated
 */
export const isAuthenticated = (): boolean => {
  const token = localStorage.getItem('authToken');
  return !!token;
};

/**
 * Get current user from localStorage
 */
export const getCurrentUser = (): UserDTO | null => {
  const userId = localStorage.getItem('userId');
  const email = localStorage.getItem('userEmail');
  const name = localStorage.getItem('userName');
  const role = localStorage.getItem('userRole');
  
  if (!userId || !email || !name || !role) {
    return null;
  }
  
  return {
    id: parseInt(userId),
    email,
    name,
    role,
  };
};

const authService = {
  login,
  verifyOtp,
  register,
  refreshToken,
  logout,
  resendOtp,
  isAuthenticated,
  getCurrentUser,
};

export default authService;
