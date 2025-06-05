import React, { useState, useEffect } from 'react';
import { Search, Plus, Edit, Trash2, Eye, X, Save } from 'lucide-react';

const StorageUnitManagement = () => {
    const [storageUnits, setStorageUnits] = useState([]);
    const [filteredUnits, setFilteredUnits] = useState([]);
    const [loading, setLoading] = useState(false);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [modalMode, setModalMode] = useState('add'); // 'add', 'edit', 'view'
    const [selectedUnit, setSelectedUnit] = useState(null);
    const [formData, setFormData] = useState({
        id: '',
        name: '',
        size: '',
        location: '',
        price: '',
        status: 'AVAILABLE',
        description: ''
    });

    // Mock data - thay thế bằng API calls thực tế
    const mockData = [
        {
            id: 1,
            name: 'Unit A-001',
            size: '5x10',
            location: 'Building A, Floor 1',
            price: 150000,
            status: 'AVAILABLE',
            description: 'Climate controlled storage unit'
        },
        {
            id: 2,
            name: 'Unit B-002',
            size: '10x10',
            location: 'Building B, Floor 2',
            price: 250000,
            status: 'OCCUPIED',
            description: 'Standard storage unit'
        },
        {
            id: 3,
            name: 'Unit C-003',
            size: '5x5',
            location: 'Building C, Floor 1',
            price: 100000,
            status: 'MAINTENANCE',
            description: 'Small storage unit'
        }
    ];

    useEffect(() => {
        fetchStorageUnits();
    }, []);

    useEffect(() => {
        if (searchKeyword) {
            handleSearch();
        } else {
            setFilteredUnits(storageUnits);
        }
    }, [searchKeyword, storageUnits]);

    // API calls - thay thế URL bằng backend endpoint thực tế
    const fetchStorageUnits = async () => {
        setLoading(true);
        try {
            // const response = await fetch('/api/storage-units');
            // const data = await response.json();
            // setStorageUnits(data);

            // Mock data cho demo
            setTimeout(() => {
                setStorageUnits(mockData);
                setFilteredUnits(mockData);
                setLoading(false);
            }, 1000);
        } catch (error) {
            console.error('Error fetching storage units:', error);
            setLoading(false);
        }
    };

    const handleSearch = async () => {
        if (!searchKeyword.trim()) {
            setFilteredUnits(storageUnits);
            return;
        }

        try {
            // const response = await fetch(`/api/storage-units/search?keyword=${searchKeyword}`);
            // const data = await response.json();
            // setFilteredUnits(data);

            // Mock search
            const filtered = storageUnits.filter(unit =>
                unit.name.toLowerCase().includes(searchKeyword.toLowerCase()) ||
                unit.location.toLowerCase().includes(searchKeyword.toLowerCase()) ||
                unit.status.toLowerCase().includes(searchKeyword.toLowerCase())
            );
            setFilteredUnits(filtered);
        } catch (error) {
            console.error('Error searching storage units:', error);
        }
    };

    const handleAdd = async () => {
        try {
            // const response = await fetch('/api/storage-units', {
            //   method: 'POST',
            //   headers: { 'Content-Type': 'application/json' },
            //   body: JSON.stringify(formData)
            // });
            // const newUnit = await response.json();

            // Mock add
            const newUnit = { ...formData, id: Date.now() };
            setStorageUnits([...storageUnits, newUnit]);
            setShowModal(false);
            resetForm();
        } catch (error) {
            console.error('Error adding storage unit:', error);
        }
    };

    const handleUpdate = async () => {
        try {
            // const response = await fetch(`/api/storage-units/${formData.id}`, {
            //   method: 'PUT',
            //   headers: { 'Content-Type': 'application/json' },
            //   body: JSON.stringify(formData)
            // });
            // const updatedUnit = await response.json();

            // Mock update
            setStorageUnits(storageUnits.map(unit =>
                unit.id === formData.id ? formData : unit
            ));
            setShowModal(false);
            resetForm();
        } catch (error) {
            console.error('Error updating storage unit:', error);
        }
    };

    const handleDelete = async (id) => {
        if (!confirm('Bạn có chắc chắn muốn xóa storage unit này?')) return;

        try {
            // await fetch(`/api/storage-units/${id}`, { method: 'DELETE' });

            // Mock delete
            setStorageUnits(storageUnits.filter(unit => unit.id !== id));
        } catch (error) {
            console.error('Error deleting storage unit:', error);
        }
    };

    const openModal = (mode, unit = null) => {
        setModalMode(mode);
        setSelectedUnit(unit);
        if (unit) {
            setFormData(unit);
        } else {
            resetForm();
        }
        setShowModal(true);
    };

    const resetForm = () => {
        setFormData({
            id: '',
            name: '',
            size: '',
            location: '',
            price: '',
            status: 'AVAILABLE',
            description: ''
        });
    };

    const handleSubmit = () => {
        if (modalMode === 'add') {
            handleAdd();
        } else if (modalMode === 'edit') {
            handleUpdate();
        }
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'AVAILABLE': return 'bg-green-100 text-green-800';
            case 'OCCUPIED': return 'bg-red-100 text-red-800';
            case 'MAINTENANCE': return 'bg-yellow-100 text-yellow-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    const formatPrice = (price) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(price);
    };

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">Storage Unit Management</h1>
                    <p className="text-gray-600">Quản lý các đơn vị lưu trữ trong hệ thống</p>
                </div>

                {/* Search and Add Section */}
                <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
                    <div className="flex flex-col sm:flex-row gap-4 items-center justify-between">
                        <div className="relative flex-1 max-w-md">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                            <input
                                type="text"
                                placeholder="Tìm kiếm theo tên, vị trí, trạng thái..."
                                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                value={searchKeyword}
                                onChange={(e) => setSearchKeyword(e.target.value)}
                            />
                        </div>
                        <button
                            onClick={() => openModal('add')}
                            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center gap-2 transition-colors"
                        >
                            <Plus className="w-5 h-5" />
                            Thêm Storage Unit
                        </button>
                    </div>
                </div>

                {/* Storage Units Table */}
                <div className="bg-white rounded-lg shadow-sm overflow-hidden">
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Tên Unit
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Kích thước
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Vị trí
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Giá
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Trạng thái
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Thao tác
                                    </th>
                                </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                                {loading ? (
                                    <tr>
                                        <td colSpan="6" className="px-6 py-4 text-center">
                                            <div className="flex justify-center items-center">
                                                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                                                <span className="ml-2">Đang tải...</span>
                                            </div>
                                        </td>
                                    </tr>
                                ) : filteredUnits.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" className="px-6 py-4 text-center text-gray-500">
                                            Không có dữ liệu
                                        </td>
                                    </tr>
                                ) : (
                                    filteredUnits.map((unit) => (
                                        <tr key={unit.id} className="hover:bg-gray-50">
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <div className="font-medium text-gray-900">{unit.name}</div>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-gray-700">
                                                {unit.size}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-gray-700">
                                                {unit.location}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-gray-700">
                                                {formatPrice(unit.price)}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(unit.status)}`}>
                                                    {unit.status}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                                <div className="flex space-x-2">
                                                    <button
                                                        onClick={() => openModal('view', unit)}
                                                        className="text-blue-600 hover:text-blue-900 p-1 rounded hover:bg-blue-50"
                                                        title="Xem chi tiết"
                                                    >
                                                        <Eye className="w-4 h-4" />
                                                    </button>
                                                    <button
                                                        onClick={() => openModal('edit', unit)}
                                                        className="text-green-600 hover:text-green-900 p-1 rounded hover:bg-green-50"
                                                        title="Chỉnh sửa"
                                                    >
                                                        <Edit className="w-4 h-4" />
                                                    </button>
                                                    <button
                                                        onClick={() => handleDelete(unit.id)}
                                                        className="text-red-600 hover:text-red-900 p-1 rounded hover:bg-red-50"
                                                        title="Xóa"
                                                    >
                                                        <Trash2 className="w-4 h-4" />
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Modal */}
                {showModal && (
                    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                        <div className="bg-white rounded-lg p-6 w-full max-w-md mx-4">
                            <div className="flex items-center justify-between mb-4">
                                <h2 className="text-xl font-semibold">
                                    {modalMode === 'add' ? 'Thêm Storage Unit' :
                                        modalMode === 'edit' ? 'Chỉnh sửa Storage Unit' : 'Chi tiết Storage Unit'}
                                </h2>
                                <button
                                    onClick={() => setShowModal(false)}
                                    className="text-gray-400 hover:text-gray-600"
                                >
                                    <X className="w-6 h-6" />
                                </button>
                            </div>

                            <div>
                                <div className="space-y-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Tên Unit *
                                        </label>
                                        <input
                                            type="text"
                                            required
                                            disabled={modalMode === 'view'}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                                            value={formData.name}
                                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                        />
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Kích thước *
                                        </label>
                                        <input
                                            type="text"
                                            required
                                            disabled={modalMode === 'view'}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                                            value={formData.size}
                                            onChange={(e) => setFormData({ ...formData, size: e.target.value })}
                                        />
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Vị trí *
                                        </label>
                                        <input
                                            type="text"
                                            required
                                            disabled={modalMode === 'view'}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                                            value={formData.location}
                                            onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                                        />
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Giá (VND) *
                                        </label>
                                        <input
                                            type="number"
                                            required
                                            disabled={modalMode === 'view'}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                                            value={formData.price}
                                            onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                                        />
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Trạng thái *
                                        </label>
                                        <select
                                            required
                                            disabled={modalMode === 'view'}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                                            value={formData.status}
                                            onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                                        >
                                            <option value="AVAILABLE">AVAILABLE</option>
                                            <option value="OCCUPIED">OCCUPIED</option>
                                            <option value="MAINTENANCE">MAINTENANCE</option>
                                        </select>
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Mô tả
                                        </label>
                                        <textarea
                                            disabled={modalMode === 'view'}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                                            rows="3"
                                            value={formData.description}
                                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                        />
                                    </div>
                                </div>

                                {modalMode !== 'view' && (
                                    <div className="flex gap-3 mt-6">
                                        <button
                                            type="button"
                                            onClick={() => setShowModal(false)}
                                            className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
                                        >
                                            Hủy
                                        </button>
                                        <button
                                            type="button"
                                            onClick={handleSubmit}
                                            className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center justify-center gap-2"
                                        >
                                            <Save className="w-4 h-4" />
                                            {modalMode === 'add' ? 'Thêm' : 'Cập nhật'}
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default StorageUnitManagement;