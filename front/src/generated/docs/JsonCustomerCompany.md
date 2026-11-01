# JsonCustomerCompany

The company a customer buys for (none for a customer who buys for themselves); the address given at the creation of the account is sent to it, for the attention of the customer

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**companyName** | **string** |  | [default to undefined]
**siren** | **string** | 9 digits (spaces dropped) | [default to undefined]
**vatNumber** | **string** | Optional intra-community VAT number (spaces dropped, upper case); a French one is FR, a key of 2 characters, then the SIREN | [optional] [default to undefined]

## Example

```typescript
import { JsonCustomerCompany } from './api';

const instance: JsonCustomerCompany = {
    companyName,
    siren,
    vatNumber,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
