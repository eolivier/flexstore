import { Configuration, ProductsApi, StocksApi } from './generated';

const config = new Configuration({
  basePath: import.meta.env.VITE_API_URL || 'http://localhost:8080',
});

export const productsApi = new ProductsApi(config);
export const stocksApi = new StocksApi(config);

console.log('API URL:', config.basePath);
console.log('import.meta.env.VITE_API_URL:', import.meta.env.VITE_API_URL);
