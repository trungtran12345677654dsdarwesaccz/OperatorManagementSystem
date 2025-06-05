import axiosInstance from "./axiosinstance";

const productsApi = {
    getProducts: async () => {
        try {
            const response = await axiosInstance.get('products');//chờ server trả về danh sách sản phẩm
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return await response;
        } catch (error) {
            console.error('Error fetching products:', error);
            throw error;
        }
    }
};

export default productsApi;
