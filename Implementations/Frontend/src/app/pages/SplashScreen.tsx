import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { useApp } from '../contexts/AppContext';

const ROLE_HOME: Record<string, string> = {
  customer: '/customer/restaurants',
  restaurant: '/restaurant/dashboard',
  rider: '/rider/dashboard',
  admin: '/admin/dashboard',
};

export default function SplashScreen() {
  const navigate = useNavigate();
  const { user } = useApp();
  const [phase, setPhase] = useState<'logo' | 'tagline' | 'fade'>('logo');

  useEffect(() => {
    // Already logged in — skip splash and go straight to the role home
    if (user?.role) {
      navigate(ROLE_HOME[user.role] ?? '/login', { replace: true });
      return;
    }

    const t1 = setTimeout(() => setPhase('tagline'), 800);
    const t2 = setTimeout(() => setPhase('fade'), 2200);
    const t3 = setTimeout(() => navigate('/login'), 2900);
    return () => { clearTimeout(t1); clearTimeout(t2); clearTimeout(t3); };
  }, [navigate, user]);

  return (
    <div
      className="min-h-screen flex flex-col items-center justify-center relative overflow-hidden bg-gradient-to-br from-orange-50 to-blue-50"
      style={{
        opacity: phase === 'fade' ? 0 : 1,
        transition: phase === 'fade' ? 'opacity 0.7s ease-out' : 'none',
      }}
    >
      {/* Logo & Brand */}
      <div
        className="flex flex-col items-center z-10"
        style={{
          transform: phase === 'logo' ? 'scale(0.7) translateY(20px)' : 'scale(1) translateY(0)',
          transition: 'transform 0.8s cubic-bezier(0.34,1.56,0.64,1)',
        }}
      >
        {/* Icon */}
        <div
          className="flex items-center justify-center rounded-3xl mb-6 bg-gradient-to-br from-orange-500 to-orange-400 shadow-2xl"
          style={{
            width: 110,
            height: 110,
          }}
        >
          <span style={{ fontSize: 60 }}>🍔</span>
        </div>

        {/* App name */}
        <h1
          className="text-gray-900"
          style={{
            fontSize: 52,
            fontWeight: 800,
            letterSpacing: '-1px',
          }}
        >
          Food<span className="text-orange-500">Express</span>
        </h1>

        {/* Tagline */}
        <p
          className="text-center mt-3 text-gray-600 uppercase tracking-wider"
          style={{
            fontSize: 16,
            opacity: phase === 'tagline' || phase === 'fade' ? 1 : 0,
            transform: phase === 'tagline' || phase === 'fade' ? 'translateY(0)' : 'translateY(10px)',
            transition: 'opacity 0.6s ease 0.1s, transform 0.6s ease 0.1s',
          }}
        >
          Delivering Happiness to Your Door
        </p>

        {/* Pill badges */}
        <div
          className="flex gap-3 mt-8 flex-wrap justify-center"
          style={{
            opacity: phase === 'tagline' || phase === 'fade' ? 1 : 0,
            transition: 'opacity 0.6s ease 0.3s',
          }}
        >
          {['🛵 Fast Delivery', '🍽️ 1000+ Restaurants', '⭐ Top Rated'].map((b) => (
            <span
              key={b}
              className="px-4 py-1.5 rounded-full text-sm bg-white border border-gray-200 text-gray-700 shadow-sm"
            >
              {b}
            </span>
          ))}
        </div>
      </div>

      {/* Loading dots */}
      <div
        className="absolute bottom-16 flex gap-2"
        style={{
          opacity: phase === 'tagline' ? 1 : 0,
          transition: 'opacity 0.4s ease',
        }}
      >
        {[0, 1, 2].map((i) => (
          <div
            key={i}
            className="rounded-full"
            style={{
              width: 8,
              height: 8,
              background: '#ff6b35',
              animation: `bounce 1.2s ease-in-out ${i * 0.2}s infinite`,
            }}
          />
        ))}
      </div>

      <style>{`
        @keyframes bounce {
          0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
          40% { transform: scale(1); opacity: 1; }
        }
      `}</style>
    </div>
  );
}
