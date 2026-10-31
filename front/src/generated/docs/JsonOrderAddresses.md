# JsonOrderAddresses


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**deliveryAddress** | [**JsonPostalAddress**](JsonPostalAddress.md) |  | [default to undefined]
**billingAddress** | [**JsonPostalAddress**](JsonPostalAddress.md) |  | [optional] [default to undefined]
**shippingMethod** | **string** | STANDARD when not given | [optional] [default to undefined]
**discountCode** | **string** | The discount code entered, if any (404 when unknown, 410 when expired or used up) | [optional] [default to undefined]

## Example

```typescript
import { JsonOrderAddresses } from './api';

const instance: JsonOrderAddresses = {
    deliveryAddress,
    billingAddress,
    shippingMethod,
    discountCode,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
