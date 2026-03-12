import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Textarea } from '../../components/ui/textarea';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '../../components/ui/dialog';
import { ArrowLeft, Plus, Pencil, Trash2, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import restaurantService from '../../services/restaurant.service';
import { useApp } from '../../contexts/AppContext';

export default function MenuManagement() {
  const navigate = useNavigate();
  const { user } = useApp();
  
  // Get restaurant ID from owner ID
  const ownerId = user?.id || '1';
  const [restaurantId, setRestaurantId] = useState<string | null>(null);
  
  const [items, setItems] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [loadingData, setLoadingData] = useState(true);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    categoryId: '',
    preparationTime: '15'
  });

  // Load restaurant by owner ID first
  useEffect(() => {
    const initRestaurant = async () => {
      try {
        const restaurants = await restaurantService.getOwnerRestaurants(ownerId);
        if (restaurants && restaurants.length > 0) {
          setRestaurantId(restaurants[0].id.toString());
        }
      } catch (error) {
        console.error('Failed to load restaurant:', error);
        toast.error('Failed to load restaurant');
      }
    };
    initRestaurant();
  }, [ownerId]);

  // Load menu items when restaurant ID is available
  useEffect(() => {
    if (restaurantId) {
      loadMenuData();
    }
  }, [restaurantId]);

  const loadMenuData = async () => {
    if (!restaurantId) return;
    try {
      setLoadingData(true);
      const [menuItems, menuCategories] = await Promise.all([
        restaurantService.getRestaurantMenu(restaurantId),
        restaurantService.getRestaurantCategories(restaurantId)
      ]);
      setItems(menuItems);
      setCategories(menuCategories);
    } catch (error) {
      console.error('Failed to load menu data:', error);
      toast.error('Failed to load menu items');
      // Fallback to empty arrays
      setItems([]);
      setCategories([]);
    } finally {
      setLoadingData(false);
    }
  };

  const handleAdd = () => {
    setEditingItem(null);
    setFormData({ 
      name: '', 
      description: '', 
      price: '', 
      categoryId: categories[0]?.id || '',
      preparationTime: '15'
    });
    setIsDialogOpen(true);
  };

  const handleEdit = (item: any) => {
    setEditingItem(item);
    setFormData({
      name: item.name,
      description: item.description,
      price: item.price.toString(),
      categoryId: item.categoryId?.toString() || '',
      preparationTime: item.preparationTime?.toString() || '15'
    });
    setIsDialogOpen(true);
  };

  const handleDelete = async (itemId: string) => {
    if (!restaurantId) {
      toast.error('Restaurant not loaded');
      return;
    }
    if (!confirm('Are you sure you want to delete this menu item?')) {
      return;
    }

    try {
      await restaurantService.deleteMenuItem(restaurantId, itemId);
      setItems(items.filter(item => item.id !== itemId));
      toast.success('Menu item deleted successfully');
    } catch (error) {
      console.error('Delete error:', error);
      toast.error('Failed to delete menu item');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!restaurantId) {
      toast.error('Restaurant not loaded');
      return;
    }
    setLoading(true);
    
    try {
      const itemData = {
        name: formData.name,
        description: formData.description,
        price: Number.parseFloat(formData.price),
        categoryId: Number.parseInt(formData.categoryId),
        preparationTime: Number.parseInt(formData.preparationTime),
        isAvailable: true
      };

      if (editingItem) {
        // Update existing item
        const updated = await restaurantService.updateMenuItem(
          restaurantId,
          editingItem.id,
          itemData
        );
        setItems(items.map(item => item.id === editingItem.id ? updated : item));
        toast.success('Menu item updated successfully');
      } else {
        // Add new item
        const newItem = await restaurantService.addMenuItem(restaurantId, itemData);
        setItems([...items, newItem]);
        toast.success('Menu item added successfully');
      }
      
      setIsDialogOpen(false);
    } catch (error: any) {
      console.error('Submit error:', error);
      const errorMsg = error.response?.data?.message || error.message || 'Operation failed';
      toast.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl">Menu Management</h1>
            <div className="flex gap-2">
              <Button onClick={handleAdd} disabled={loadingData || categories.length === 0}>
                <Plus className="w-4 h-4 mr-2" />
                Add Item
              </Button>
              <Button variant="ghost" onClick={() => navigate('/restaurant/dashboard')}>
                <ArrowLeft className="w-4 h-4 mr-2" />
                Dashboard
              </Button>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-8">
        {loadingData ? (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
          </div>
        ) : categories.length === 0 ? (
          <Card>
            <CardContent className="text-center py-12">
              <p className="text-gray-500 mb-4">No categories yet. Create categories first.</p>
              <Button onClick={() => navigate('/restaurant/categories')}>
                Manage Categories
              </Button>
            </CardContent>
          </Card>
        ) : (
          <>
            {categories.map(category => {
              const categoryItems = items.filter(item => item.categoryId === category.id);
              
              return (
                <div key={category.id} className="mb-8">
                  <h2 className="text-xl mb-4">{category.name}</h2>
                  {categoryItems.length === 0 ? (
                    <Card>
                      <CardContent className="text-center py-6">
                        <p className="text-gray-400 text-sm">No items in this category</p>
                      </CardContent>
                    </Card>
                  ) : (
                    <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">{categoryItems.map(item => (
                  <Card key={item.id}>
                    <CardHeader>
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <CardTitle className="text-lg">{item.name}</CardTitle>
                          <p className="text-sm text-gray-500 mt-1 line-clamp-2">
                            {item.description}
                          </p>
                        </div>
                      </div>
                    </CardHeader>
                    <CardContent>
                      <div className="flex items-center justify-between">
                        <p className="text-xl font-semibold text-blue-600">
                          ฿{item.price}
                        </p>
                        <div className="flex gap-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleEdit(item)}
                          >
                            <Pencil className="w-4 h-4" />
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleDelete(item.id)}
                          >
                            <Trash2 className="w-4 h-4 text-red-500" />
                          </Button>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
                    </div>
                  )}
                </div>
              );
            })}

        {items.length === 0 && !loadingData && (
          <Card>
            <CardContent className="text-center py-12">
              <p className="text-gray-500 mb-4">No menu items yet</p>
              <Button onClick={handleAdd} disabled={categories.length === 0}>
                <Plus className="w-4 h-4 mr-2" />
                Add Your First Item
              </Button>
            </CardContent>
          </Card>
        )}
          </>
        )}
      </div>

      {/* Add/Edit Dialog */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {editingItem ? 'Edit Menu Item' : 'Add New Menu Item'}
            </DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <Label htmlFor="name">Item Name *</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                required
              />
            </div>
            <div>
              <Label htmlFor="description">Description *</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                required
              />
            </div>
            <div>
              <Label htmlFor="categoryId">Category *</Label>
              <select
                id="categoryId"
                value={formData.categoryId}
                onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                className="w-full px-3 py-2 rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              >
                <option value="">Select a category</option>
                {categories.map(cat => (
                  <option key={cat.id} value={cat.id}>
                    {cat.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="price">Price (฿) *</Label>
                <Input
                  id="price"
                  type="number"
                  step="0.01"
                  min="0"
                  value={formData.price}
                  onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                  required
                />
              </div>
              <div>
                <Label htmlFor="preparationTime">Prep Time (min) *</Label>
                <Input
                  id="preparationTime"
                  type="number"
                  min="1"
                  value={formData.preparationTime}
                  onChange={(e) => setFormData({ ...formData, preparationTime: e.target.value })}
                  required
                />
              </div>
            </div>
            <div className="flex gap-2 justify-end">
              <Button type="button" variant="outline" onClick={() => setIsDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={loading}>
                {loading ? (
                  <>
                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    Saving...
                  </>
                ) : (
                  editingItem ? 'Update Item' : 'Add Item'
                )}
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
