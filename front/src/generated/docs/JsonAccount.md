# JsonAccount


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [default to undefined]
**firstName** | **string** |  | [default to undefined]
**lastName** | **string** |  | [default to undefined]
**email** | **string** |  | [default to undefined]
**role** | **string** |  | [default to undefined]
**status** | **string** |  | [default to undefined]
**phoneNumber** | **string** |  | [optional] [default to undefined]
**postalAddresses** | [**Array&lt;JsonPostalAddress&gt;**](JsonPostalAddress.md) |  | [default to undefined]

## Example

```typescript
import { JsonAccount } from './api';

const instance: JsonAccount = {
    id,
    firstName,
    lastName,
    email,
    role,
    status,
    phoneNumber,
    postalAddresses,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
