import { Configuration, ProductsApi } from "./generated";

const config = new Configuration({
  basePath: import.meta.env.VITE_API_URL || "http://localhost:8080"
});

export const productsApi = new ProductsApi(config);
