# JsonAdminProduct

A product as the administrator edits it, with its translations in every language

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [default to undefined]
**category** | **string** |  | [default to undefined]
**price** | **number** |  | [default to undefined]
**currency** | **string** |  | [default to undefined]
**translations** | [**Array&lt;JsonProductTranslation&gt;**](JsonProductTranslation.md) | The name and the description in every language of the product, the default one (en) included | [default to undefined]

## Example

```typescript
import { JsonAdminProduct } from './api';

const instance: JsonAdminProduct = {
    id,
    category,
    price,
    currency,
    translations,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
