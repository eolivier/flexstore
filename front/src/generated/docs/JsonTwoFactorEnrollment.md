# JsonTwoFactorEnrollment

What an administrator reads once an enrollment started, to set up their authenticator app

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**secret** | **string** | The secret, to type manually in the authenticator app | [default to undefined]
**provisioningUri** | **string** | An otpauth:// link, to render as a QR code | [default to undefined]

## Example

```typescript
import { JsonTwoFactorEnrollment } from './api';

const instance: JsonTwoFactorEnrollment = {
    secret,
    provisioningUri,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
