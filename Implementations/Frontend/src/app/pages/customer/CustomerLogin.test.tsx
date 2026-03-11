import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect } from 'vitest';
import CustomerLogin from './CustomerLogin';

vi.mock('react-router', () => ({
  useNavigate: () => vi.fn(),
}));

vi.mock('../../contexts/AppContext', () => ({
  useApp: () => ({ setUser: vi.fn() }),
}));

vi.mock('sonner', () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

describe('CustomerLogin', () => {
  it('renders email and password fields', () => {
    render(<CustomerLogin />);
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
  });

  it('submitting the credentials form transitions to the OTP step', async () => {
    render(<CustomerLogin />);
    const form = document.querySelector('form');
    if (!form) throw new Error('form element not found');
    fireEvent.submit(form);

    await waitFor(() => {
      expect(screen.getByText(/otp sent to your email/i)).toBeInTheDocument();
    });
  });
});
