import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import LoginPage from './LoginPage';
import { toast } from 'sonner';

// Mock dependencies
vi.mock('react-router', () => ({
  useNavigate: () => vi.fn(),
}));

vi.mock('../contexts/AppContext', () => ({
  useApp: () => ({
    user: null,
    setUser: vi.fn(),
  }),
}));

vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

vi.mock('../services/auth.service', () => ({
  default: {
    login: vi.fn(),
    verifyOtp: vi.fn(),
  },
}));

import authService from '../services/auth.service';

beforeEach(() => {
  vi.clearAllMocks();
});

// Test helpers
const fillLoginForm = (email: string, password: string) => {
  const emailInput = screen.getByPlaceholderText(/you@example.com/i);
  const passwordInput = screen.getByPlaceholderText(/••••••••/);
  fireEvent.change(emailInput, { target: { value: email } });
  fireEvent.change(passwordInput, { target: { value: password } });
  return { emailInput, passwordInput };
};

const createMockAuthResponse = (email: string, name: string, role: string) => ({
  success: true,
  data: {
    user: { id: 1, name, email, role },
  },
} as any);

describe('LoginPage', () => {
  it('renders login form', () => {
    render(<LoginPage />);
    expect(screen.getByText(/sign in to your account/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/you@example.com/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/••••••••/)).toBeInTheDocument();
  });

  it('shows demo accounts', () => {
    render(<LoginPage />);
    expect(screen.getByText(/👤 Customer/i)).toBeInTheDocument();
    expect(screen.getByText(/🍽️ Restaurant/i)).toBeInTheDocument();
    expect(screen.getByText(/🛵 Rider/i)).toBeInTheDocument();
    expect(screen.getByText(/🛡️ Admin/i)).toBeInTheDocument();
  });

  it('allows user to enter email and password', () => {
    render(<LoginPage />);
    
    const { emailInput, passwordInput } = fillLoginForm('test@example.com', 'password123');
    
    expect(emailInput).toHaveValue('test@example.com');
    expect(passwordInput).toHaveValue('password123');
  });

  it('toggles password visibility', () => {
    render(<LoginPage />);
    
    const passwordInput = screen.getByPlaceholderText(/••••••••/);
    expect(passwordInput).toHaveAttribute('type', 'password');
    
    // Find the eye icon button (within same parent as password input)
    const toggleButtons = screen.getAllByRole('button');
    const toggleButton = toggleButtons.find(btn => btn.querySelector('svg'));
    if (toggleButton) fireEvent.click(toggleButton);
    
    expect(passwordInput).toHaveAttribute('type', 'text');
  });

  it('submits login form successfully', async () => {
    vi.mocked(authService.login).mockResolvedValueOnce(
      createMockAuthResponse('test@example.com', 'Test User', 'CUSTOMER')
    );

    render(<LoginPage />);
    
    fillLoginForm('test@example.com', 'password123');
    const submitButton = screen.getByRole('button', { name: /sign in/i });
    fireEvent.click(submitButton);
    
    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123',
      });
      expect(toast.success).toHaveBeenCalledWith('OTP sent to your email');
    });
  });

  it('shows error message on login failure', async () => {
    vi.mocked(authService.login).mockRejectedValueOnce({
      response: { data: { message: 'Invalid credentials' } },
    });

    render(<LoginPage />);
    
    fillLoginForm('wrong@example.com', 'wrongpass');
    const submitButton = screen.getByRole('button', { name: /sign in/i });
    fireEvent.click(submitButton);
    
    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Invalid credentials');
    });
  });

  it('shows OTP input after successful login', async () => {
    vi.mocked(authService.login).mockResolvedValueOnce(
      createMockAuthResponse('test@example.com', 'Test User', 'CUSTOMER')
    );

    render(<LoginPage />);
    
    fillLoginForm('test@example.com', 'password123');
    const submitButton = screen.getByRole('button', { name: /sign in/i });
    fireEvent.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByText(/enter the 6-digit code/i)).toBeInTheDocument();
    });
  });

  it('fills demo account credentials when clicking demo account', () => {
    render(<LoginPage />);
    
    const customerButton = screen.getByText(/👤 Customer/i);
    fireEvent.click(customerButton);
    
    const emailInput = screen.getByPlaceholderText(/you@example.com/i);
    const passwordInput = screen.getByPlaceholderText(/••••••••/);
    
    expect(emailInput).toHaveValue('customer@foodexpress.com');
    expect(passwordInput).toHaveValue('customer123');
  });

  it('shows loading state during login', async () => {
    vi.mocked(authService.login).mockImplementation(
      () => new Promise(resolve => setTimeout(resolve, 1000))
    );

    render(<LoginPage />);
    
    const emailInput = screen.getByPlaceholderText(/you@example.com/i);
    const passwordInput = screen.getByPlaceholderText(/••••••••/);
    const submitButton = screen.getByRole('button', { name: /sign in/i });
    
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(submitButton);
    
    expect(submitButton).toBeDisabled();
  });

  it('verifies OTP successfully', async () => {
    // First login
    vi.mocked(authService.login).mockResolvedValueOnce({
      success: true,
      data: {
        user: { id: 1, name: 'Test User', email: 'test@example.com', role: 'CUSTOMER' },
      },
    } as any);

    // Then verify OTP
    vi.mocked(authService.verifyOtp).mockResolvedValueOnce({
      success: true,
      data: {
        token: 'mock-token',
        user: { id: 1, name: 'Test User', email: 'test@example.com', role: 'CUSTOMER' },
      },
    } as any);

    render(<LoginPage />);
    
    // Login first
    const emailInput = screen.getByPlaceholderText(/you@example.com/i);
    const passwordInput = screen.getByPlaceholderText(/••••••••/);
    const submitButton = screen.getByRole('button', { name: /sign in/i });
    
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByText(/two-factor verification/i)).toBeInTheDocument();
    });

    // Enter OTP  
    const otpInput = screen.getAllByRole('textbox')[0];
    fireEvent.change(otpInput, { target: { value: '123456' } });

    const verifyButton = screen.getByRole('button', { name: /verify & sign in/i });
    fireEvent.click(verifyButton);

    await waitFor(() => {
      expect(authService.verifyOtp).toHaveBeenCalled();
    });
  });

  it('disables verify button when OTP is incomplete', async () => {
    // First login
    vi.mocked(authService.login).mockResolvedValueOnce({
      success: true,
      data: {
        user: { id: 1, name: 'Test User', email: 'test@example.com', role: 'CUSTOMER' },
      },
    } as any);

    render(<LoginPage />);
    
    // Login first
    const emailInput = screen.getByPlaceholderText(/you@example.com/i);
    const passwordInput = screen.getByPlaceholderText(/••••••••/);
    const submitButton = screen.getByRole('button', { name: /sign in/i });
    
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByText(/two-factor verification/i)).toBeInTheDocument();
    });

    // Verify button should be disabled with incomplete OTP
    const verifyButton = screen.getByRole('button', { name: /verify & sign in/i });
    expect(verifyButton).toBeDisabled();
  });
});
