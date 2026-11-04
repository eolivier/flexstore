# JsonProduct


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [default to undefined]
**sku** | **string** | Stock Keeping Unit, unique in the catalog | [default to undefined]
**name** | **string** | In the language asked (Accept-Language), or in en when the product is not translated into it | [default to undefined]
**description** | **string** | In the language asked (Accept-Language), or in en when the product is not translated into it | [default to undefined]
**category** | **string** |  | [default to undefined]
**price** | **number** | The regular price. Shown struck through when the product is on promotion | [default to undefined]
**currency** | **string** |  | [default to undefined]
**promotionPercentage** | **number** | The percentage off the regular price while a promotion is running, null otherwise | [optional] [default to undefined]
**currentPrice** | **number** | The price to pay, the promotion included (the regular price when there is none) | [default to undefined]

## Example

```typescript
import { JsonProduct } from './api';

const instance: JsonProduct = {
    id,
    sku,
    name,
    description,
    category,
    price,
    currency,
    promotionPercentage,
    currentPrice,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
