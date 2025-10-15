import { useState, useEffect, useCallback } from 'react';
import { toast } from 'react-toastify';
import { Edit, UserCheck, UserX, Shield, User, UserPlus, CheckCircle, XCircle } from 'lucide-react';
import { userService } from '@/services/userService';
import ConfirmationModal from '@/components/common/ConfirmationModal';

const RoleBadge = ({ role }) => {
    const isAdmin = role === 'ADMIN';
    return (
        <span className={`inline-flex items-center gap-1.5 px-2 py-1 rounded-full text-xs font-medium ${isAdmin ? 'bg-purple-100 text-purple-800' : 'bg-gray-100 text-gray-800'
            }`}>
            {isAdmin ? <Shield size={14} /> : <User size={14} />}
            {isAdmin ? 'Admin' : 'User'}
        </span>
    );
};

const StatusBadge = ({ status, text }) => {
    const isPositive = status === true;
    return (
        <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${isPositive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
            }`}>
            {isPositive ? <CheckCircle size={12} /> : <XCircle size={12} />}
            {text}
        </span>
    );
};

const UserEditModal = ({ user, isOpen, onClose, onSave }) => {
    const [formData, setFormData] = useState(user || {});
    const [isSaving, setIsSaving] = useState(false);

    useEffect(() => {
        setFormData(user);
    }, [user]);

    if (!isOpen || !user) return null;

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSaving(true);
        try {
            await onSave(user.id, formData);
            toast.success("Cập nhật người dùng thành công!");
            onClose();
        } catch (error) {
            toast.error(error.response?.data?.message || "Cập nhật thất bại.");
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
            <div className="bg-white rounded-lg shadow-xl w-full max-w-lg p-6 m-4 max-h-[90vh] overflow-y-auto">
                <h2 className="text-xl font-bold mb-4 text-gray-800">Chỉnh sửa người dùng: {user.name || user.email}</h2>
                <form onSubmit={handleSubmit}>
                    <div className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Email</label>
                            <span className="appearance-none block w-full px-3 py-3 border border-gray-300 bg-gray-200 text-gray-900 rounded-lg focus:border-green-500 sm:text-sm"
                            >{user.email}</span>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Tên</label>
                            <input type="text" name="name" value={formData?.name || ''} onChange={handleChange} className="appearance-none block w-full px-3 py-3 border border-gray-300 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Lớp</label>
                            <select
                                name="grade"
                                value={formData?.grade || ''}
                                onChange={handleChange}
                                className="appearance-none block w-full px-3 py-3 border border-gray-300 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                            >
                                <option value="" disabled>Chọn lớp</option>
                                <option value="10">Lớp 10</option>
                                <option value="11">Lớp 11</option>
                                <option value="12">Lớp 12</option>
                            </select>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Mục tiêu</label>
                            <select
                                name="goal"
                                value={formData?.goal || ''}
                                onChange={handleChange}
                                className="appearance-none block w-full px-3 py-3 border border-gray-300 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                            >
                                <option value="" disabled>Chọn mục tiêu</option>
                                <option value="EXAM_PREP">Ôn thi</option>
                                <option value="KNOWLEDGE_IMPROVEMENT">Nâng cao kiến thức</option>
                                <option value="FUN">Học cho vui</option>
                            </select>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Vai trò</label>
                            <select name="role" 
                            value={formData?.role || 'USER'} 
                            onChange={handleChange} 
                            className="appearance-none block w-full px-3 py-3 border border-gray-300 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                            >
                                <option value="USER">User</option>
                                <option value="ADMIN">Admin</option>
                            </select>
                        </div>
                        <div className="flex items-center space-x-4">
                            <label className="flex items-center">
                                <input type="checkbox" name="verified" checked={formData?.verified || false} onChange={handleChange} className="rounded border-gray-300 text-indigo-600 shadow-sm focus:ring-indigo-500" />
                                <span className="ml-2 text-sm text-gray-700">Đã xác thực</span>
                            </label>
                        </div>
                    </div>
                    <div className="mt-6 flex justify-end gap-3">
                        <button type="button" onClick={onClose} className="px-4 py-2 bg-gray-200 text-white rounded-md hover:bg-gray-300">Hủy</button>
                        <button type="submit" disabled={isSaving} className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-blue-300">
                            {isSaving ? 'Đang lưu...' : 'Lưu thay đổi'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

const UserCreateModal = ({ isOpen, onClose, onSave }) => {
    const initialFormData = {
        email: '',
        password: '',
        name: '',
        role: 'ADMIN',
        grade: '',
        goal: ''
    };
    const [formData, setFormData] = useState(initialFormData);
    const [isSaving, setIsSaving] = useState(false);

    if (!isOpen) return null;

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSaving(true);
        try {
            await onSave(formData);
            toast.success("Tạo người dùng thành công!");
            setFormData(initialFormData); // Reset form
            onClose();
        } catch (error) {
            toast.error(error.response?.data?.message || "Tạo người dùng thất bại.");
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
            <div className="bg-white rounded-lg shadow-xl w-full max-w-lg p-6 m-4 max-h-[90vh] overflow-y-auto">
                <h2 className="text-xl font-bold mb-4 text-gray-800">Thêm tài khoản ADMIN mới</h2>
                <form onSubmit={handleSubmit}>
                    <div className="space-y-4">
                        <input name="email" value={formData.email} onChange={handleChange} placeholder="Email" required className="w-full p-3 border rounded-lg text-gray-800" />
                        <input type="password" name="password" value={formData.password} onChange={handleChange} placeholder="Mật khẩu (ít nhất 8 ký tự)" required className="w-full p-3 border rounded-lg text-gray-800" />
                        <input name="name" value={formData.name} onChange={handleChange} placeholder="Tên hiển thị" required className="w-full p-3 border rounded-lg text-gray-800" />
                        <div className="w-full p-3 border border-gray-300 rounded-lg bg-gray-100 text-gray-500">
                            <span className="font-medium">Vai trò: </span>
                            <span className="font-semibold text-gray-800">ADMIN</span>
                        </div>
                    </div>
                    <div className="mt-6 flex justify-end gap-3">
                        <button type="button" onClick={onClose} className="px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300">Hủy</button>
                        <button type="submit" disabled={isSaving} className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:bg-green-300">
                            {isSaving ? 'Đang tạo...' : 'Tạo người dùng'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default function UserManagementPage() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [pagination, setPagination] = useState({ page: 0, size: 10, totalPages: 0 });
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [selectedUser, setSelectedUser] = useState(null);
    const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
    const [userToToggle, setUserToToggle] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [filters, setFilters] = useState({ role: '', enabled: '', verified: '' });
    const [searchTerm, setSearchTerm] = useState('');
    const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');

    // Debounce search term để tránh gọi API liên tục khi gõ
    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedSearchTerm(searchTerm);
            setPagination(p => ({ ...p, page: 0 })); // Reset về trang đầu khi tìm kiếm
        }, 500);
        return () => clearTimeout(handler);
    }, [searchTerm]);

    const loadUsers = useCallback(async (pageToLoad) => {
        setLoading(true);
        try {
            const currentPage = pageToLoad ?? pagination.page;
            const params = { page: currentPage, size: pagination.size, keyword: debouncedSearchTerm, ...filters };
            // Loại bỏ các giá trị filter rỗng
            const finalParams = Object.fromEntries(Object.entries(params).filter(([_, v]) => v !== '' && v != null));

            const response = await userService.getUsersForAdmin(finalParams);
            setUsers(response.content || []);
            setPagination({
                page: response.number || 0,
                size: response.size || 10,
                totalPages: response.totalPages || 0,
            });
        } catch (error) {
            toast.error("Không thể tải danh sách người dùng.");
            console.error("Failed to fetch users:", error);
        } finally {
            setLoading(false);
        }
    }, [debouncedSearchTerm, filters, pagination.page, pagination.size]);

    useEffect(() => {
        loadUsers();
    }, [debouncedSearchTerm, filters, loadUsers]); // Tải lại khi filter hoặc search term thay đổi

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({ ...prev, [name]: value }));
    };

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < pagination.totalPages) {
            loadUsers(newPage);
        }
    };

    const handleEdit = (user) => {
        setSelectedUser(user);
        setIsModalOpen(true);
    };

    const handleSave = async (userId, updatedData) => {
        // Chỉ gửi các trường cần thiết cho backend
        const payload = {
            name: updatedData.name,
            grade: updatedData.grade,
            goal: updatedData.goal,
            role: updatedData.role,
            enabled: updatedData.enabled,
            verified: updatedData.verified,
        };
        await userService.updateUserByAdmin(userId, payload);
        // Tải lại danh sách để cập nhật thay đổi
        loadUsers(pagination.page);
    };

    const handleCreate = async (newUserData) => {
        await userService.createUserByAdmin(newUserData);
        // Tải lại trang đầu tiên để thấy người dùng mới
        loadUsers(0);
    };

    const handleToggleStatusClick = (user) => {
        setUserToToggle(user);
        setIsConfirmModalOpen(true);
    };

    const confirmToggleStatus = async () => {
        if (!userToToggle) return;

        setIsSubmitting(true);
        try {
            const payload = {
                ...userToToggle, // Giữ lại các thông tin khác
                enabled: !userToToggle.enabled, // Đảo ngược trạng thái
            };
            await userService.updateUserByAdmin(userToToggle.id, payload);
            toast.success(`Đã ${payload.enabled ? 'kích hoạt' : 'vô hiệu hóa'} tài khoản ${userToToggle.email}`);
            setIsConfirmModalOpen(false);
            setUserToToggle(null);
            loadUsers(pagination.page); // Tải lại danh sách
        } catch (error) {
            toast.error(error.response?.data?.message || "Thao tác thất bại.");
        } finally {
            setIsSubmitting(false);
        }
    };




    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString('vi-VN', {
            year: 'numeric', month: '2-digit', day: '2-digit',
        });
    };

    return (
        <div className="p-6 bg-gray-50 min-h-full">
            <div className="flex justify-between items-center mb-4">
                <h1 className="text-2xl font-bold text-gray-800">Quản lý người dùng</h1>
                <button
                    onClick={() => setIsCreateModalOpen(true)}
                    className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition shadow-sm"
                >
                    <UserPlus size={18} />
                    Thêm tài khoản ADMIN
                </button>
            </div>

            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 mb-6">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                    <input
                        type="text"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        placeholder="Tìm theo email, tên..."
                        className="w-full p-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-800"
                    />
                    <select
                        name="role"
                        value={filters.role}
                        onChange={handleFilterChange}
                        className="p-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-800"
                    >
                        <option value="">Tất cả vai trò</option>
                        <option value="USER">User</option>
                        <option value="ADMIN">Admin</option>
                    </select>
                    <select
                        name="enabled"
                        value={filters.enabled}
                        onChange={handleFilterChange}
                        className="p-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-800"
                    >
                        <option value="">Tất cả trạng thái</option>
                        <option value="true">Kích hoạt</option>
                        <option value="false">Vô hiệu</option>
                    </select>
                    <select
                        name="verified"
                        value={filters.verified}
                        onChange={handleFilterChange}
                        className="p-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-800"
                    >
                        <option value="">Tất cả xác thực</option>
                        <option value="true">Đã xác thực</option>
                        <option value="false">Chưa xác thực</option>
                    </select>
                </div>
            </div>

            <div className="bg-white rounded-lg shadow-sm border border-gray-200">
                <div className="overflow-x-auto">
                    <table className="min-w-full bg-white text-sm">
                        <thead className="bg-gray-50">
                            <tr>
                                <th className="py-3 px-4 border-b text-left font-medium text-gray-600">ID</th>
                                <th className="py-3 px-4 border-b text-left font-medium text-gray-600">Email</th>
                                <th className="py-3 px-4 border-b text-left font-medium text-gray-600">Tên</th>
                                <th className="py-3 px-4 border-b text-left font-medium text-gray-600">Vai trò</th>
                                <th className="py-3 px-4 border-b text-center font-medium text-gray-600">Trạng thái</th>
                                <th className="py-3 px-4 border-b text-center font-medium text-gray-600">Xác thực</th>
                                <th className="py-3 px-4 border-b text-left font-medium text-gray-600">Ngày tạo</th>
                                <th className="py-3 px-4 border-b text-center font-medium text-gray-600">Hành động</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <tr><td colSpan="8" className="text-center p-8 text-gray-500">Đang tải...</td></tr>
                            ) : users.length > 0 ? (
                                users.map(user => (
                                    <tr key={user.id} className="hover:bg-gray-50">
                                        <td className="py-3 px-4 border-b font-mono text-gray-500">{user.id}</td>
                                        <td className="py-3 px-4 border-b font-medium text-gray-800">{user.email}</td>
                                        <td className="py-3 px-4 border-b text-gray-600">{user.name}</td>
                                        <td className="py-3 px-4 border-b">
                                            <RoleBadge role={user.role} />
                                        </td>
                                        <td className="py-3 px-4 border-b text-center">
                                            <StatusBadge status={user.enabled} text={user.enabled ? 'Kích hoạt' : 'Vô hiệu'} />
                                        </td>
                                        <td className="py-3 px-4 border-b text-center">
                                            <StatusBadge status={user.verified} text={user.verified ? 'Đã xác thực' : 'Chưa'} />
                                        </td>
                                        <td className="py-3 px-4 border-b text-gray-600">{formatDate(user.createdAt)}</td>
                                        <td className="py-3 px-4 border-b text-center space-x-1">
                                            <button onClick={() => handleEdit(user)} className="p-1.5 rounded-md text-blue-600 hover:bg-blue-100 hover:text-blue-700 transition" title="Sửa">
                                                <Edit size={16} />
                                            </button>
                                            <button
                                                onClick={() => handleToggleStatusClick(user)}
                                                className={`p-1.5 rounded-md transition ${user.enabled ? 'text-red-600 hover:bg-red-100' : 'text-green-600 hover:bg-green-100'}`}
                                                title={user.enabled ? 'Vô hiệu hóa' : 'Kích hoạt'}
                                            >
                                                {user.enabled ? <UserX size={16} /> : <UserCheck size={16} />}
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr><td colSpan="8" className="text-center p-8 text-gray-500">Không tìm thấy người dùng nào.</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>

                {pagination.totalPages > 1 && (
                    <div className="flex justify-center items-center gap-2 p-4 border-t">
                        <button
                            onClick={() => handlePageChange(pagination.page - 1)}
                            disabled={pagination.page === 0}
                            className="px-3 py-1 rounded bg-white border border-gray-200 text-gray-600 hover:bg-gray-50 disabled:opacity-50"
                        >
                            Trước
                        </button>
                        <span className="text-sm text-gray-600">
                            Trang {pagination.page + 1} / {pagination.totalPages}
                        </span>
                        <button
                            onClick={() => handlePageChange(pagination.page + 1)}
                            disabled={pagination.page === pagination.totalPages - 1}
                            className="px-3 py-1 rounded bg-white border border-gray-200 text-gray-600 hover:bg-gray-50 disabled:opacity-50"
                        >
                            Sau
                        </button>
                    </div>
                )}
            </div>

            <UserEditModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                user={selectedUser}
                onSave={handleSave}
            />

            <UserCreateModal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
                onSave={handleCreate}
            />

            <ConfirmationModal
                isOpen={isConfirmModalOpen}
                onClose={() => setIsConfirmModalOpen(false)}
                onConfirm={confirmToggleStatus}
                title={`Xác nhận ${userToToggle?.enabled ? 'vô hiệu hóa' : 'kích hoạt'} tài khoản`}
                message={`Bạn có chắc chắn muốn ${userToToggle?.enabled ? 'vô hiệu hóa' : 'kích hoạt'} tài khoản "${userToToggle?.email}" không?`}
                confirmText={userToToggle?.enabled ? 'Vô hiệu hóa' : 'Kích hoạt'}
                isLoading={isSubmitting}
                variant={userToToggle?.enabled ? 'danger' : 'default'}
            />
        </div>
    );
}