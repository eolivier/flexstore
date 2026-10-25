# JsonUserUpdate

The fields an administrator can edit; the password is changed by a separate operation

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**firstName** | **string** |  | [default to undefined]
**lastName** | **string** |  | [default to undefined]
**email** | **string** |  | [default to undefined]
**role** | **string** | Role of the user (USER when absent) | [optional] [default to undefined]

## Example

```typescript
import { JsonUserUpdate } from './api';

const instance: JsonUserUpdate = {
    firstName,
    lastName,
    email,
    role,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
