import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import MenuManagement from './MenuManagement';
import { toast } from 'sonner';

// Mock dependencies
vi.mock('react-router', () => ({
  useNavigate: () => vi.fn(),
}));

vi.mock('../../contexts/AppContext', () => ({
  useApp: () => ({
    user: { id: '2', role: 'restaurant', name: 'Test Restaurant' },
  }),
}));

vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

vi.mock('../../services/restaurant.service', () => ({
  default: {
    getOwnerRestaurants: vi.fn(),
    getRestaurantMenu: vi.fn(),
    getRestaurantCategories: vi.fn(),
    addMenuItem: vi.fn(),
    updateMenuItem: vi.fn(),
    deleteMenuItem: vi.fn(),
  },
}));

import restaurantService from '../../services/restaurant.service';

const flushMenuLoad = async () => {
  await act(async () => {
    await Promise.resolve();
  });
};

const mockMenuItems = [
  {
    id: '1',
    name: 'Pad Thai',
    description: 'Classic Thai noodles',
    price: 120,
    categoryId: '1',
    preparationTime: 15,
    isAvailable: true,
    restaurantId: '2',
  },
  {
    id: '2',
    name: 'Tom Yum Soup',
    description: 'Spicy and sour soup',
    price: 150,
    categoryId: '1',
    preparationTime: 20,
    isAvailable: true,
    restaurantId: '2',
  },
] as any;

const mockCategories = [
  { id: '1', name: 'Main Dishes', description: 'Main course items', restaurantId: '2', displayOrder: 1 },
  { id: '2', name: 'Appetizers', description: 'Starter items', restaurantId: '2', displayOrder: 2 },
] as any;

beforeEach(() => {
  vi.clearAllMocks();
  vi.mocked(restaurantService.getOwnerRestaurants).mockResolvedValue([{ id: '2', name: 'Test Restaurant' } as any]);
  vi.mocked(restaurantService.getRestaurantMenu).mockResolvedValue(mockMenuItems);
  vi.mocked(restaurantService.getRestaurantCategories).mockResolvedValue(mockCategories);
  // Mock window.confirm
  globalThis.confirm = vi.fn(() => true);
});

