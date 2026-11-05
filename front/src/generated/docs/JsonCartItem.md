# JsonCartItem


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**itemId** | **string** |  | [optional] [default to undefined]
**productId** | **string** |  | [optional] [default to undefined]
**productName** | **string** |  | [optional] [default to undefined]
**productDescription** | **string** |  | [optional] [default to undefined]
**productCategory** | **string** |  | [optional] [default to undefined]
**productPrice** | **number** | The price to pay for one product, the promotion included | [optional] [default to undefined]
**productCurrency** | **string** |  | [optional] [default to undefined]
**productQuantity** | **number** |  | [optional] [default to undefined]
**itemPrice** | **number** |  | [optional] [default to undefined]
**regularPrice** | **number** | The price of one product without the promotion. Shown struck through when the product is on promotion | [optional] [default to undefined]
**promotionPercentage** | **number** | The percentage off the regular price while a promotion is running, null otherwise | [optional] [default to undefined]

## Example

```typescript
import { JsonCartItem } from './api';

const instance: JsonCartItem = {
    itemId,
    productId,
    productName,
    productDescription,
    productCategory,
    productPrice,
    productCurrency,
    productQuantity,
    itemPrice,
    regularPrice,
    promotionPercentage,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
