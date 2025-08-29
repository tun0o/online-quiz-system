import { X, AlertTriangle } from 'lucide-react';

export default function ConfirmationModal({
  isOpen,
  onClose,
  onConfirm,
  title,
  message,
  confirmText = 'Xác nhận',
  cancelText = 'Hủy',
  isLoading = false,
  variant = 'default', // 'default' or 'danger'
  size = 'md' // 'sm', 'md', or 'lg'
}) {
  if (!isOpen) return null;

  const sizeClasses = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg'
  };

  const colorSchemes = {
    default: {
      icon: 'text-blue-500',
      button: 'bg-blue-600 hover:bg-blue-700',
    },
    danger: {
      icon: 'text-red-500',
      button: 'bg-red-600 hover:bg-red-700',
    }
  };

  const scheme = colorSchemes[variant] || colorSchemes.default;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 transition-opacity" onClick={onClose}>
      <div className={`bg-white rounded-lg p-6 w-full shadow-xl transform transition-all ${sizeClasses[size] || sizeClasses.md}`} onClick={(e) => e.stopPropagation()}>
        <div className="flex items-start">
          <div className={`mr-4 flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full ${scheme.icon.replace('text-', 'bg-').replace('500', '100')}`}>
            <AlertTriangle className={scheme.icon} size={24} />
          </div>
          <div className="flex-1">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">{title}</h3>
            <p className="text-sm text-gray-600">{message}</p>
          </div>
        </div>
        <div className="flex justify-end gap-3 mt-6">
          <button onClick={onClose} disabled={isLoading} className="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition disabled:opacity-50">
            {cancelText}
          </button>
          <button onClick={onConfirm} disabled={isLoading} className={`px-4 py-2 text-white rounded-lg transition disabled:opacity-50 ${scheme.button}`}>
            {isLoading ? 'Đang xử lý...' : confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}