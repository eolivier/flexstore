# DraftJsonProduct


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**category** | **string** |  | [default to undefined]
**price** | **number** |  | [default to undefined]
**currency** | **string** |  | [default to undefined]
**translations** | [**Array&lt;JsonProductTranslation&gt;**](JsonProductTranslation.md) | The name and the description in each language of the product, the default one (en) required | [default to undefined]

## Example

```typescript
import { DraftJsonProduct } from './api';

const instance: DraftJsonProduct = {
    category,
    price,
    currency,
    translations,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
