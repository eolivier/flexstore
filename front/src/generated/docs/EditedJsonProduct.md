# EditedJsonProduct

The fields an administrator edits; the id is given by the path

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**sku** | **string** | Stock Keeping Unit, unique in the catalog: upper-case letters and digits in groups separated by hyphens, 32 characters at most (TSHIRT-ORGANIC-M); the spaces around are dropped, the letters put in upper case | [default to undefined]
**category** | **string** |  | [default to undefined]
**price** | **number** |  | [default to undefined]
**currency** | **string** | Currency code (EUR) | [default to undefined]
**translations** | [**Array&lt;JsonProductTranslation&gt;**](JsonProductTranslation.md) | Every translation of the product, the default one (en) required; a translation not given is removed | [default to undefined]

## Example

```typescript
import { EditedJsonProduct } from './api';

const instance: EditedJsonProduct = {
    sku,
    category,
    price,
    currency,
    translations,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
