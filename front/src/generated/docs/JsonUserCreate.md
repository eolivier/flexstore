# JsonUserCreate


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**firstName** | **string** |  | [default to undefined]
**lastName** | **string** |  | [default to undefined]
**email** | **string** |  | [default to undefined]
**password** | **string** | User\&#39;s password (will be hashed by the server) | [default to undefined]
**role** | **string** | Role of the new user (USER when absent) | [optional] [default to undefined]

## Example

```typescript
import { JsonUserCreate } from './api';

const instance: JsonUserCreate = {
    firstName,
    lastName,
    email,
    password,
    role,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
