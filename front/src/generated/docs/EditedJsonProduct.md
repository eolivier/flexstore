# EditedJsonProduct

The fields an administrator edits; the id is given by the path

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**category** | **string** |  | [default to undefined]
**price** | **number** |  | [default to undefined]
**currency** | **string** | Currency code (EUR) | [default to undefined]
**translations** | [**Array&lt;JsonProductTranslation&gt;**](JsonProductTranslation.md) | Every translation of the product, the default one (en) required; a translation not given is removed | [default to undefined]

## Example

```typescript
import { EditedJsonProduct } from './api';

const instance: EditedJsonProduct = {
    category,
    price,
    currency,
    translations,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
