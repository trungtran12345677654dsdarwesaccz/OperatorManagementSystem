import React, { useState } from 'react';
import { LaptopOutlined, NotificationOutlined, UserOutlined } from '@ant-design/icons';
import { Breadcrumb, Layout, Menu, theme, Form, Input, Button } from 'antd';

const { Header, Content, Footer, Sider } = Layout;

const items1 = ['Company Details', 'Job Details', 'Candidate Requirements', 'Job Description'].map(key => ({
    key,
    label: `${key}`,
}));

const labels = [
    'Customer',
    'List',
    'Information',
];
const childrenLabels = [
    ['Tùy chọn 1', 'Tùy chọn 2', 'Tùy chọn 3'],
    ['Máy 1', 'Máy 2', 'Máy 3'],
    ['Thông báo 1', 'Thông báo 2', 'Thông báo 3'],
];

const items2 = [UserOutlined, LaptopOutlined, NotificationOutlined].map((icon, index) => {
    const key = String(index + 1);
    return {
        key: `sub${key}`,
        icon: React.createElement(icon),
        label: labels[index], // Lấy label từ mảng labels
        children: Array.from({ length: 3 }).map((_, j) => {
            const subKey = index * 4 + j + 1;
            return {
                key: subKey,
                label: childrenLabels[index][j], // Lấy label con từ mảng childrenLabels

            };
        }),
    };
});

const JobForm = () => {
    const [formData, setFormData] = useState({
        companyName: 'Premier Coast Realty',
        yourName: 'John Smith',
        phone: '904-229-5287',
        state: 'NY',
        country: 'USA',
        address: '123 NY, USA',
    });

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = (values) => {
        console.log('Form submitted:', values);
        // Add logic to handle form submission (e.g., API call)
    };

    return (
        <Form
            layout="vertical"
            initialValues={formData}
            onFinish={handleSubmit}
            style={{ maxWidth: 600 }}
        >
            <Form.Item label="Company Name" name="companyName">
                <Input name="companyName" onChange={handleInputChange} />
            </Form.Item>
            <Form.Item label="Your Name" name="yourName">
                <Input name="yourName" onChange={handleInputChange} />
            </Form.Item>
            <Form.Item label="Phone" name="phone">
                <Input name="phone" onChange={handleInputChange} />
            </Form.Item>
            <Form.Item label="State" name="state">
                <Input name="state" onChange={handleInputChange} />
            </Form.Item>
            <Form.Item label="Country" name="country">
                <Input name="country" onChange={handleInputChange} />
            </Form.Item>
            <Form.Item label="Address" name="address">
                <Input name="address" onChange={handleInputChange} />
            </Form.Item>
            <Form.Item>
                <Button type="primary" htmlType="submit">
                    Submit
                </Button>
            </Form.Item>
        </Form>
    );
};

const App = () => {
    const {
        token: { colorBgContainer, borderRadiusLG },
    } = theme.useToken();
    const [collapsed, setCollapsed] = useState(false);
    const [selectedKey, setSelectedKey] = useState('2');

    const handleMenuClick = (e) => {
        setSelectedKey(e.key);
    };

    return (
        <Layout>
            <Header style={{ display: 'flex', alignItems: 'center' }}>
                <div className="demo-logo" style={{ marginRight: 20 }}>
                    <img src="https://st2.depositphotos.com/1020091/11637/v/450/depositphotos_116376136-stock-illustration-support-worker-icon.jpg" alt="Logo" style={{ height: 62 }} />

                </div>

                <Menu
                    theme="dark"
                    mode="horizontal"
                    selectedKeys={[selectedKey]}
                    items={items1}
                    onClick={handleMenuClick}
                    style={{ flex: 1, minWidth: 0 }}
                />
            </Header>

            <div style={{ padding: window.innerWidth > 768 ? '0 48px' : '0 16px' }}>
                <Breadcrumb
                    style={{ margin: '20px 0' }}
                    items={[{ title: 'Home' }, { title: 'List' }, { title: 'App' }]}
                />
                <Layout
                    style={{ padding: '50px 0', background: colorBgContainer, borderRadius: borderRadiusLG }}
                >
                    <Sider
                        collapsible
                        collapsed={collapsed}
                        onCollapse={(value) => setCollapsed(value)}
                        style={{ background: colorBgContainer }}
                        width={200}
                    >
                        <Menu
                            mode="inline"
                            defaultSelectedKeys={['1']}
                            defaultOpenKeys={['sub1']}
                            style={{ height: '100%' }}
                            items={items2}
                            onClick={handleMenuClick}
                        />
                    </Sider>
                    <Content style={{ padding: '0 24px', minHeight: 280 }}>
                        <JobForm />
                    </Content>
                    {/* <div style={{ display: 'inline-block' }}> */}

                    <img src="https://i.pinimg.com/736x/e8/4e/b6/e84eb69b1eff3122fc60fc1c312ba398.jpg" alt="Logo" style={{ height: 700, marginRight: 90, marginTop: -40 }} />

                    {/* <img src="https://i.pinimg.com/736x/21/99/23/219923777205b79cd718418de180c4f1.jpg" alt="Logo" style={{ height: 250, marginTop: '5px' }} /> */}


                    {/* </div > */}

                </Layout>
            </div>

            <Footer style={{ textAlign: 'center' }}>
                Ant Design ©{new Date().getFullYear()} Created by Phong
            </Footer>
        </Layout>
    );
};

export default App;