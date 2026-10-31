# JsonAdminProduct

A product as the administrator edits it, with its translations in every language

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [default to undefined]
**sku** | **string** | Stock Keeping Unit, unique in the catalog | [default to undefined]
**category** | **string** |  | [default to undefined]
**price** | **number** |  | [default to undefined]
**currency** | **string** |  | [default to undefined]
**translations** | [**Array&lt;JsonProductTranslation&gt;**](JsonProductTranslation.md) | The name and the description in every language of the product, the default one (en) included | [default to undefined]
**vatRate** | **string** | The rate of VAT included in the price: 20, 10, 5.5 or 2.1 % | [default to undefined]

## Example

```typescript
import { JsonAdminProduct } from './api';

const instance: JsonAdminProduct = {
    id,
    sku,
    category,
    price,
    currency,
    translations,
    vatRate,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
