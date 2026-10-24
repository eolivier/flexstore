# JsonLoginResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**token** | **string** | JWT to send in the Authorization header: Bearer &lt;token&gt; | [default to undefined]
**expiresAt** | **string** |  | [default to undefined]
**user** | [**JsonUser**](JsonUser.md) |  | [default to undefined]

## Example

```typescript
import { JsonLoginResponse } from './api';

const instance: JsonLoginResponse = {
    token,
    expiresAt,
    user,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
