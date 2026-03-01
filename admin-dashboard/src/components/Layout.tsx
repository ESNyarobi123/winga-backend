import { Outlet, NavLink, useNavigate } from "react-router-dom";
import { Button } from "@heroui/react";
import {
  LayoutDashboard,
  Briefcase,
  FileCheck,
  Users,
  FolderOpen,
  AlertCircle,
  Settings,
  ShieldCheck,
  LogOut,
  MessageSquareWarning,
} from "lucide-react";
import { useAuth } from "../hooks/useAuth";

const nav = [
  { to: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { to: "/jobs", label: "Jobs", icon: Briefcase },
  { to: "/moderation", label: "Posts (Jobs)", icon: ShieldCheck },
  { to: "/bids-moderation", label: "Bids", icon: MessageSquareWarning },
  { to: "/applications", label: "Applications", icon: FileCheck },
  { to: "/users", label: "Users", icon: Users },
  { to: "/categories", label: "Categories", icon: FolderOpen },
  { to: "/disputes", label: "Disputes", icon: AlertCircle },
  { to: "/settings", label: "Settings", icon: Settings },
];

export default function Layout() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className="flex h-screen w-full bg-slate-100 dark:bg-slate-900">
      <aside className="w-64 shrink-0 border-r border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 flex flex-col shadow-sm">
        <div className="p-5 border-b border-slate-200 dark:border-slate-700">
          <h2 className="font-bold text-xl text-slate-800 dark:text-slate-100">Winga Admin</h2>
          <p className="text-xs text-slate-500 mt-0.5">Content &amp; Bids moderation</p>
        </div>
        <nav className="flex-1 p-2 space-y-0.5">
          {nav.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? "bg-primary text-white"
                    : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700"
                }`
              }
            >
              <Icon size={20} />
              {label}
            </NavLink>
          ))}
        </nav>
        <div className="p-3 border-t border-slate-200 dark:border-slate-700 flex items-center gap-2">
          <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center text-primary font-semibold text-sm">
            A
          </div>
          <span className="flex-1 text-sm truncate text-slate-700 dark:text-slate-300">Admin</span>
          <Button size="sm" variant="light" onPress={handleLogout} isIconOnly aria-label="Logout">
            <LogOut size={18} />
          </Button>
        </div>
      </aside>
      <main className="flex-1 overflow-auto p-6">
        <Outlet />
      </main>
    </div>
  );
}
