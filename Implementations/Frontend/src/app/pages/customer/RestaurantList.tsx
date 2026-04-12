import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select';
import { Card, CardContent } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { useApp } from '../../contexts/AppContext';
import restaurantService from '../../services/restaurant.service';
import type { Restaurant } from '../../services/restaurant.service';
import { ShoppingCart, Search, Clock, Star, LogOut, Loader2, UtensilsCrossed } from 'lucide-react';
import { ImageWithFallback } from '../../components/figma/ImageWithFallback';

export default function RestaurantList() {
  const navigate = useNavigate();
  const { user, cart, logout } = useApp();
  const [searchQuery, setSearchQuery] = useState('');
  const [cuisineFilter, setCuisineFilter] = useState('All');
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [cuisineTypes, setCuisineTypes] = useState<string[]>(['All']);
  const [loading, setLoading] = useState(true);
  const [total, setTotal] = useState(0);

  const loadRestaurants = useCallback(async () => {
    setLoading(true);
    try {
      const searchTerm = searchQuery.trim() || undefined;
      const cuisine = cuisineFilter !== 'All' ? cuisineFilter : undefined;
      const data = await restaurantService.searchRestaurants({ searchTerm, cuisineType: cuisine });
      const list: Restaurant[] = Array.isArray(data) ? data : ((data as any)?.content ?? []);
      setRestaurants(list);
      setTotal((data as any)?.totalElements ?? list.length);
    } catch {
      setRestaurants([]);
      setTotal(0);
    } finally {
      setLoading(false);
    }
  }, [searchQuery, cuisineFilter]);

  // Load cuisine types once
  useEffect(() => {
    restaurantService.getCuisineTypes?.().then(types => {
      if (types?.length) setCuisineTypes(['All', ...types]);
    }).catch(() => {});
  }, []);

  // Debounced load on filter change
  useEffect(() => {
    const timer = setTimeout(loadRestaurants, 300);
    return () => clearTimeout(timer);
  }, [loadRestaurants]);

  const cartItemCount = cart.reduce((sum, item) => sum + item.quantity, 0);

  const getRestaurantName = (restaurant: Restaurant) => (
    typeof restaurant.name === 'string' && restaurant.name.trim().length > 0
      ? restaurant.name
      : 'Restaurant'
  );

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h1 className="text-2xl">🍔 FoodExpress</h1>
              <p className="text-sm text-gray-500">Welcome, {user?.name}</p>
            </div>
            <div className="flex gap-2">
              <Button 
                variant="outline"
                onClick={() => navigate('/customer/orders')}
              >
                My Orders
              </Button>
              <Button 
                variant="outline"
                onClick={() => navigate('/customer/checkout')}
                className="relative"
              >
                <ShoppingCart className="w-4 h-4 mr-2" />
                Cart
                {cartItemCount > 0 && (
                  <Badge className="absolute -top-2 -right-2 h-5 w-5 flex items-center justify-center p-0">
                    {cartItemCount}
                  </Badge>
                )}
              </Button>
              <Button 
                variant="ghost"
                onClick={() => {
                  logout();
                  navigate('/login');
                }}
              >
                <LogOut className="w-4 h-4" />
              </Button>
            </div>
          </div>

          {/* Filters */}
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <Input
                placeholder="Search restaurants..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10"
              />
            </div>
            <Select value={cuisineFilter} onValueChange={setCuisineFilter}>
              <SelectTrigger className="w-full sm:w-48">
                <SelectValue placeholder="Cuisine Type" />
              </SelectTrigger>
              <SelectContent>
                {cuisineTypes.map(cuisine => (
                  <SelectItem key={cuisine} value={cuisine}>{cuisine}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
      </div>

      {/* Restaurant Grid */}
      <div className="max-w-7xl mx-auto px-4 py-8">
        {loading ? (
          <div className="flex justify-center py-16">
            <Loader2 className="w-8 h-8 animate-spin text-gray-400" />
          </div>
        ) : (
          <>
            <p className="text-sm text-gray-600 mb-4">{total} restaurants found</p>
            <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {restaurants.map(restaurant => (
                <Card
                  key={restaurant.id}
                  className="hover:shadow-lg transition-shadow cursor-pointer"
                  onClick={() => navigate(`/customer/restaurant/${restaurant.id}`)}
                >
                  <div className="relative h-48 overflow-hidden rounded-t-lg">
                    <ImageWithFallback
                      src={restaurant.coverImageUrl ?? restaurant.logoUrl ?? ''}
                      alt={getRestaurantName(restaurant)}
                      className="w-full h-full object-cover"
                    />
                    <Badge className="absolute top-2 right-2">
                      {restaurant.cuisineType}
                    </Badge>
                  </div>
                  <CardContent className="pt-4">
                    <h3 className="font-semibold mb-2">{getRestaurantName(restaurant)}</h3>
                    <div className="space-y-1 text-sm text-gray-600">
                      <div className="flex items-center gap-1">
                        <Star className="w-4 h-4 text-yellow-500 fill-yellow-500" />
                        <span>{restaurant.totalReviews ? restaurant.averageRating?.toFixed(1) : 'New'}</span>
                        <span className="text-xs text-gray-500">({restaurant.totalReviews ?? 0})</span>
                      </div>
                      {!!restaurant.estimatedDeliveryTime && (
                        <div className="flex items-center gap-1">
                          <Clock className="w-4 h-4" />
                          <span>{restaurant.estimatedDeliveryTime} min</span>
                        </div>
                      )}
                      {restaurant.cuisineType && (
                        <div className="flex items-center gap-1">
                          <UtensilsCrossed className="w-4 h-4" />
                          <span>{restaurant.cuisineType}</span>
                        </div>
                      )}
                      {restaurant.minimumOrderAmount != null && (
                        <p className="text-xs">Min. order: ฿{restaurant.minimumOrderAmount}</p>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>

            {restaurants.length === 0 && (
              <div className="text-center py-12">
                <p className="text-gray-500">No restaurants found matching your criteria</p>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
