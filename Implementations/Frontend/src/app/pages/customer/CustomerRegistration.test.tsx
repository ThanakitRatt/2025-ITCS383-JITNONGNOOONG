import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import CustomerRegistration from './CustomerRegistration';

const mockNavigate = vi.fn();

vi.mock('react-router', () => ({
  useNavigate: () => mockNavigate,
}));

vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

vi.mock('../../services/auth.service', () => ({
  default: {
    register: vi.fn(),
  },
}));

import { toast } from 'sonner';
import authService from '../../services/auth.service';

const fillForm = () => {
  fireEvent.change(screen.getByLabelText(/first name/i), { target: { value: ' John ' } });
  fireEvent.change(screen.getByLabelText(/last name/i), { target: { value: ' Doe ' } });
  fireEvent.change(screen.getByLabelText(/^email/i), { target: { value: ' john@example.com ' } });
  fireEvent.change(screen.getByLabelText(/mobile number/i), { target: { value: ' 0812345678 ' } });
  fireEvent.change(screen.getByLabelText(/delivery address/i), { target: { value: ' 123 Test St ' } });
  fireEvent.change(screen.getByLabelText(/^credit card number/i), { target: { value: '4111111111111111' } });
  fireEvent.change(screen.getByLabelText(/expiry date/i), { target: { value: '12/30' } });
  fireEvent.change(screen.getByLabelText(/^cvv/i), { target: { value: '123' } });
};

describe('CustomerRegistration', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows an error when passwords do not match', async () => {
    render(<CustomerRegistration />);

    fillForm();
    fireEvent.change(screen.getByLabelText(/^password/i), { target: { value: 'password123' } });
    fireEvent.change(screen.getByLabelText(/confirm password/i), { target: { value: 'different123' } });
    fireEvent.submit(screen.getByRole('button', { name: /create account/i }).closest('form')!);

    expect(toast.error).toHaveBeenCalledWith('Passwords do not match');
    expect(authService.register).not.toHaveBeenCalled();
  });

  it('shows an error when the password is too short', () => {
    render(<CustomerRegistration />);

    fillForm();
    fireEvent.change(screen.getByLabelText(/^password/i), { target: { value: 'short' } });
    fireEvent.change(screen.getByLabelText(/confirm password/i), { target: { value: 'short' } });
    fireEvent.submit(screen.getByRole('button', { name: /create account/i }).closest('form')!);

    expect(toast.error).toHaveBeenCalledWith('Password must be at least 8 characters');
    expect(authService.register).not.toHaveBeenCalled();
  });

  it('submits trimmed registration data and redirects on success', async () => {
    vi.mocked(authService.register).mockResolvedValueOnce({} as never);

    render(<CustomerRegistration />);

    fillForm();
    fireEvent.change(screen.getByLabelText(/^password/i), { target: { value: 'password123' } });
    fireEvent.change(screen.getByLabelText(/confirm password/i), { target: { value: 'password123' } });
    fireEvent.submit(screen.getByRole('button', { name: /create account/i }).closest('form')!);

    await waitFor(() => {
      expect(authService.register).toHaveBeenCalledWith({
        name: 'John Doe',
        email: 'john@example.com',
        password: 'password123',
        phoneNumber: '0812345678',
        role: 'CUSTOMER',
        address: '123 Test St',
      });
      expect(toast.success).toHaveBeenCalledWith('Registration successful! Please sign in.');
      expect(mockNavigate).toHaveBeenCalledWith('/login');
    });
  });

  it('shows the API error message when registration fails', async () => {
    vi.mocked(authService.register).mockRejectedValueOnce({
      response: { data: { message: 'Email already exists' } },
    });

    render(<CustomerRegistration />);

    fillForm();
    fireEvent.change(screen.getByLabelText(/^password/i), { target: { value: 'password123' } });
    fireEvent.change(screen.getByLabelText(/confirm password/i), { target: { value: 'password123' } });
    fireEvent.submit(screen.getByRole('button', { name: /create account/i }).closest('form')!);

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Email already exists');
    });
  });

  it('navigates back to login from the header button', () => {
    render(<CustomerRegistration />);

    fireEvent.click(screen.getByRole('button', { name: /back to login/i }));

    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });
});
