# JsonLoginResponse

SIGNED_IN: token, expiresAt and user are filled. TWO_FACTOR_REQUIRED: the password was right, but the code of the authenticator app is still needed; only challengeId is filled. POST it with the code to /api/users/login/two-factor-authentication. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**status** | **string** |  | [default to undefined]
**token** | **string** | JWT to send in the Authorization header: Bearer &lt;token&gt; | [optional] [default to undefined]
**expiresAt** | **string** |  | [optional] [default to undefined]
**refreshToken** | **string** | Opaque secret that renews the token once it expired (POST /api/users/refresh); it works once | [optional] [default to undefined]
**refreshExpiresAt** | **string** |  | [optional] [default to undefined]
**user** | [**JsonUser**](JsonUser.md) |  | [optional] [default to undefined]
**challengeId** | **string** | Present when status is TWO_FACTOR_REQUIRED | [optional] [default to undefined]

## Example

```typescript
import { JsonLoginResponse } from './api';

const instance: JsonLoginResponse = {
    status,
    token,
    expiresAt,
    refreshToken,
    refreshExpiresAt,
    user,
    challengeId,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
