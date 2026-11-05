export interface CartItems {
  items: Item[];
  totalItemsPrice: number;
  itemsCurrency: string;
}

export interface Item {
  itemId: string;
  productId?: string;
  productName?: string;
  productDescription?: string;
  productCategory?: string;
  // The price to pay for one product, the promotion included
  productPrice?: number;
  productCurrency?: string;
  productQuantity: number;
  itemPrice?: number;
  // The price without the promotion, and the percentage off while a promotion is running
  regularPrice?: number;
  promotionPercentage?: number | null;
}
