-- =============================================
-- SWP Database Setup Script
-- =============================================

-- 1. Tạo database (nếu chưa có)
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'SWP')
BEGIN
    CREATE DATABASE SWP;
END
GO

-- 2. Sử dụng database SWP
USE SWP;
GO

-- 3. Xóa bảng cũ nếu tồn tại
IF OBJECT_ID('Customer', 'U') IS NOT NULL
    DROP TABLE Customer;
GO

-- 4. Tạo bảng Customer
CREATE TABLE Customer (
    customerId INT PRIMARY KEY IDENTITY(1,1),
    fullname NVARCHAR(255) NOT NULL,
    email NVARCHAR(255) UNIQUE NOT NULL,
    password NVARCHAR(255) NOT NULL,
    phone NVARCHAR(20),
    address NVARCHAR(500),
    city NVARCHAR(100),
    district NVARCHAR(100),
    createdAt DATETIME2 DEFAULT GETDATE(),
    status NVARCHAR(50) DEFAULT 'ACTIVE'
);
GO

-- 5. Thêm data mẫu
INSERT INTO Customer (fullname, email, password, phone, address, city, district, status) VALUES
('Nguyễn Văn An', 'nguyenvanan@gmail.com', 'password123', '0901234567', '123 Đường ABC', 'Hà Nội', 'Ba Đình', 'ACTIVE'),
('Trần Thị Bình', 'tranthibinh@gmail.com', 'password123', '0901234568', '456 Đường DEF', 'Hồ Chí Minh', 'Quận 1', 'ACTIVE'),
('Phạm Minh Cường', 'phamminhcuong@gmail.com', 'password123', '0901234569', '789 Đường GHI', 'Đà Nẵng', 'Hải Châu', 'ACTIVE'),
('Lê Thị Dung', 'lethidung@gmail.com', 'password123', '0901234570', '321 Đường JKL', 'Hà Nội', 'Hoàn Kiếm', 'BLOCKED'),
('Hoàng Văn Em', 'hoangvanem@gmail.com', 'password123', '0901234571', '654 Đường MNO', 'Hồ Chí Minh', 'Quận 2', 'ACTIVE'),
('Vũ Thị Phương', 'vuthiphuong@gmail.com', 'password123', '0901234572', '987 Đường PQR', 'Hải Phòng', 'Ngô Quyền', 'ACTIVE'),
('Đặng Minh Quân', 'dangminhquan@gmail.com', 'password123', '0901234573', '147 Đường STU', 'Cần Thơ', 'Ninh Kiều', 'ACTIVE'),
('Bùi Thị Hoa', 'buithihoa@gmail.com', 'password123', '0901234574', '258 Đường VWX', 'Hà Nội', 'Đống Đa', 'ACTIVE'),
('Ngô Văn Inh', 'ngovaninh@gmail.com', 'password123', '0901234575', '369 Đường YZ', 'Đà Nẵng', 'Thanh Khê', 'BLOCKED'),
('Tôn Thị Kim', 'tonthikim@gmail.com', 'password123', '0901234576', '741 Đường ABC', 'Hồ Chí Minh', 'Quận 3', 'ACTIVE');
GO

-- 6. Kiểm tra dữ liệu đã tạo
SELECT * FROM Customer;
GO

-- 7. Hiển thị thống kê
SELECT 
    COUNT(*) as TotalCustomers,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as ActiveCustomers,
    COUNT(CASE WHEN status = 'BLOCKED' THEN 1 END) as BlockedCustomers
FROM Customer;
GO

PRINT 'Database SWP đã được tạo thành công!';
PRINT 'Bảng Customer đã được tạo với 10 records mẫu.'; 