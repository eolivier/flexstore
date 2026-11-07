import { defineStore } from 'pinia';
import { ref } from 'vue';
import { JsonShopPageContentTypeEnum, type JsonShopPageContents } from '@/generated';
import { shopPageContentApi } from '@/api';
import { useAuthStore } from '@/stores/auth.store';

// The types of content, in the order of the administration screen
export type ShopPageContentType = JsonShopPageContentTypeEnum;
export const shopPageContentTypes: ShopPageContentType[] = [
  JsonShopPageContentTypeEnum.MetaShopDescription,
  JsonShopPageContentTypeEnum.MetaKeyWords,
  JsonShopPageContentTypeEnum.GoogleAnalyticsId,
];

// 1 000 characters at most, like the back end
export const MAXIMUM_SHOP_PAGE_CONTENT_LENGTH = 1000;

// saved; invalid: refused by the shop (too long, or not an identifier); failed: not reached
export type ShopPageContentEdit = 'saved' | 'invalid' | 'failed';

// How a type is named in the paths of the API: META_KEY_WORDS is meta-key-words
function pathOf(type: ShopPageContentType) {
  return type.toLowerCase().replace(/_/g, '-');
}

// What the shop says about itself in its pages: read by everyone (the head of the pages), edited by the
// administrators. A text is empty until the administrators write it
export const useShopPageContentStore = defineStore('shopPageContent', () => {
  const texts = ref<Record<string, string>>({});
  const loadError = ref(false);
  const authStore = useAuthStore();

  function textOf(type: ShopPageContentType) {
    return texts.value[type] ?? '';
  }

  function keep(contents: JsonShopPageContents) {
    contents.contents.forEach((content) => {
      texts.value[content.type] = content.text;
    });
  }

  async function fetchContents() {
    loadError.value = false;
    const response = await shopPageContentApi.getShopPageContents().catch(() => null);
    if (response) {
      keep(response.data);
    } else {
      loadError.value = true;
    }
  }

  async function editContent(type: ShopPageContentType, text: string): Promise<ShopPageContentEdit> {
    const response = await authStore
      .authFetch(`/api/admin/shop-page-contents/${pathOf(type)}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text }),
      })
      .catch(() => null);
    if (response?.status === 400) return 'invalid';
    if (!response?.ok) return 'failed';
    keep(await response.json());
    return 'saved';
  }

  return { texts, loadError, textOf, fetchContents, editContent };
});
