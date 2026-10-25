# JsonPostalAddress

A postal address: its recipient, maybe someone else than the user, and where it is. The recipient is a person (PRIVATE_INDIVIDUAL, with firstName and lastName) or a company (LEGAL_ENTITY, with companyName and an optional vatNumber) 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**recipientType** | **string** |  | [default to undefined]
**firstName** | **string** |  | [optional] [default to undefined]
**lastName** | **string** |  | [optional] [default to undefined]
**companyName** | **string** |  | [optional] [default to undefined]
**vatNumber** | **string** | The VAT identification number of the company (optional) | [optional] [default to undefined]
**street** | **string** |  | [default to undefined]
**complement** | **string** | Building, floor... (optional) | [optional] [default to undefined]
**postalCode** | **string** |  | [default to undefined]
**city** | **string** |  | [default to undefined]
**country** | **string** |  | [default to undefined]

## Example

```typescript
import { JsonPostalAddress } from './api';

const instance: JsonPostalAddress = {
    recipientType,
    firstName,
    lastName,
    companyName,
    vatNumber,
    street,
    complement,
    postalCode,
    city,
    country,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
