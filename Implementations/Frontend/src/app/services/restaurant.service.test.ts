import { describe, it, expect, vi, beforeEach } from 'vitest';
import type { AxiosResponse } from 'axios';

vi.mock('./apiClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
  handleApiError: vi.fn((e: unknown) => (e instanceof Error ? e.message : 'error')),
}));

import apiClient from './apiClient';
import restaurantService from './restaurant.service';

function fakeResponse<T>(data: T): AxiosResponse<{ success: boolean; data: T }> {
  return { data: { success: true, data } } as AxiosResponse<{ success: boolean; data: T }>;
}

const mockRestaurant = {
  id: '1',
  name: 'Test Restaurant',
  cuisineType: 'THAI',
  address: '123 Test St',
  phoneNumber: '+66-2-111-2222',
  isActive: true,
  averageRating: 4.5,
};

const mockPaginated = {
  content: [mockRestaurant],
  page: 0,
  size: 20,
  totalElements: 1,
  totalPages: 1,
};

beforeEach(() => {
  vi.resetAllMocks();
});

// ========== getAllRestaurants ==========
describe('getAllRestaurants', () => {
  it('returns paginated restaurant list', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse(mockPaginated));

    const result = await restaurantService.getAllRestaurants();

    expect(result).toEqual(mockPaginated);
    expect(apiClient.get).toHaveBeenCalledOnce();
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('Network Error'));

    await expect(restaurantService.getAllRestaurants()).rejects.toThrow();
  });
});

// ========== searchRestaurants ==========
describe('searchRestaurants', () => {
  it('returns filtered restaurants', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse(mockPaginated));

    const result = await restaurantService.searchRestaurants({ searchTerm: 'Thai' });

    expect(result).toEqual(mockPaginated);
    expect(apiClient.get).toHaveBeenCalledOnce();
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('server error'));

    await expect(restaurantService.searchRestaurants({})).rejects.toThrow();
  });
});

// ========== getRestaurantById ==========
describe('getRestaurantById', () => {
  it('returns a single restaurant', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse(mockRestaurant));

    const result = await restaurantService.getRestaurantById('1');

    expect(result).toEqual(mockRestaurant);
    expect(apiClient.get).toHaveBeenCalledOnce();
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('not found'));

    await expect(restaurantService.getRestaurantById('99')).rejects.toThrow();
  });
});

// ========== getRestaurantsByCuisine ==========
describe('getRestaurantsByCuisine', () => {
  it('returns restaurants by cuisine', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse(mockPaginated));

    const result = await restaurantService.getRestaurantsByCuisine('THAI');

    expect(result).toEqual(mockPaginated);
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.getRestaurantsByCuisine('THAI')).rejects.toThrow();
  });
});

// ========== getCuisineTypes ==========
describe('getCuisineTypes', () => {
  it('returns list of cuisine types', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse(['THAI', 'ITALIAN']));

    const result = await restaurantService.getCuisineTypes();

    expect(result).toEqual(['THAI', 'ITALIAN']);
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.getCuisineTypes()).rejects.toThrow();
  });
});

// ========== getTopRatedRestaurants ==========
describe('getTopRatedRestaurants', () => {
  it('returns top rated restaurants', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse([mockRestaurant]));

    const result = await restaurantService.getTopRatedRestaurants(5);

    expect(result).toEqual([mockRestaurant]);
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.getTopRatedRestaurants()).rejects.toThrow();
  });
});

// ========== getRestaurantMenu ==========
describe('getRestaurantMenu', () => {
  it('returns menu items array from paginated response', async () => {
    const menuItem = { id: '1', restaurantId: '1', name: 'Pad Thai', description: 'Noodles', price: 120, isAvailable: true };
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse({ content: [menuItem] }));

    const result = await restaurantService.getRestaurantMenu('1');

    expect(result).toEqual([menuItem]);
  });

  it('returns empty array when content is null', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse({ content: null }));

    const result = await restaurantService.getRestaurantMenu('1');

    expect(result).toEqual([]);
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.getRestaurantMenu('1')).rejects.toThrow();
  });
});

