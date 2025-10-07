import React, { useMemo } from "react";

function stringToColor(str) {
  // simple hash -> color
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  const h = Math.abs(hash) % 360; // hue
  return `hsl(${h}deg 60% 40%)`;
}

export default function AvatarFallback({ user, size = 40, showDetails = true, className = "", maxEmailLength = 25 }) {
  const email = user?.email ?? "";
  const fullName = user?.fullName ?? "";
  const displayName = fullName || email;
  const initial = displayName ? displayName.charAt(0).toUpperCase() : "?";
  const bg = useMemo(() => stringToColor(displayName || "anon"), [displayName]);

  // Hàm rút gọn email thông minh
  const truncateEmail = (email, maxLength) => {
    if (email.length <= maxLength) return email;
    
    const [localPart, domain] = email.split('@');
    if (!domain) return email;
    
    // Nếu domain quá dài, rút gọn local part
    if (domain.length > maxLength - 10) {
      const truncatedLocal = localPart.substring(0, Math.max(1, maxLength - domain.length - 4));
      return `${truncatedLocal}...@${domain}`;
    }
    
    // Nếu local part quá dài, rút gọn local part
    if (localPart.length > maxLength - domain.length - 4) {
      const truncatedLocal = localPart.substring(0, Math.max(1, maxLength - domain.length - 4));
      return `${truncatedLocal}...@${domain}`;
    }
    
    return email;
  };

  const displayText = fullName ? fullName : truncateEmail(email, maxEmailLength);

  if (!showDetails) {
    // Chỉ hiển thị avatar tròn
    return (
      <div
        style={{ width: size, height: size, backgroundColor: bg }}
        className={`rounded-full flex items-center justify-center text-white font-bold ${className}`}
        aria-label={`Avatar của ${displayName}`}
        title={displayName} // Tooltip hiển thị tên hoặc email đầy đủ
      >
        {initial}
      </div>
    );
  }

  // Hiển thị đầy đủ với thông tin
  return (
    <div className="bg-green-50 p-4 rounded-lg border border-green-200">
      <div className="flex items-center gap-3">
        <div
          style={{ width: size, height: size, backgroundColor: bg }}
          className="rounded-full flex items-center justify-center text-white font-bold"
          aria-label={`Avatar của ${displayName}`}
          title={displayName} // Tooltip hiển thị tên hoặc email đầy đủ
        >
          {initial}
        </div>
        <div className="min-w-0 flex-1">
          <p 
            className="font-medium text-gray-800 truncate" 
            title={displayName} // Tooltip hiển thị tên hoặc email đầy đủ
          >
            {displayText}
          </p>
          <p className="text-sm text-gray-600">
            {user?.roles?.includes("ADMIN") ? "Quản trị viên" : "Người dùng"}
          </p>
        </div>
      </div>
    </div>
  );
}
