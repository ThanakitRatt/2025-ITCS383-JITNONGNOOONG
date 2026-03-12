import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { useApp } from '../../contexts/AppContext';
import { Package, UtensilsCrossed, Tag, ArrowLeft, TrendingUp, Loader2 } from 'lucide-react';
import restaurantService from '../../services/restaurant.service';
import orderService from '../../services/order.service';
import type { Restaurant as RestaurantData } from '../../services/restaurant.service';
import type { Order } from '../../services/order.service';

export default function RestaurantDashboard() {
  const navigate = useNavigate();
  const { user, logout } = useApp();
  const ownerId = user?.id?.toString() || '1';

  const [restaurant, setRestaurant] = useState<RestaurantData | null>(null);
  const [recentOrders, setRecentOrders] = useState<Order[]>([]);
  const [menuCount, setMenuCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadDashboard = async () => {
      setLoading(true);
      try {
        // First, get the restaurant data by owner ID
        const ownerRestaurants = await restaurantService.getOwnerRestaurants(ownerId).catch(() => null);
        if (!ownerRestaurants || ownerRestaurants.length === 0) {
          console.error('No restaurant found for owner:', ownerId);
          setLoading(false);
          return;
        }

        const restaurantData = ownerRestaurants[0];
        setRestaurant(restaurantData);
        
        // Now fetch menu and orders using the actual restaurant ID
        const restaurantId = restaurantData.id.toString();
        const [menuResult, ordersPage] = await Promise.all([
          restaurantService.getRestaurantMenu(restaurantId).catch(() => null),
          orderService.getRestaurantOrders(restaurantId, 0, 20).catch(() => null),
        ]);

        if (menuResult) {
          // Handle both array and paginated response
          const count = Array.isArray(menuResult)
            ? menuResult.length
            : ((menuResult as any)?.content?.length ?? (menuResult as any)?.totalElements ?? 0);
          setMenuCount(count);
        }

        const orders: Order[] = ordersPage?.content ?? [];
        setRecentOrders(orders);
      } catch {
        // fail silently — individual errors already caught above
      } finally {
        setLoading(false);
      }
    };

    loadDashboard();
  }, [ownerId]);

  const handleExit = () => {
    logout();
    navigate('/login');
  };

  const today = new Date().toDateString();
  const todayOrders = recentOrders.filter(o => new Date(o.createdAt).toDateString() === today);
  const todayRevenue = todayOrders.reduce((sum, o) => sum + (o.totalAmount ?? 0), 0);
  const pendingOrders = recentOrders.filter(o =>
    ['PENDING', 'CONFIRMED', 'PREPARING'].includes(o.status)
  ).length;
  const completedToday = todayOrders.filter(o => o.status === 'DELIVERED').length;

  const cards = [
    {
      title: 'Pending Orders',
      value: loading ? '...' : pendingOrders,
      icon: Package,
      color: 'text-blue-600',
      bgColor: 'bg-blue-50',
      action: () => navigate('/restaurant/orders')
    },
    {
      title: "Today's Revenue",
      value: loading ? '...' : `฿${todayRevenue.toFixed(0)}`,
      icon: TrendingUp,
      color: 'text-green-600',
      bgColor: 'bg-green-50'
    },
    {
      title: 'Menu Items',
      value: loading ? '...' : menuCount,
      icon: UtensilsCrossed,
      color: 'text-purple-600',
      bgColor: 'bg-purple-50',
      action: () => navigate('/restaurant/menu')
    },
    {
      title: 'Completed Today',
      value: loading ? '...' : completedToday,
      icon: Tag,
      color: 'text-orange-600',
      bgColor: 'bg-orange-50'
    }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl">🍔 Restaurant Portal</h1>
              <p className="text-sm text-gray-500">
                {loading ? <Loader2 className="w-4 h-4 inline animate-spin" /> : (restaurant?.name ?? user?.name ?? 'My Restaurant')}
              </p>
            </div>
            <Button variant="ghost" onClick={handleExit}>
              <ArrowLeft className="w-4 h-4 mr-2" />
              Exit
            </Button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-8">
        {/* Quick Stats */}
        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {cards.map((card, idx) => {
            const Icon = card.icon;
            return (
              <Card 
                key={idx} 
                className={card.action ? 'cursor-pointer hover:shadow-lg transition-shadow' : ''}
                onClick={card.action}
              >
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-gray-600">{card.title}</p>
                      <p className="text-3xl mt-2">{card.value}</p>
                    </div>
                    <div className={`${card.bgColor} p-3 rounded-full`}>
                      <Icon className={`w-6 h-6 ${card.color}`} />
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>

        {/* Quick Actions */}
        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-3 gap-4">
              <Button 
                className="h-24 text-lg"
                onClick={() => navigate('/restaurant/orders')}
              >
                <Package className="w-6 h-6 mr-2" />
                View Orders
              </Button>
              <Button 
                className="h-24 text-lg"
                variant="outline"
                onClick={() => navigate('/restaurant/menu')}
              >
                <UtensilsCrossed className="w-6 h-6 mr-2" />
                Manage Menu
              </Button>
              <Button 
                className="h-24 text-lg"
                variant="outline"
                onClick={() => navigate('/restaurant/promotions')}
              >
                <Tag className="w-6 h-6 mr-2" />
                Create Promotion
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Recent Orders */}
        <Card className="mt-6">
          <CardHeader>
            <CardTitle>Recent Orders</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="flex justify-center py-8"><Loader2 className="w-6 h-6 animate-spin text-gray-400" /></div>
            ) : recentOrders.length === 0 ? (
              <p className="text-center text-gray-500 py-8">No orders yet</p>
            ) : (
              <div className="space-y-4">
                {recentOrders.slice(0, 5).map(order => (
                  <div key={order.id} className="flex items-center justify-between p-4 border rounded-lg">
                    <div>
                      <p className="font-semibold">Order #{order.orderNumber ?? order.id}</p>
                      <p className="text-sm text-gray-500">{order.customerId}</p>
                    </div>
                    <div className="text-right">
                      <p className="font-semibold">฿{(order.totalAmount ?? 0).toFixed(2)}</p>
                      <p className="text-sm text-gray-500 capitalize">{order.status?.toLowerCase()}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}