import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { useApp } from '../../contexts/AppContext';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { adminStats } from '../../data/mockData';
import { ArrowLeft, DollarSign, Users, Store, Bike, ShoppingCart, TrendingUp } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { getAdminStats, type AdminStats } from '../../services/admin.service';

const revenueData = [
  { day: 'Mon', revenue: 45000 },
  { day: 'Tue', revenue: 52000 },
  { day: 'Wed', revenue: 48000 },
  { day: 'Thu', revenue: 61000 },
  { day: 'Fri', revenue: 72000 },
  { day: 'Sat', revenue: 95000 },
  { day: 'Sun', revenue: 88000 }
];

export default function AdminDashboard() {
  const navigate = useNavigate();
  const { logout } = useApp();
  const [liveStats, setLiveStats] = useState<AdminStats | null>(null);

  useEffect(() => {
    getAdminStats()
      .then(setLiveStats)
      .catch(() => { /* backend unavailable — fall back to mock values */ });
  }, []);

  const handleExit = () => {
    logout();
    navigate('/login');
  };

  const todayRevenue = liveStats?.orders.todayRevenue ?? adminStats.dailyRevenue;
  const monthRevenue = liveStats?.orders.monthRevenue ?? adminStats.monthlyRevenue;
  const todayOrderCount = liveStats?.orders.todayOrders ?? adminStats.todayOrders;
  const activeRestaurantCount = liveStats?.restaurants.activeRestaurants ?? adminStats.activeRestaurants;

  const stats = [
    {
      title: 'Daily Revenue',
      value: `฿${todayRevenue.toLocaleString(undefined, { maximumFractionDigits: 0 })}`,
      icon: DollarSign,
      color: 'text-green-600',
      bgColor: 'bg-green-50',
      change: '+12.5%'
    },
    {
      title: 'Monthly Revenue',
      value: `฿${(monthRevenue / 1000).toFixed(1)}K`,
      icon: TrendingUp,
      color: 'text-blue-600',
      bgColor: 'bg-blue-50',
      change: '+8.2%'
    },
    {
      title: 'Active Customers',
      value: adminStats.activeCustomers.toLocaleString(),
      icon: Users,
      color: 'text-purple-600',
      bgColor: 'bg-purple-50'
    },
    {
      title: 'Active Restaurants',
      value: activeRestaurantCount,
      icon: Store,
      color: 'text-orange-600',
      bgColor: 'bg-orange-50'
    },
    {
      title: 'Active Riders',
      value: adminStats.activeRiders,
      icon: Bike,
      color: 'text-indigo-600',
      bgColor: 'bg-indigo-50'
    },
    {
      title: "Today's Orders",
      value: todayOrderCount,
      icon: ShoppingCart,
      color: 'text-pink-600',
      bgColor: 'bg-pink-50'
    }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl">🛡️ Admin Dashboard</h1>
              <p className="text-sm text-gray-500">FoodExpress Platform Management</p>
            </div>
            <Button variant="ghost" onClick={handleExit}>
              <ArrowLeft className="w-4 h-4 mr-2" />
              Exit
            </Button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-8">
        {/* Key Metrics */}
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
          {stats.map((stat, idx) => {
            const Icon = stat.icon;
            return (
              <Card key={idx}>
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-gray-600">{stat.title}</p>
                      <p className="text-2xl mt-2">{stat.value}</p>
                      {stat.change && (
                        <p className="text-sm text-green-600 mt-1">{stat.change}</p>
                      )}
                    </div>
                    <div className={`${stat.bgColor} p-3 rounded-full`}>
                      <Icon className={`w-6 h-6 ${stat.color}`} />
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>

        {/* Revenue Chart */}
        <Card className="mb-8">
          <CardHeader>
            <CardTitle>Weekly Revenue Trend</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={revenueData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="day" />
                  <YAxis />
                  <Tooltip 
                    formatter={(value: number) => `฿${value.toLocaleString()}`}
                  />
                  <Area 
                    type="monotone" 
                    dataKey="revenue" 
                    stroke="#3b82f6" 
                    fill="#93c5fd" 
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>

        {/* Quick Actions */}
        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-2 gap-4">
              <Button 
                className="h-20 text-lg"
                onClick={() => navigate('/admin/accounts')}
              >
                <Users className="w-6 h-6 mr-2" />
                Manage Accounts
              </Button>
              <Button 
                className="h-20 text-lg"
                variant="outline"
                onClick={() => navigate('/admin/promotions')}
              >
                <TrendingUp className="w-6 h-6 mr-2" />
                System Promotions
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Platform Info */}
        <Card className="mt-6">
          <CardHeader>
            <CardTitle>Platform Statistics</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-2 gap-6">
              <div>
                <p className="text-sm text-gray-600 mb-1">Commission Rate</p>
                <p className="text-xl font-semibold">{adminStats.commissionRate * 100}%</p>
              </div>
              <div>
                <p className="text-sm text-gray-600 mb-1">Platform Status</p>
                <p className="text-xl font-semibold text-green-600">Operational</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}