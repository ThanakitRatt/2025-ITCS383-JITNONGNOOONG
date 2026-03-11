import { useState } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Input } from '../../components/ui/input';
import { Badge } from '../../components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../../components/ui/tabs';
import { Switch } from '../../components/ui/switch';
import { restaurants } from '../../data/mockData';
import { ArrowLeft, Search, Ban, Trash2, CheckCircle } from 'lucide-react';
import { toast } from 'sonner';

interface Customer {
  id: string;
  name: string;
  email: string;
  phone: string;
  totalOrders: number;
  active: boolean;
}

export default function AdminAccounts() {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  
  const defaultCustomers: Customer[] = [
    { id: 'CUST001', name: 'John Doe', email: 'john@example.com', phone: '+66 81 234 5678', totalOrders: 24, active: true },
    { id: 'CUST002', name: 'Sarah Wilson', email: 'sarah@example.com', phone: '+66 82 345 6789', totalOrders: 15, active: true },
    { id: 'CUST003', name: 'Michael Brown', email: 'michael@example.com', phone: '+66 83 456 7890', totalOrders: 8, active: false }
  ];
  const [customers, setCustomers] = useState<Customer[]>(() => {
    try { const s = localStorage.getItem('admin_customers'); return s ? JSON.parse(s) : defaultCustomers; } catch { return defaultCustomers; }
  });

  const defaultRestaurants = restaurants.map(r => ({ ...r, status: r.isActive ? 'active' : 'suspended' }));
  const [restaurantList, setRestaurantList] = useState(() => {
    try { const s = localStorage.getItem('admin_restaurants'); return s ? JSON.parse(s) : defaultRestaurants; } catch { return defaultRestaurants; }
  });

  const updateCustomers = (next: Customer[]) => {
    localStorage.setItem('admin_customers', JSON.stringify(next));
    setCustomers(next);
  };

  const handleToggleCustomer = (customerId: string) => {
    updateCustomers(customers.map(c => c.id === customerId ? { ...c, active: !c.active } : c));
    toast.success('Customer status updated');
  };

  const handleDeleteCustomer = (customerId: string) => {
    updateCustomers(customers.filter(c => c.id !== customerId));
    toast.success('Customer account deleted');
  };

  const updateRestaurants = (next: typeof restaurantList) => {
    localStorage.setItem('admin_restaurants', JSON.stringify(next));
    setRestaurantList(next);
  };

  const handleToggleRestaurant = (restaurantId: string) => {
    updateRestaurants(restaurantList.map(r =>
      r.id === restaurantId
        ? { ...r, status: r.status === 'active' ? 'suspended' : 'active', isActive: r.status !== 'active' }
        : r
    ));
    toast.success('Restaurant status updated');
  };

  const handleDeleteRestaurant = (restaurantId: string) => {
    updateRestaurants(restaurantList.filter(r => r.id !== restaurantId));
    toast.success('Restaurant account deleted');
  };

  const filteredCustomers = customers.filter(c => 
    c.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    c.email.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const filteredRestaurants = restaurantList.filter(r => 
    r.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl">Account Management</h1>
            <Button variant="ghost" onClick={() => navigate('/admin/dashboard')}>
              <ArrowLeft className="w-4 h-4 mr-2" />
              Dashboard
            </Button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-8">
        {/* Search */}
        <div className="mb-6">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <Input
              placeholder="Search accounts..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10"
            />
          </div>
        </div>

        <Tabs defaultValue="customers">
          <TabsList className="mb-6">
            <TabsTrigger value="customers">Customers</TabsTrigger>
            <TabsTrigger value="restaurants">Restaurants</TabsTrigger>
          </TabsList>

          <TabsContent value="customers">
            <Card>
              <CardHeader>
                <CardTitle>Customer Accounts</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {filteredCustomers.map(customer => (
                    <Card key={customer.id}>
                      <CardContent className="pt-6">
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-1">
                              <h3 className="font-semibold">{customer.name}</h3>
                              <Badge variant={customer.active ? 'default' : 'secondary'}>
                                {customer.active ? 'Active' : 'Suspended'}
                              </Badge>
                            </div>
                            <p className="text-sm text-gray-600">{customer.email}</p>
                            <p className="text-sm text-gray-600">{customer.phone}</p>
                            <p className="text-sm text-gray-500 mt-2">
                              Total Orders: {customer.totalOrders}
                            </p>
                          </div>
                          <div className="flex items-center gap-2">
                            <div className="flex items-center gap-2 mr-4">
                              <Switch
                                checked={customer.active}
                                onCheckedChange={() => handleToggleCustomer(customer.id)}
                              />
                              <span className="text-sm">
                                {customer.active ? 'Enabled' : 'Disabled'}
                              </span>
                            </div>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleDeleteCustomer(customer.id)}
                            >
                              <Trash2 className="w-4 h-4 text-red-500" />
                            </Button>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                  {filteredCustomers.length === 0 && (
                    <p className="text-center text-gray-500 py-8">No customers found</p>
                  )}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="restaurants">
            <Card>
              <CardHeader>
                <CardTitle>Restaurant Accounts</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {filteredRestaurants.map(restaurant => (
                    <Card key={restaurant.id}>
                      <CardContent className="pt-6">
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-1">
                              <h3 className="font-semibold">{restaurant.name}</h3>
                              <Badge variant={restaurant.isActive ? 'default' : 'secondary'}>
                                {restaurant.status}
                              </Badge>
                            </div>
                            <p className="text-sm text-gray-600">Cuisine: {restaurant.cuisine}</p>
                            <p className="text-sm text-gray-600">Rating: {restaurant.rating} ⭐</p>
                            <p className="text-sm text-gray-500 mt-2">
                              Distance: {restaurant.distance} km
                            </p>
                          </div>
                          <div className="flex items-center gap-2">
                            <div className="flex items-center gap-2 mr-4">
                              <Switch
                                checked={restaurant.isActive}
                                onCheckedChange={() => handleToggleRestaurant(restaurant.id)}
                              />
                              <span className="text-sm">
                                {restaurant.isActive ? 'Enabled' : 'Disabled'}
                              </span>
                            </div>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleDeleteRestaurant(restaurant.id)}
                            >
                              <Trash2 className="w-4 h-4 text-red-500" />
                            </Button>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                  {filteredRestaurants.length === 0 && (
                    <p className="text-center text-gray-500 py-8">No restaurants found</p>
                  )}
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}