// ========== getMenuItem ==========
describe('getMenuItem', () => {
  it('returns a single menu item', async () => {
    const menuItem = { id: '1', name: 'Pad Thai', price: 120, isAvailable: true };
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse(menuItem));

    const result = await restaurantService.getMenuItem('1', '1');

    expect(result).toEqual(menuItem);
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.getMenuItem('1', '99')).rejects.toThrow();
  });
});

// ========== createRestaurant ==========
describe('createRestaurant', () => {
  it('returns the created restaurant', async () => {
    vi.mocked(apiClient.post).mockResolvedValueOnce(fakeResponse(mockRestaurant));

    const result = await restaurantService.createRestaurant({
      name: 'Test Restaurant',
      address: '123 Test St',
      phoneNumber: '+66-2-111-2222',
      email: 'test@example.com',
      cuisineType: 'THAI',
      latitude: 13.75,
      longitude: 100.5,
      deliveryFee: 35,
      minimumOrderAmount: 100,
      openingTime: '09:00',
      closingTime: '21:00',
      estimatedDeliveryTime: 30,
      ownerId: 1,
    });

    expect(result).toEqual(mockRestaurant);
    expect(apiClient.post).toHaveBeenCalledOnce();
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.post).mockRejectedValueOnce(new Error('error'));

    await expect(
      restaurantService.createRestaurant({ name: 'X', address: 'Y', phoneNumber: 'Z', email: 'x@x.com', cuisineType: 'THAI', latitude: 0, longitude: 0, deliveryFee: 0, minimumOrderAmount: 0, openingTime: '00:00', closingTime: '23:59', estimatedDeliveryTime: 30, ownerId: 1 }),
    ).rejects.toThrow();
  });
});

// ========== addMenuItem ==========
describe('addMenuItem', () => {
  it('returns the created menu item', async () => {
    const menuItem = { id: '2', name: 'Tom Yum', price: 150, isAvailable: true };
    vi.mocked(apiClient.post).mockResolvedValueOnce(fakeResponse(menuItem));

    const result = await restaurantService.addMenuItem('1', {
      name: 'Tom Yum',
      description: 'Spicy soup',
      price: 150,
      categoryId: 1,
    });

    expect(result).toEqual(menuItem);
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.post).mockRejectedValueOnce(new Error('error'));

    await expect(
      restaurantService.addMenuItem('1', { name: 'X', description: '', price: 0, categoryId: 1 }),
    ).rejects.toThrow();
  });
});

// ========== addMenuCategory ==========
describe('addMenuCategory', () => {
  it('returns the created category', async () => {
    const category = { id: '1', name: 'Main Dishes', restaurantId: '1', displayOrder: 1 };
    vi.mocked(apiClient.post).mockResolvedValueOnce(fakeResponse(category));

    const result = await restaurantService.addMenuCategory('1', { name: 'Main Dishes' });

    expect(result).toEqual(category);
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.post).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.addMenuCategory('1', { name: 'X' })).rejects.toThrow();
  });
});

// ========== updateRestaurant ==========
describe('updateRestaurant', () => {
  it('returns the updated restaurant', async () => {
    const updated = { ...mockRestaurant, name: 'Updated Name' };
    vi.mocked(apiClient.put).mockResolvedValueOnce(fakeResponse(updated));

    const result = await restaurantService.updateRestaurant('1', { name: 'Updated Name' });

    expect(result.name).toBe('Updated Name');
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.put).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.updateRestaurant('1', {})).rejects.toThrow();
  });
});

// ========== updateMenuItem ==========
describe('updateMenuItem', () => {
  it('returns the updated menu item', async () => {
    const updated = { id: '1', name: 'Updated Item', price: 200, isAvailable: true };
    vi.mocked(apiClient.put).mockResolvedValueOnce(fakeResponse(updated));

    const result = await restaurantService.updateMenuItem('1', '1', { price: 200 });

    expect(result).toEqual(updated);
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.put).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.updateMenuItem('1', '1', {})).rejects.toThrow();
  });
});

// ========== updateRestaurantStatus ==========
describe('updateRestaurantStatus', () => {
  it('returns the updated restaurant', async () => {
    vi.mocked(apiClient.put).mockResolvedValueOnce(fakeResponse(mockRestaurant));

    const result = await restaurantService.updateRestaurantStatus('1', 'ACTIVE');

    expect(result).toEqual(mockRestaurant);
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.put).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.updateRestaurantStatus('1', 'INACTIVE')).rejects.toThrow();
  });
});

