# JsonTwoFactorChallengeVerification


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**challengeId** | **string** | The challengeId returned by POST /api/users/login when status is TWO_FACTOR_REQUIRED | [default to undefined]
**code** | **string** | The 6-digit code of the authenticator app | [default to undefined]

## Example

```typescript
import { JsonTwoFactorChallengeVerification } from './api';

const instance: JsonTwoFactorChallengeVerification = {
    challengeId,
    code,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
