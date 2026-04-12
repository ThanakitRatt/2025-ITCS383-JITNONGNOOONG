import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router';
import { Button } from '../../components/ui/button';
import { Card } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '../../components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select';
import { useApp } from '../../contexts/AppContext';
import restaurantService, { Restaurant, MenuItem, RestaurantReview, ReviewSort } from '../../services/restaurant.service';
import { ArrowLeft, Star, Clock, Plus, Minus, ShoppingCart, Loader2, UtensilsCrossed } from 'lucide-react';
import { ImageWithFallback } from '../../components/figma/ImageWithFallback';
import { toast } from 'sonner';

export default function RestaurantDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { addToCart, cart } = useApp();
  const [selectedItem, setSelectedItem] = useState<MenuItem | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
  const [menu, setMenu] = useState<MenuItem[]>([]);
  const [reviews, setReviews] = useState<RestaurantReview[]>([]);
  const [reviewSort, setReviewSort] = useState<ReviewSort>('recent');
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    Promise.all([
      restaurantService.getRestaurantById(id),
      restaurantService.getRestaurantMenu(id),
      restaurantService.getRestaurantReviews(id, reviewSort).catch(() => []),
    ])
      .then(([restaurantData, menuData, reviewData]) => {
        setRestaurant(restaurantData);
        setMenu(menuData);
        setReviews(reviewData);
      })
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false));
  }, [id, reviewSort]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
      </div>
    );
  }

  if (notFound || !restaurant) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <UtensilsCrossed className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <p className="text-gray-500">Restaurant not found</p>
          <Button onClick={() => navigate('/customer/restaurants')} className="mt-4">
            Back to Restaurants
          </Button>
        </div>
      </div>
    );
  }

  const categories = Array.from(new Set(menu.filter(i => i.isAvailable).map(item => item.categoryName ?? 'Other')));
  const reviewSummaryRating =
    typeof restaurant.averageRating === 'number' ? restaurant.averageRating.toFixed(1) : 'N/A';

  const handleAddToCart = () => {
    if (selectedItem && restaurant) {
      addToCart({
        id: selectedItem.id,
        name: selectedItem.name,
        price: selectedItem.price,
        quantity: quantity,
        restaurantId: restaurant.id,
        restaurantName: restaurant.name
      });
      toast.success(`${selectedItem.name} added to cart`);
      setSelectedItem(null);
      setQuantity(1);
    }
  };

  const cartItemCount = cart.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <Button variant="ghost" onClick={() => navigate('/customer/restaurants')}>
              <ArrowLeft className="w-4 h-4 mr-2" />
              Back
            </Button>
            <Button 
              onClick={() => navigate('/customer/checkout')}
              className="relative"
            >
              <ShoppingCart className="w-4 h-4 mr-2" />
              Cart ({cartItemCount})
            </Button>
          </div>
        </div>
      </div>

      {/* Restaurant Info */}
      <div className="relative h-64 overflow-hidden">
        <ImageWithFallback
          src={restaurant.coverImageUrl ?? restaurant.logoUrl ?? ''}
          alt={restaurant.name}
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
        <div className="absolute bottom-0 left-0 right-0 p-6 text-white">
          <div className="max-w-7xl mx-auto">
            <h1 className="text-3xl mb-2">{restaurant.name}</h1>
            <div className="flex flex-wrap gap-4 text-sm">
              <div className="flex items-center gap-1">
                <Star className="w-4 h-4 fill-yellow-500 text-yellow-500" />
                <span>{restaurant.averageRating?.toFixed(1) ?? 'N/A'}</span>
                <span className="text-white/80">({restaurant.totalReviews ?? 0} reviews)</span>
              </div>
              <div className="flex items-center gap-1">
                <Clock className="w-4 h-4" />
                <span>{restaurant.estimatedDeliveryTime == null ? 'N/A' : `${restaurant.estimatedDeliveryTime} min`}</span>
              </div>
              <Badge>{restaurant.cuisineType}</Badge>
            </div>
          </div>
        </div>
      </div>

      {/* Menu */}
      <div className="max-w-7xl mx-auto px-4 py-8">
        {!!restaurant.minimumOrderAmount && (
          <p className="text-sm text-gray-600 mb-6">Minimum order: ฿{restaurant.minimumOrderAmount}</p>
        )}
        
        {categories.length === 0 && (
          <div className="text-center py-12">
            <UtensilsCrossed className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <p className="text-gray-500">No menu items available</p>
          </div>
        )}

        {categories.map(category => (
          <div key={category} className="mb-8">
            <h2 className="text-xl mb-4">{category}</h2>
            <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {menu
                .filter(item => (item.categoryName ?? 'Other') === category && item.isAvailable)
                .map(item => (
                  <Card 
                    key={item.id}
                    className="cursor-pointer hover:shadow-lg transition-shadow"
                    onClick={() => {
                      setSelectedItem(item);
                      setQuantity(1);
                    }}
                  >
                    <div className="flex gap-4 p-4">
                      <ImageWithFallback
                        src={item.imageUrl ?? ''}
                        alt={item.name}
                        className="w-24 h-24 object-cover rounded"
                      />
                      <div className="flex-1">
                        <h3 className="font-semibold mb-1">{item.name}</h3>
                        <p className="text-sm text-gray-600 mb-2 line-clamp-2">
                          {item.description}
                        </p>
                        <p className="font-semibold text-blue-600">฿{item.price}</p>
                      </div>
                    </div>
                  </Card>
                ))}
            </div>
          </div>
        ))}

        <div className="mt-12">
          <div className="mb-6 flex flex-col gap-4 rounded-2xl border bg-white p-5 shadow-sm md:flex-row md:items-center md:justify-between">
            <div>
              <p className="text-sm uppercase tracking-wide text-gray-500">Customer Reviews</p>
              <div className="mt-2 flex items-center gap-3">
                <Star className="h-5 w-5 fill-yellow-500 text-yellow-500" />
                <span className="text-2xl font-semibold">{reviewSummaryRating}</span>
                <span className="text-sm text-gray-500">{restaurant.totalReviews ?? 0} total reviews</span>
              </div>
            </div>

            <div className="w-full md:w-56">
              <Select value={reviewSort} onValueChange={(value) => setReviewSort(value as ReviewSort)}>
                <SelectTrigger>
                  <SelectValue placeholder="Sort reviews" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="recent">Most Recent</SelectItem>
                  <SelectItem value="highest">Highest Rated</SelectItem>
                  <SelectItem value="lowest">Lowest Rated</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {reviews.length === 0 ? (
            <Card className="p-6 text-center text-gray-500">No reviews yet for this restaurant.</Card>
          ) : (
            <div className="space-y-4">
              {reviews.map((review) => (
                <Card key={review.id} className="p-5">
                  <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                    <div>
                      <p className="font-semibold">{review.customerName}</p>
                      <div className="mt-1 flex items-center gap-2">
                        <div className="flex items-center gap-1">
                          {[1, 2, 3, 4, 5].map((starValue) => (
                            <Star
                              key={starValue}
                              className={`h-4 w-4 ${starValue <= review.rating ? 'fill-yellow-500 text-yellow-500' : 'text-gray-300'}`}
                            />
                          ))}
                        </div>
                        <span className="text-sm text-gray-500">{new Date(review.createdAt).toLocaleDateString()}</span>
                      </div>
                    </div>
                    <Badge variant="secondary">{review.rating}/5</Badge>
                  </div>
                  {review.reviewText && (
                    <p className="mt-3 text-sm leading-6 text-gray-700">{review.reviewText}</p>
                  )}
                </Card>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Add to Cart Dialog */}
      <Dialog open={!!selectedItem} onOpenChange={() => setSelectedItem(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{selectedItem?.name}</DialogTitle>
            <DialogDescription>
              Adjust the quantity and add this menu item to your cart.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <ImageWithFallback
              src={selectedItem?.imageUrl ?? ''}
              alt={selectedItem?.name || ''}
              className="w-full h-48 object-cover rounded"
            />
            <p className="text-gray-600">{selectedItem?.description}</p>
            <div className="flex items-center justify-between">
              <span className="text-lg">Price: ฿{selectedItem?.price}</span>
              <div className="flex items-center gap-3">
                <Button 
                  variant="outline" 
                  size="sm"
                  onClick={() => setQuantity(Math.max(1, quantity - 1))}
                >
                  <Minus className="w-4 h-4" />
                </Button>
                <span className="w-8 text-center">{quantity}</span>
                <Button 
                  variant="outline" 
                  size="sm"
                  onClick={() => setQuantity(quantity + 1)}
                >
                  <Plus className="w-4 h-4" />
                </Button>
              </div>
            </div>
            <div className="flex items-center justify-between pt-4 border-t">
              <span className="font-semibold">Total: ฿{(selectedItem?.price || 0) * quantity}</span>
              <Button onClick={handleAddToCart}>
                Add to Cart
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
