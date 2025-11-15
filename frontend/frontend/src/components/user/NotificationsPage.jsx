import { useState, useEffect, useCallback, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { Bell, CheckCheck, Mail, FileText, DollarSign, Star, X } from 'lucide-react';
import { userService } from '@/services/userService';
import { useAuthStore } from '@/hooks/useAuth';
import { formatDistanceToNow, isToday, isYesterday } from 'date-fns';
import { vi } from 'date-fns/locale';

const NOTIFICATION_PAGE_SIZE = 20;
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
            { }
            return <Mail className="text-gray-500" size={20} />;
    }
};

const formatDate = (dateString) => {
    return formatDistanceToNow(new Date(dateString), { addSuffix: true, locale: vi });
};

const groupNotificationsByDate = (notifications) => {
    const groups = {
        'Hôm nay': [],
        'Hôm qua': [],
        'Cũ hơn': [],
    };

    notifications.forEach(notification => {
        const notificationDate = new Date(notification.createdAt);
        if (isToday(notificationDate)) {
            groups['Hôm nay'].push(notification);
        } else if (isYesterday(notificationDate)) {
            groups['Hôm qua'].push(notification);
        } else {
            groups['Cũ hơn'].push(notification);
        }
    });

    // Trả về các nhóm có chứa thông báo
    return Object.fromEntries(Object.entries(groups).filter(([, value]) => value.length > 0));
};

export default function NotificationsPage() {
    const [notifications, setNotifications] = useState([]);
    const [page, setPage] = useState(0);
    const [loading, setLoading] = useState(false);
    const [hasMore, setHasMore] = useState(true);
    const navigate = useNavigate();
    const { fetchUnreadCount, setUnreadCount } = useAuthStore();

    const groupedNotifications = useMemo(() => groupNotificationsByDate(notifications), [notifications]);

    const observer = useRef();
    const lastNotificationElementRef = useCallback(node => {
        if (loading) return;
        if (observer.current) observer.current.disconnect();
        observer.current = new IntersectionObserver(entries => {
            if (entries[0].isIntersecting && hasMore) {
                setPage(prevPage => prevPage + 1);
            }
        });
        if (node) observer.current.observe(node);
    }, [loading, hasMore]);

    const loadNotifications = useCallback(async (currentPage) => {
        if (!hasMore && currentPage > 0) return;
        setLoading(true);
        try {
            const data = await userService.getNotifications({ page: currentPage, size: NOTIFICATION_PAGE_SIZE });
            setNotifications(prev => currentPage === 0 ? data.content : [...prev, ...data.content]);
            setHasMore(!data.last);
        } catch (error) {
            toast.error('Không thể tải thông báo.');
            console.error('Failed to fetch notifications:', error);
        } finally {
            setLoading(false);
        }
    }, [hasMore]);

    useEffect(() => {
        // Chỉ gọi khi page thay đổi
        loadNotifications(page);
    }, [page, loadNotifications]);

    const reloadNotifications = useCallback(() => {
        setNotifications([]);
        setHasMore(true);
        setPage(0);
    }, [loadNotifications]);

    const handleMarkAsRead = async (notificationId) => {
        try {
            await userService.markNotificationAsRead(notificationId);

            setNotifications(prev =>
                prev.map(n =>
                    n.id === notificationId ? { ...n, read: true } : n
                )
            );

            reloadNotifications();
            fetchUnreadCount(); // Cập nhật lại số lượng ở chuông
        } catch (error) {
            console.error("Failed to mark notification as read:", error);
            toast.error('Không thể cập nhật thông báo.');
        }
    };

    const handleMarkAllAsRead = async () => {
        try {
            await userService.markAllNotificationsAsRead();
            toast.success('Đã đánh dấu tất cả là đã đọc.');

            setNotifications(prev => prev.map(n => ({ ...n, read: true })));

            // Tải lại toàn bộ danh sách từ đầu
            reloadNotifications();
            fetchUnreadCount(); // Cập nhật lại số lượng ở chuông
        } catch (error) {
            console.error("Failed to mark all as read:", error);
            toast.error('Không thể cập nhật tất cả thông báo.');
        }
    };

    const handleNotificationClick = (notification) => {
        // Chỉ đánh dấu đã đọc nếu nó chưa được đọc
        if (!notification.read) {
            handleMarkAsRead(notification.id);
        }
        // Luôn điều hướng nếu có link
        if (notification.link) {
            navigate(notification.link);
        }
    };

    const hasUnread = notifications.some(n => !n.read);

    // Hiển thị loading ban đầu
    if (page === 0 && loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600" />
            </div>
        );
    }

    return (
        <div className="max-w-4xl mx-auto bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden">
            <div className="flex justify-between items-center p-4 sm:p-6 border-b border-gray-200">
                <div className="flex items-center gap-3">
                    <Bell size={28} className="text-green-600" />
                    <h1 className="text-2xl font-bold text-gray-800">Thông báo</h1>
                </div>
                {notifications.length > 0 && hasUnread && (
                    <button
                        onClick={handleMarkAllAsRead}
                        className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-blue-600 bg-blue-50 rounded-lg hover:bg-blue-100 transition"
                    >
                        <CheckCheck size={16} />
                        Đánh dấu tất cả đã đọc
                    </button>
                )}
            </div>

            <div className="notification-list">
                {notifications.length === 0 && !loading ? (
                    <div className="text-center py-16 text-gray-500">
                        <Mail size={48} className="mx-auto mb-4" />
                        <p className="text-lg">Bạn không có thông báo nào.</p>
                    </div>
                ) : (
                    Object.entries(groupedNotifications).map(([groupTitle, groupNotifications], groupIndex) => (
                        <div key={groupTitle}>
                            <h2 className="text-sm font-bold text-gray-500 uppercase tracking-wider bg-gray-50 px-4 py-2 border-b border-t border-gray-200 sticky top-0 z-10">
                                {groupTitle}
                            </h2>
                            {groupNotifications.map((notification, notificationIndex) => {
                                // Xác định phần tử cuối cùng của toàn bộ danh sách
                                const overallIndex = notifications.findIndex(n => n.id === notification.id);
                                const isLastElement = overallIndex === notifications.length - 1;

                                return (
                                    <div
                                        ref={isLastElement ? lastNotificationElementRef : null}
                                        key={notification.id}
                                        onClick={() => handleNotificationClick(notification)}
                                        className={`flex items-start gap-4 p-4 border-b border-gray-200 transition-colors cursor-pointer
                                            ${!notification.read
                                                ? 'bg-green-50 hover:bg-green-100'
                                                : 'bg-white hover:bg-gray-50'
                                            }`
                                        }
                                    >
                                        <div className="flex-shrink-0 mt-1">{getNotificationIcon(notification.notificationType)}</div>
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
                                );
                            })}
                        </div>
                    ))
                )}
            </div>

            {loading && (
                <div className="text-center p-4 text-gray-500">
                    Đang tải thêm...
                </div>
            )}

            {!hasMore && notifications.length > 0 && (
                <div className="text-center p-4 text-gray-500 text-sm">
                    Đã tải hết tất cả thông báo.
                </div>
            )}
        </div>
    );
}
