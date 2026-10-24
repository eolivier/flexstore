# JsonChangePasswordRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**currentPassword** | **string** |  | [default to undefined]
**newPassword** | **string** | New password, at least 8 characters (will be hashed by the server) | [default to undefined]

## Example

```typescript
import { JsonChangePasswordRequest } from './api';

const instance: JsonChangePasswordRequest = {
    currentPassword,
    newPassword,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
