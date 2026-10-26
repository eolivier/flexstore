# JsonOrder


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [default to undefined]
**placedAt** | **string** |  | [default to undefined]
**status** | **string** |  | [default to undefined]
**lines** | [**Array&lt;JsonOrderLine&gt;**](JsonOrderLine.md) |  | [default to undefined]
**totalPrice** | **number** |  | [default to undefined]
**currency** | **string** |  | [default to undefined]
**deliveryAddress** | [**JsonPostalAddress**](JsonPostalAddress.md) |  | [default to undefined]
**billingAddress** | [**JsonPostalAddress**](JsonPostalAddress.md) |  | [default to undefined]
**paymentDeadline** | **string** | When the order must be paid by, or it is cancelled. Only for the customer, while the order awaits its payment | [optional] [default to undefined]

## Example

```typescript
import { JsonOrder } from './api';

const instance: JsonOrder = {
    id,
    placedAt,
    status,
    lines,
    totalPrice,
    currency,
    deliveryAddress,
    billingAddress,
    paymentDeadline,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
