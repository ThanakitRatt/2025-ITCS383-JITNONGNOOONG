import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../../components/ui/button';
import { Card, CardContent } from '../../components/ui/card';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { ArrowLeft, Plus, Trash2, Loader2, Tag } from 'lucide-react';
import { toast } from 'sonner';
import restaurantService, { MenuCategory } from '../../services/restaurant.service';
import { useApp } from '../../contexts/AppContext';

export default function RestaurantCategories() {
  const navigate = useNavigate();
  const { user } = useApp();
  const ownerId = user?.id || '1';
  const [restaurantId, setRestaurantId] = useState<string | null>(null);

  const [categories, setCategories] = useState<MenuCategory[]>([]);
  const [loadingData, setLoadingData] = useState(true);
  const [newName, setNewName] = useState('');
  const [newDescription, setNewDescription] = useState('');
  const [adding, setAdding] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

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

  useEffect(() => {
    if (restaurantId) {
      loadCategories();
    }
  }, [restaurantId]);

  const loadCategories = async () => {
    if (!restaurantId) return;
    try {
      setLoadingData(true);
      const data = await restaurantService.getRestaurantCategories(restaurantId);
      setCategories(data);
    } catch {
      toast.error('Failed to load categories');
    } finally {
      setLoadingData(false);
    }
  };

  const handleAdd = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    if (!restaurantId || !newName.trim()) return;
    setAdding(true);
    try {
      const created = await restaurantService.addMenuCategory(restaurantId, {
        name: newName.trim(),
        description: newDescription.trim(),
      });
      setCategories([...categories, created]);
      setNewName('');
      setNewDescription('');
      toast.success('Category added');
    } catch {
      toast.error('Failed to add category');
    } finally {
      setAdding(false);
    }
  };

  const handleDelete = async (categoryId: string) => {
    if (!restaurantId) {
      toast.error('Restaurant not loaded');
      return;
    }
    if (!confirm('Delete this category? Menu items in this category will be unassigned.')) return;
    setDeletingId(categoryId);
    try {
      await restaurantService.deleteMenuCategory(restaurantId, categoryId);
      setCategories(categories.filter(c => String(c.id) !== categoryId));
      toast.success('Category deleted');
    } catch {
      toast.error('Failed to delete category');
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm">
        <div className="max-w-3xl mx-auto px-4 py-4 flex items-center justify-between">
          <h1 className="text-2xl">Manage Categories</h1>
          <Button variant="ghost" onClick={() => navigate('/restaurant/menu')}>
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to Menu
          </Button>
        </div>
      </div>

      <div className="max-w-3xl mx-auto px-4 py-8 space-y-6">
        {/* Add Category Form */}
        <Card>
          <CardContent className="pt-6">
            <form onSubmit={handleAdd} className="space-y-4">
              <h2 className="text-lg font-medium">Add New Category</h2>
              <div className="space-y-2">
                <Label htmlFor="cat-name">Name *</Label>
                <Input
                  id="cat-name"
                  placeholder="e.g. Appetizers"
                  value={newName}
                  onChange={e => setNewName(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="cat-desc">Description</Label>
                <Input
                  id="cat-desc"
                  placeholder="Optional description"
                  value={newDescription}
                  onChange={e => setNewDescription(e.target.value)}
                />
              </div>
              <Button type="submit" disabled={adding || !newName.trim()}>
                {adding ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : <Plus className="w-4 h-4 mr-2" />}
                Add Category
              </Button>
            </form>
          </CardContent>
        </Card>

        {/* Category List */}
        <div>
          <h2 className="text-lg font-medium mb-3">Existing Categories</h2>
          {loadingData && (
            <div className="flex justify-center py-12">
              <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
            </div>
          )}
          {!loadingData && categories.length === 0 && (
            <Card>
              <CardContent className="text-center py-12">
                <Tag className="w-10 h-10 mx-auto text-gray-300 mb-3" />
                <p className="text-gray-500">No categories yet. Add one above.</p>
              </CardContent>
            </Card>
          )}
          {!loadingData && categories.length > 0 && (
            <div className="space-y-2">
              {categories.map(cat => (
                <Card key={cat.id}>
                  <CardContent className="py-4 flex items-center justify-between">
                    <div>
                      <p className="font-medium">{cat.name}</p>
                      {cat.description && (
                        <p className="text-sm text-gray-500">{cat.description}</p>
                      )}
                    </div>
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={deletingId === String(cat.id)}
                      onClick={() => handleDelete(String(cat.id))}
                    >
                      {deletingId === String(cat.id)
                        ? <Loader2 className="w-4 h-4 animate-spin" />
                        : <Trash2 className="w-4 h-4 text-red-500" />}
                    </Button>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
