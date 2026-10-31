import { Configuration, ProductImagesApi, ProductsApi, ReviewsApi, StocksApi } from './generated';

const config = new Configuration({
  basePath: import.meta.env.VITE_API_URL || 'http://localhost:8080',
});

export const productsApi = new ProductsApi(config);
export const stocksApi = new StocksApi(config);
export const productImagesApi = new ProductImagesApi(config);
export const reviewsApi = new ReviewsApi(config);

// The address of the back end, for the links the browser follows itself (the images)
export const apiBaseUrl = config.basePath;

console.log('API URL:', config.basePath);
console.log('import.meta.env.VITE_API_URL:', import.meta.env.VITE_API_URL);
