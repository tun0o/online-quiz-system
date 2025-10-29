import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { Bell, Check, CheckCheck, Mail, FileText, DollarSign, Star, X } from 'lucide-react';
import { userService } from '@/services/userService';
import { useAuthStore } from '@/hooks/useAuth';

const getNotificationIcon = (notificationType) => {
    switch (notificationType) {
        case 'GRADING_COMPLETED':
            return <FileText className="text-blue-500" size={20} />;
        case 'PAYMENT_SUCCESS':
        case 'PAYMENT_FAILURE':
            return <DollarSign className="text-green-500" size={20} />;
        case 'SUBMISSION_APPROVED':
            return <Star className="text-yellow-500" size={20} />;
        case 'SUBMISSION_REJECTED':
            return <X className="text-red-500" size={20} />;
        default:
            {}
            return <Mail className="text-gray-500" size={20} />;
    }
};

const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
};

export default function NotificationsPage() {
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();
    const { setUnreadCount } = useAuthStore();

    const fetchNotifications = async () => {
        try {
            const data = await userService.getNotifications();
            setNotifications(data || []);
        } catch (error) {
            toast.error('Không thể tải thông báo.');
            console.error('Failed to fetch notifications:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchNotifications();
    }, []);

    const handleMarkAsRead = async (notificationId) => {
        try {
            await userService.markNotificationAsRead(notificationId);
            setNotifications(prev =>
                prev.map(n => (n.id === notificationId ? { ...n, read: true } : n))
            );
            setUnreadCount(prev => prev - 1); // Giảm số lượng đi 1
            toast.success('Đã đánh dấu là đã đọc.');
        } catch (error) {
            toast.error('Không thể cập nhật thông báo.');
        }
    };

    const handleMarkAllAsRead = async () => {
        try {
            await userService.markAllNotificationsAsRead();
            setNotifications(prev => prev.map(n => ({ ...n, read: true })));
            setUnreadCount(0); // Đặt lại số lượng về 0
            toast.success('Đã đánh dấu tất cả là đã đọc.');
        } catch (error) {
            toast.error('Không thể cập nhật tất cả thông báo.');
        }
    };

    const handleNotificationClick = (notification) => {
        if (!notification.read) {
            handleMarkAsRead(notification.id);
        }
        if (notification.link) {
            navigate(notification.link);
        }
    };

    const unreadCount = notifications.filter(n => !n.read).length;

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600" />
            </div>
        );
    }

    return (
        <div className="max-w-4xl mx-auto bg-white p-6 rounded-lg shadow-md border border-gray-200">
            <div className="flex justify-between items-center mb-6 border-b pb-4">
                <div className="flex items-center gap-3">
                    <Bell size={28} className="text-green-600" />
                    <h1 className="text-2xl font-bold text-gray-800">Thông báo</h1>
                </div>
                {notifications.length > 0 && unreadCount > 0 && (
                    <button
                        onClick={handleMarkAllAsRead}
                        className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-blue-50 rounded-lg hover:bg-blue-100 transition"
                    >
                        <CheckCheck size={16} />
                        Đánh dấu tất cả đã đọc
                    </button>
                )}
            </div>

            {notifications.length === 0 ? (
                <div className="text-center py-16 text-gray-500">
                    <Mail size={48} className="mx-auto mb-4" />
                    <p className="text-lg">Bạn không có thông báo nào.</p>
                </div>
            ) : (
                <div className="space-y-3">
                    {notifications.map(notification => (
                        <div
                            key={notification.id}
                            onClick={() => handleNotificationClick(notification)}
                            className={`flex items-start gap-4 p-4 rounded-lg border transition-all duration-200 cursor-pointer
                                ${!notification.read
                                    ? 'bg-green-50 border-green-200 hover:bg-green-100'
                                    : 'bg-gray-50 border-gray-200 hover:bg-gray-100'
                                }`
                            }
                        >
                            <div className="flex-shrink-0 mt-1">
                                {getNotificationIcon(notification.notificationType)}
                            </div>
                            <div className="flex-grow">
                                <p className="text-gray-800">{notification.message}</p>
                                <p className="text-xs text-gray-500 mt-1">{formatDate(notification.createdAt)}</p>
                            </div>
                            <div className="flex-shrink-0 flex items-center gap-2">
                                {!notification.read && (
                                    <div className="w-2.5 h-2.5 bg-blue-500 rounded-full" title="Chưa đọc"></div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
