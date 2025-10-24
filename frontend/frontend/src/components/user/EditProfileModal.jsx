import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';

const EditProfileModal = ({ user, isOpen, onClose, onSave, isLoading }) => {
    const [formData, setFormData] = useState({
        name: '',
        grade: '',
        goal: '',
    });

    useEffect(() => {
        if (isOpen && user) {
            setFormData({
                name: user.name || '',
                grade: user.grade || '',
                goal: user.goal || '',
            });
        }
    }, [isOpen, user]);

    if (!isOpen) return null;

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        await onSave(formData);
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50 p-4">
            <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6 m-4 max-h-[90vh] overflow-y-auto">
                <h2 className="text-2xl font-bold text-gray-800 mb-6 text-center">Chỉnh sửa hồ sơ</h2>
                <form onSubmit={handleSubmit} className="space-y-5">
                    <div>
                        <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">Tên hiển thị</label>
                        <input
                            type="text"
                            id="name"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent text-gray-800"
                            placeholder="Nhập tên của bạn"
                        />
                    </div>
                    <div>
                        <label htmlFor="grade" className="block text-sm font-medium text-gray-700 mb-1">Lớp học</label>
                        <select
                            id="grade"
                            name="grade"
                            value={formData.grade}
                            onChange={handleChange}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent text-gray-800"
                        >
                            <option value="">Chọn lớp</option>
                            <option value="10">Lớp 10</option>
                            <option value="11">Lớp 11</option>
                            <option value="12">Lớp 12</option>
                            <option value="OTHER">Khác</option>
                        </select>
                    </div>
                    <div>
                        <label htmlFor="goal" className="block text-sm font-medium text-gray-700 mb-1">Mục tiêu học tập</label>
                        <select
                            id="goal"
                            name="goal"
                            value={formData.goal}
                            onChange={handleChange}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent text-gray-800"
                        >
                            <option value="">Chọn mục tiêu</option>
                            <option value="EXAM_PREP">Ôn thi</option>
                            <option value="KNOWLEDGE_IMPROVEMENT">Nâng cao kiến thức</option>
                            <option value="FUN">Học cho vui</option>
                        </select>
                    </div>
                    <div className="flex justify-end gap-3 mt-8">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-5 py-2.5 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition-colors duration-200"
                            disabled={isLoading}
                        >
                            Hủy
                        </button>
                        <button
                            type="submit"
                            className="px-5 py-2.5 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors duration-200 disabled:bg-green-300"
                            disabled={isLoading}
                        >
                            {isLoading ? 'Đang lưu...' : 'Lưu thay đổi'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default EditProfileModal;
