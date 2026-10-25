# JsonPaymentSession


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**checkoutUrl** | **string** | The page where the customer pays the order | [default to undefined]
**outcome** | **string** | PAID when the payment is already confirmed (simulated payment) | [default to undefined]

## Example

```typescript
import { JsonPaymentSession } from './api';

const instance: JsonPaymentSession = {
    checkoutUrl,
    outcome,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
