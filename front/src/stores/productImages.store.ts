import { defineStore } from 'pinia';
import { ref } from 'vue';
import { apiBaseUrl, productImagesApi } from '@/api';
import { useAuthStore } from './auth.store';

// small: the cards of the catalog and the cart; large: the page of the product
export type ImageFormat = 'small' | 'large';

// tooLarge: more than 2 MB; unsupported: not a JPEG, PNG or WebP image
export type ImageUpload = 'saved' | 'tooLarge' | 'unsupported' | 'error';

export const MAX_IMAGE_BYTES = 2 * 1024 * 1024;

// The images of the products: which products have one, and their address
export const useProductImagesStore = defineStore('productImages', () => {
  const productIds = ref<Set<string>>(new Set());
  // Changed with each image given or removed, so that the browser does not show the previous one
  const version = ref(0);
  const authStore = useAuthStore();

  async function fetchProductsWithImage() {
    const response = await productImagesApi.getProductsWithImage().catch(() => null);
    if (response) productIds.value = new Set(response.data);
  }

  function hasImage(productId: string | undefined): boolean {
    return productId !== undefined && productIds.value.has(productId);
  }

  function imageUrl(productId: string, format: ImageFormat): string {
    const path = `/api/products/${encodeURIComponent(productId)}/image?format=${format}&v=${version.value}`;
    return `${apiBaseUrl}${path}`;
  }

  // For the administrators: the image is resized by the back end
  async function uploadImage(productId: string, file: File): Promise<ImageUpload> {
    if (file.size > MAX_IMAGE_BYTES) return 'tooLarge';
    const body = new FormData();
    body.append('image', file);
    const response = await authStore
      .authFetch(`/api/admin/products/${encodeURIComponent(productId)}/image`, { method: 'PUT', body })
      .catch(() => null);
    if (response?.status === 413) return 'tooLarge';
    if (response?.status === 415) return 'unsupported';
    if (!response?.ok) return 'error';
    productIds.value = new Set([...productIds.value, productId]);
    version.value++;
    return 'saved';
  }

  // Returns whether the image was removed
  async function removeImage(productId: string): Promise<boolean> {
    const response = await authStore
      .authFetch(`/api/admin/products/${encodeURIComponent(productId)}/image`, { method: 'DELETE' })
      .catch(() => null);
    if (!response?.ok) return false;
    productIds.value = new Set([...productIds.value].filter((id) => id !== productId));
    version.value++;
    return true;
  }

  return { productIds, version, fetchProductsWithImage, hasImage, imageUrl, uploadImage, removeImage };
});
