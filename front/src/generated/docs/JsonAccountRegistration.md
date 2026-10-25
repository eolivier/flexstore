# JsonAccountRegistration


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**firstName** | **string** |  | [default to undefined]
**lastName** | **string** |  | [default to undefined]
**email** | **string** |  | [default to undefined]
**password** | **string** | At least 8 characters (will be hashed by the server) | [default to undefined]
**phoneNumber** | **string** |  | [default to undefined]
**postalAddress** | [**JsonPostalAddress**](JsonPostalAddress.md) |  | [default to undefined]

## Example

```typescript
import { JsonAccountRegistration } from './api';

const instance: JsonAccountRegistration = {
    firstName,
    lastName,
    email,
    password,
    phoneNumber,
    postalAddress,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
