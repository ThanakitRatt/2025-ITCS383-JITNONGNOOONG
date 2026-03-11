import { useNavigate } from 'react-router';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { useApp } from '../../contexts/AppContext';
import { ArrowLeft, MapPin, DollarSign, Package, Clock } from 'lucide-react';
import { toast } from 'sonner';

export default function RiderDashboard() {
  const navigate = useNavigate();
  const { orders, updateOrder, user, logout } = useApp();

  const riderId = user?.id ?? 'RIDER001';
  const myDeliveries = orders.filter(o => o.riderId === riderId);
  const activeDelivery = myDeliveries.find(o => o.status === 'delivering');
  
  const availableOrders = orders.filter(o => 
    o.status === 'ready' && !o.riderId
  );

  const todayEarnings = myDeliveries
    .filter(o => 
      o.status === 'completed' && 
      o.createdAt.toDateString() === new Date().toDateString()
    )
    .length * 50; // Mock ฿50 per delivery

  const completedToday = myDeliveries.filter(o => 
    o.status === 'completed' && 
    o.createdAt.toDateString() === new Date().toDateString()
  ).length;

  const handleExit = () => {
    logout();
    navigate('/login');
  };

  const handleAcceptOrder = (orderId: string) => {
    updateOrder(orderId, {
      riderId: riderId,
      riderName: 'Mike Chen',
      status: 'delivering'
    });
    toast.success('Order accepted!');
    navigate(`/rider/delivery/${orderId}`);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl">🚴 Rider Portal</h1>
              <p className="text-sm text-gray-500">Mike Chen</p>
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
        <div className="grid md:grid-cols-3 gap-6 mb-8">
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Today's Earnings</p>
                  <p className="text-3xl mt-2">฿{todayEarnings}</p>
                </div>
                <div className="bg-green-50 p-3 rounded-full">
                  <DollarSign className="w-6 h-6 text-green-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Completed Today</p>
                  <p className="text-3xl mt-2">{completedToday}</p>
                </div>
                <div className="bg-blue-50 p-3 rounded-full">
                  <Package className="w-6 h-6 text-blue-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Active Delivery</p>
                  <p className="text-3xl mt-2">{activeDelivery ? '1' : '0'}</p>
                </div>
                <div className="bg-orange-50 p-3 rounded-full">
                  <Clock className="w-6 h-6 text-orange-600" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Active Delivery */}
        {activeDelivery && (
          <Card className="mb-8 border-2 border-orange-500">
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Active Delivery</CardTitle>
                <Badge className="bg-orange-500">In Progress</Badge>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div>
                  <p className="text-sm text-gray-500">Order #{activeDelivery.id}</p>
                  <p className="font-semibold">{activeDelivery.restaurantName}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500 mb-1">Customer</p>
                  <p className="font-semibold">{activeDelivery.customerName}</p>
                </div>
                <div className="flex items-start gap-2">
                  <MapPin className="w-4 h-4 mt-1 text-gray-500 flex-shrink-0" />
                  <p className="text-sm">{activeDelivery.deliveryAddress}</p>
                </div>
                <Button 
                  className="w-full"
                  onClick={() => navigate(`/rider/delivery/${activeDelivery.id}`)}
                >
                  View Delivery Details
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Available Orders */}
        <Card>
          <CardHeader>
            <CardTitle>Available Orders Nearby</CardTitle>
          </CardHeader>
          <CardContent>
            {availableOrders.length === 0 ? (
              <div className="text-center py-8">
                <Package className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500">No available orders at the moment</p>
                <p className="text-sm text-gray-400 mt-2">New orders will appear here</p>
              </div>
            ) : (
              <div className="space-y-4">
                {availableOrders.map(order => (
                  <Card key={order.id} className="border-2">
                    <CardContent className="pt-6">
                      <div className="space-y-3">
                        <div className="flex items-start justify-between">
                          <div>
                            <p className="font-semibold">{order.restaurantName}</p>
                            <p className="text-sm text-gray-500">Order #{order.id}</p>
                          </div>
                          <Badge className="bg-green-500">
                            ฿50 delivery fee
                          </Badge>
                        </div>
                        <div className="flex items-start gap-2">
                          <MapPin className="w-4 h-4 mt-1 text-gray-500 flex-shrink-0" />
                          <div className="text-sm">
                            <p className="font-semibold">{order.customerName}</p>
                            <p className="text-gray-600">{order.deliveryAddress}</p>
                          </div>
                        </div>
                        <Button 
                          className="w-full"
                          onClick={() => handleAcceptOrder(order.id)}
                        >
                          Accept Order
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}