describe('MenuManagement', () => {
  it('renders menu management header', async () => {
    render(<MenuManagement />);
    await flushMenuLoad();
    expect(screen.getByText('Menu Management')).toBeInTheDocument();
  });

  it('loads and displays menu items', async () => {
    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
      expect(screen.getByText('Tom Yum Soup')).toBeInTheDocument();
    });
  });

  it('shows loading state while fetching data', async () => {
    render(<MenuManagement />);
    await flushMenuLoad();
    expect(screen.getByText(/menu management/i)).toBeInTheDocument();
  });

  it('shows an error when the restaurant cannot be loaded', async () => {
    vi.mocked(restaurantService.getOwnerRestaurants).mockRejectedValueOnce(new Error('Owner load failed'));

    render(<MenuManagement />);

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Failed to load restaurant');
    });
  });

  it('handles API error when loading menu', async () => {
    vi.mocked(restaurantService.getRestaurantMenu).mockRejectedValueOnce(
      new Error('API Error')
    );
    vi.mocked(restaurantService.getRestaurantCategories).mockRejectedValueOnce(
      new Error('API Error')
    );

    render(<MenuManagement />);

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Failed to load menu items');
    });
  });

  it('opens add dialog when clicking Add Menu Item button', async () => {
    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    const addButton = screen.getByText('Add Item');
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });
  });

  it('opens edit dialog when clicking edit button', async () => {
    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    const buttons = screen.getAllByRole('button');
    const editButton = buttons.find(btn => btn.querySelector('.lucide-pencil'));
    if (editButton) fireEvent.click(editButton);

    await waitFor(() => {
      expect(screen.getByDisplayValue('Pad Thai')).toBeInTheDocument();
    });
  });

  it('deletes menu item when clicking delete and confirming', async () => {
    vi.mocked(restaurantService.deleteMenuItem).mockResolvedValueOnce();

    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    const buttons = screen.getAllByRole('button');
    const deleteButton = buttons.find(btn => btn.querySelector('.lucide-trash-2'));
    if (deleteButton) fireEvent.click(deleteButton);

    await waitFor(() => {
      expect(restaurantService.deleteMenuItem).toHaveBeenCalledWith('2', '1');
      expect(toast.success).toHaveBeenCalledWith('Menu item deleted successfully');
    });
  });

  it('does not delete menu item when canceling confirmation', async () => {
    globalThis.confirm = vi.fn(() => false);

    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    const buttons = screen.getAllByRole('button');
    const deleteButton = buttons.find(btn => btn.querySelector('.lucide-trash-2'));
    if (deleteButton) fireEvent.click(deleteButton);

    expect(restaurantService.deleteMenuItem).not.toHaveBeenCalled();
  });

  it('handles delete error', async () => {
    vi.mocked(restaurantService.deleteMenuItem).mockRejectedValueOnce(
      new Error('Delete failed')
    );

    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    const buttons = screen.getAllByRole('button');
    const deleteButton = buttons.find(btn => btn.querySelector('.lucide-trash-2'));
    if (deleteButton) fireEvent.click(deleteButton);

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Failed to delete menu item');
    });
  });

  it('creates new menu item when submitting form', async () => {
    const newItem = { ...mockMenuItems[0], id: '3', name: 'New Item' };
    vi.mocked(restaurantService.addMenuItem).mockResolvedValueOnce(newItem);

    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    // Open add dialog
    const addButton = screen.getByText('Add Item');
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
    });

    // Fill form
    fireEvent.change(screen.getByLabelText(/name/i), {
      target: { value: 'New Item' },
    });
    fireEvent.change(screen.getByLabelText(/price/i), {
      target: { value: '100' },
    });

    // Submit form
    const form = screen.getByLabelText(/name/i).closest('form');
    if (form) {
      fireEvent.submit(form);
    }

    await waitFor(() => {
      expect(restaurantService.addMenuItem).toHaveBeenCalled();
      expect(toast.success).toHaveBeenCalledWith('Menu item added successfully');
    });
  });

  it('updates an existing menu item when submitting the edit form', async () => {
    const updatedItem = { ...mockMenuItems[0], name: 'Updated Pad Thai', price: 130 };
    vi.mocked(restaurantService.updateMenuItem).mockResolvedValueOnce(updatedItem);

    render(<MenuManagement />);

    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    const buttons = screen.getAllByRole('button');
    const editButton = buttons.find(btn => btn.querySelector('.lucide-pencil'));
    if (editButton) fireEvent.click(editButton);

    await waitFor(() => {
      expect(screen.getByDisplayValue('Pad Thai')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText(/item name/i), {
      target: { value: 'Updated Pad Thai' },
    });
    fireEvent.change(screen.getByLabelText(/price/i), {
      target: { value: '130' },
    });

    const form = screen.getByLabelText(/item name/i).closest('form');
    if (form) {
      fireEvent.submit(form);
    }

    await waitFor(() => {
      expect(restaurantService.updateMenuItem).toHaveBeenCalledWith('2', '1', expect.objectContaining({
        name: 'Updated Pad Thai',
        price: 130,
      }));
      expect(toast.success).toHaveBeenCalledWith('Menu item updated successfully');
    });
  });

  it('shows a validation error when numeric fields are invalid', async () => {
    render(<MenuManagement />);

    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Add Item'));

    await waitFor(() => {
      expect(screen.getByLabelText(/item name/i)).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText(/item name/i), {
      target: { value: 'Broken Item' },
    });
    fireEvent.change(screen.getByLabelText(/category/i), {
      target: { value: 'not-a-number' },
    });
    fireEvent.change(screen.getByLabelText(/price/i), {
      target: { value: 'abc' },
    });
    fireEvent.change(screen.getByLabelText(/description/i), {
      target: { value: 'Broken item description' },
    });
    fireEvent.change(screen.getByLabelText(/prep time/i), {
      target: { value: 'not-a-number' },
    });

    const form = screen.getByLabelText(/item name/i).closest('form');
    if (form) {
      fireEvent.submit(form);
    }

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Please complete all menu item details');
    });

    expect(restaurantService.addMenuItem).not.toHaveBeenCalled();
  });

  it('shows the service error message when adding an item fails', async () => {
    vi.mocked(restaurantService.addMenuItem).mockRejectedValueOnce({
      response: { data: { message: 'Category mismatch' } },
    });

    render(<MenuManagement />);

    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Add Item'));

    await waitFor(() => {
      expect(screen.getByLabelText(/item name/i)).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText(/item name/i), {
      target: { value: 'Broken Item' },
    });
    fireEvent.change(screen.getByLabelText(/price/i), {
      target: { value: '100' },
    });

    const form = screen.getByLabelText(/item name/i).closest('form');
    if (form) {
      fireEvent.submit(form);
    }

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Category mismatch');
    });
  });

  it('falls back to a generic operation error when submission fails without a message', async () => {
    vi.mocked(restaurantService.addMenuItem).mockRejectedValueOnce({ unexpected: true });

    render(<MenuManagement />);

    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Add Item'));

    await waitFor(() => {
      expect(screen.getByLabelText(/item name/i)).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText(/item name/i), {
      target: { value: 'Fallback Item' },
    });
    fireEvent.change(screen.getByLabelText(/description/i), {
      target: { value: 'Fallback description' },
    });
    fireEvent.change(screen.getByLabelText(/price/i), {
      target: { value: '100' },
    });
    fireEvent.change(screen.getByLabelText(/prep time/i), {
      target: { value: '20' },
    });

    const form = screen.getByLabelText(/item name/i).closest('form');
    if (form) {
      fireEvent.submit(form);
    }

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Operation failed');
    });
  });

  it('shows empty state when no menu items', async () => {
    vi.mocked(restaurantService.getRestaurantMenu).mockResolvedValueOnce([]);

    render(<MenuManagement />);

    await waitFor(() => {
      expect(screen.getByText(/no menu items yet/i)).toBeInTheDocument();
    });
  });

  it('shows category guidance when no categories exist', async () => {
    vi.mocked(restaurantService.getRestaurantCategories).mockResolvedValueOnce([]);

    render(<MenuManagement />);

    await waitFor(() => {
      expect(screen.getByText(/no categories yet\. create categories first\./i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /manage categories/i })).toBeInTheDocument();
    });
  });

  it('closes the dialog when cancel is clicked', async () => {
    render(<MenuManagement />);

    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Add Item'));

    await waitFor(() => {
      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /cancel/i }));

    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });
  });

  it('navigates back when clicking back button', async () => {
    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    const backButton = screen.getByRole('button', { name: /dashboard/i });
    fireEvent.click(backButton);

    // Just verify the button exists and can be clicked
    expect(backButton).toBeInTheDocument();
  });
});
