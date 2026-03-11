/**
 * Example Component: Restaurant List with API Integration
 * 
 * This component demonstrates how to:
 * 1. Fetch data from the backend API
 * 2. Handle loading and error states
 * 3. Display paginated results
 * 4. Implement search functionality
 * 
 * Usage:
 * import RestaurantListApi from '@/app/examples/RestaurantListApi';
 * 
 * Then use <RestaurantListApi /> in your page
 */

import React, { useState, useEffect } from 'react';
import { restaurantService } from '@/app/services';
import type { Restaurant, SearchFilters } from '@/app/services';

export default function RestaurantListApi() {
  // State management
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [searchTerm, setSearchTerm] = useState<string>('');

  // Fetch restaurants on component mount and when page changes
  useEffect(() => {
    fetchRestaurants();
  }, [page]);

  /**
   * Fetch restaurants from API
   */
  const fetchRestaurants = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await restaurantService.getAllRestaurants(page, 20, 'name', 'asc');
      
      setRestaurants(response.content);
      setTotalPages(response.totalPages);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch restaurants');
      console.error('Error fetching restaurants:', err);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Search restaurants
   */
  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!searchTerm.trim()) {
      fetchRestaurants();
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const filters: SearchFilters = {
        searchTerm: searchTerm,
        page: 0,
        size: 20,
        sortBy: 'name',
        sortDir: 'asc',
      };

      const response = await restaurantService.searchRestaurants(filters);
      
      setRestaurants(response.content);
      setTotalPages(response.totalPages);
      setPage(0);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Search failed');
      console.error('Error searching restaurants:', err);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Filter by cuisine
   */
  const filterByCuisine = async (cuisineType: string) => {
    try {
      setLoading(true);
      setError(null);

      const response = await restaurantService.getRestaurantsByCuisine(cuisineType, 0, 20);
      
      setRestaurants(response.content);
      setTotalPages(response.totalPages);
      setPage(0);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to filter restaurants');
      console.error('Error filtering restaurants:', err);
    } finally {
      setLoading(false);
    }
  };

  // Loading state
  if (loading && restaurants.length === 0) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading restaurants...</p>
        </div>
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center max-w-md mx-auto p-6 bg-red-50 rounded-lg">
          <h2 className="text-xl font-semibold text-red-600 mb-2">Error</h2>
          <p className="text-red-700">{error}</p>
          <button
            onClick={fetchRestaurants}
            className="mt-4 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">Restaurants</h1>

      {/* Search Bar */}
      <form onSubmit={handleSearch} className="mb-6">
        <div className="flex gap-2">
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search restaurants..."
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            type="submit"
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            Search
          </button>
          <button
            type="button"
            onClick={fetchRestaurants}
            className="px-6 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700"
          >
            Reset
          </button>
        </div>
      </form>

      {/* Cuisine Filters */}
      <div className="mb-6 flex gap-2 flex-wrap">
        <button
          onClick={() => filterByCuisine('Thai')}
          className="px-4 py-2 bg-orange-100 text-orange-700 rounded-full hover:bg-orange-200"
        >
          Thai
        </button>
        <button
          onClick={() => filterByCuisine('Japanese')}
          className="px-4 py-2 bg-red-100 text-red-700 rounded-full hover:bg-red-200"
        >
          Japanese
        </button>
        <button
          onClick={() => filterByCuisine('Western')}
          className="px-4 py-2 bg-blue-100 text-blue-700 rounded-full hover:bg-blue-200"
        >
          Western
        </button>
        <button
          onClick={() => filterByCuisine('Chinese')}
          className="px-4 py-2 bg-yellow-100 text-yellow-700 rounded-full hover:bg-yellow-200"
        >
          Chinese
        </button>
      </div>

      {/* Restaurant Grid */}
      {restaurants.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-600">No restaurants found</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {restaurants.map((restaurant) => (
            <div
              key={restaurant.id}
              className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow"
            >
              {(restaurant.coverImageUrl ?? restaurant.logoUrl) && (
                <img
                  src={restaurant.coverImageUrl ?? restaurant.logoUrl}
                  alt={restaurant.name}
                  className="w-full h-48 object-cover"
                />
              )}
              <div className="p-4">
                <h3 className="text-xl font-semibold mb-2">{restaurant.name}</h3>
                {restaurant.description && (
                  <p className="text-gray-600 text-sm mb-2">{restaurant.description}</p>
                )}
                <div className="flex items-center justify-between">
                  <span className="inline-block px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-sm">
                    {restaurant.cuisineType}
                  </span>
                  <div className="flex items-center">
                    <span className="text-yellow-500">★</span>
                    <span className="ml-1 text-gray-700">{restaurant.averageRating.toFixed(1)}</span>
                  </div>
                </div>
                {restaurant.estimatedDeliveryTime && (
                  <p className="text-gray-500 text-sm mt-2">
                    🕐 {restaurant.estimatedDeliveryTime}
                  </p>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="mt-8 flex justify-center gap-2">
          <button
            onClick={() => setPage(Math.max(0, page - 1))}
            disabled={page === 0}
            className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-300"
          >
            Previous
          </button>
          <span className="px-4 py-2">
            Page {page + 1} of {totalPages}
          </span>
          <button
            onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
            disabled={page >= totalPages - 1}
            className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-300"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}
