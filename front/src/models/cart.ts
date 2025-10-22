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
  productPrice?: number;
  productCurrency?: string;
  productQuantity: number;
  itemPrice?: number;
}