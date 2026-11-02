# JsonSeller


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **string** |  | [default to undefined]
**address** | **string** | On one line | [default to undefined]
**siret** | **string** | 14 digits, spaces allowed | [default to undefined]
**vatNumber** | **string** | Intra-community VAT number, the country then 2 to 13 digits or letters | [default to undefined]
**phone** | **string** | Digits, maybe after a +, grouped by spaces, dots, dashes or parentheses; empty in the versions written before it was asked | [default to undefined]
**email** | **string** |  | [default to undefined]
**website** | **string** | With or without its scheme | [default to undefined]
**legalForm** | **string** | SAS, SARL, EURL...; empty in the versions written before it was asked | [default to undefined]
**shareCapital** | **string** | In euros, digits maybe grouped by spaces; empty in the versions written before it was asked | [default to undefined]
**registryCity** | **string** | The city of the trade and companies register (RCS); empty in the versions written before it was asked | [default to undefined]

## Example

```typescript
import { JsonSeller } from './api';

const instance: JsonSeller = {
    name,
    address,
    siret,
    vatNumber,
    phone,
    email,
    website,
    legalForm,
    shareCapital,
    registryCity,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
