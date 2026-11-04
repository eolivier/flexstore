# JsonRefreshResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**token** | **string** | JWT to send in the Authorization header: Bearer &lt;token&gt; | [default to undefined]
**expiresAt** | **string** |  | [default to undefined]
**refreshToken** | **string** | The refresh token that replaces the one presented | [default to undefined]
**refreshExpiresAt** | **string** |  | [default to undefined]

## Example

```typescript
import { JsonRefreshResponse } from './api';

const instance: JsonRefreshResponse = {
    token,
    expiresAt,
    refreshToken,
    refreshExpiresAt,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