// ========== toggleRestaurantAvailability ==========
describe('toggleRestaurantAvailability', () => {
  it('returns updated restaurant on success', async () => {
    vi.mocked(apiClient.put).mockResolvedValueOnce(fakeResponse(mockRestaurant));

    const result = await restaurantService.toggleRestaurantAvailability('1', true);

    expect(result).toEqual(mockRestaurant);
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.put).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.toggleRestaurantAvailability('1', false)).rejects.toThrow();
  });
});

// ========== deleteRestaurant ==========
describe('deleteRestaurant', () => {
  it('calls delete endpoint', async () => {
    vi.mocked(apiClient.delete).mockResolvedValueOnce({});

    await restaurantService.deleteRestaurant('1');

    expect(apiClient.delete).toHaveBeenCalledOnce();
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.delete).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.deleteRestaurant('1')).rejects.toThrow();
  });
});

// ========== deleteMenuItem ==========
describe('deleteMenuItem', () => {
  it('calls delete endpoint', async () => {
    vi.mocked(apiClient.delete).mockResolvedValueOnce({});

    await restaurantService.deleteMenuItem('1', '1');

    expect(apiClient.delete).toHaveBeenCalledOnce();
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.delete).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.deleteMenuItem('1', '99')).rejects.toThrow();
  });
});

// ========== deleteMenuCategory ==========
describe('deleteMenuCategory', () => {
  it('calls delete endpoint', async () => {
    vi.mocked(apiClient.delete).mockResolvedValueOnce({});

    await restaurantService.deleteMenuCategory('1', '1');

    expect(apiClient.delete).toHaveBeenCalledOnce();
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.delete).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.deleteMenuCategory('1', '1')).rejects.toThrow();
  });
});

// ========== getOwnerRestaurants ==========
describe('getOwnerRestaurants', () => {
  it('returns list of owner restaurants', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse([mockRestaurant]));

    const result = await restaurantService.getOwnerRestaurants('owner1');

    expect(result).toEqual([mockRestaurant]);
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.getOwnerRestaurants('owner1')).rejects.toThrow();
  });
});

// ========== getRestaurantCategories ==========
describe('getRestaurantCategories', () => {
  it('returns list of categories', async () => {
    const categories = [{ id: '1', name: 'Main Dishes', restaurantId: '1', displayOrder: 1 }];
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse(categories));

    const result = await restaurantService.getRestaurantCategories('1');

    expect(result).toEqual(categories);
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.getRestaurantCategories('1')).rejects.toThrow();
  });
});

// ========== searchMenuItems ==========
describe('searchMenuItems', () => {
  it('returns paginated menu items matching the search term', async () => {
    const menuItem = { id: '1', name: 'Pad Thai', price: 120, isAvailable: true };
    vi.mocked(apiClient.get).mockResolvedValueOnce(
      fakeResponse({ content: [menuItem], page: 0, size: 20, totalElements: 1, totalPages: 1 }),
    );

    const result = await restaurantService.searchMenuItems('1', 'Pad');

    expect(result.content).toEqual([menuItem]);
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('error'));

    await expect(restaurantService.searchMenuItems('1', 'Pad')).rejects.toThrow();
  });
});

// ========== calculateDistance ==========
describe('calculateDistance', () => {
  it('returns 0 for identical coordinates', () => {
    expect(restaurantService.calculateDistance(13.7, 100.5, 13.7, 100.5)).toBe(0);
  });

  it('calculates distance between Bangkok and Chiang Mai (~600km)', () => {
    // Bangkok ~13.75, 100.52 | Chiang Mai ~18.79, 98.98
    const dist = restaurantService.calculateDistance(13.75, 100.52, 18.79, 98.98);
    expect(dist).toBeGreaterThan(500);
    expect(dist).toBeLessThan(800);
  });

  it('returns a positive number for different coordinates', () => {
    const dist = restaurantService.calculateDistance(0, 0, 1, 1);
    expect(dist).toBeGreaterThan(0);
  });
});
