# DraftJsonProduct


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**sku** | **string** | Stock Keeping Unit, unique in the catalog: upper-case letters and digits in groups separated by hyphens, 32 characters at most (TSHIRT-ORGANIC-M); the spaces around are dropped, the letters put in upper case | [default to undefined]
**category** | **string** |  | [default to undefined]
**price** | **number** |  | [default to undefined]
**currency** | **string** |  | [default to undefined]
**translations** | [**Array&lt;JsonProductTranslation&gt;**](JsonProductTranslation.md) | The name and the description in each language of the product, the default one (en) required | [default to undefined]

## Example

```typescript
import { DraftJsonProduct } from './api';

const instance: DraftJsonProduct = {
    sku,
    category,
    price,
    currency,
    translations,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
