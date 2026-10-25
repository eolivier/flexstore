# JsonPostalAddress

A postal address, its recipient (maybe someone else than the user) and where it is

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**firstName** | **string** |  | [default to undefined]
**lastName** | **string** |  | [default to undefined]
**companyName** | **string** | The company of the recipient (optional) | [optional] [default to undefined]
**street** | **string** |  | [default to undefined]
**complement** | **string** | Building, floor... (optional) | [optional] [default to undefined]
**postalCode** | **string** |  | [default to undefined]
**city** | **string** |  | [default to undefined]
**country** | **string** |  | [default to undefined]

## Example

```typescript
import { JsonPostalAddress } from './api';

const instance: JsonPostalAddress = {
    firstName,
    lastName,
    companyName,
    street,
    complement,
    postalCode,
    city,
    country,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
