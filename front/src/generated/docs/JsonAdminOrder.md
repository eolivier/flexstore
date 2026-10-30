# JsonAdminOrder

An order as the administrators see it, with the customer who placed it

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [default to undefined]
**placedAt** | **string** |  | [default to undefined]
**status** | **string** |  | [default to undefined]
**lines** | [**Array&lt;JsonOrderLine&gt;**](JsonOrderLine.md) |  | [default to undefined]
**itemsPrice** | **number** | The price of the lines, without the shipping | [default to undefined]
**shippingMethod** | **string** |  | [default to undefined]
**shippingCost** | **number** | 0 when the shipping is free | [default to undefined]
**totalPrice** | **number** | The price of the lines plus the shipping | [default to undefined]
**currency** | **string** |  | [default to undefined]
**deliveryAddress** | [**JsonPostalAddress**](JsonPostalAddress.md) |  | [default to undefined]
**billingAddress** | [**JsonPostalAddress**](JsonPostalAddress.md) |  | [default to undefined]
**paymentDeadline** | **string** | When the order must be paid by, or it is cancelled. Only for the customer, while the order awaits its payment | [optional] [default to undefined]
**customer** | [**JsonOrderCustomer**](JsonOrderCustomer.md) |  | [default to undefined]
**statusHistory** | [**Array&lt;JsonOrderStatusChange&gt;**](JsonOrderStatusChange.md) | The statuses the order went through, oldest first (the first one, when it was placed) | [default to undefined]

## Example

```typescript
import { JsonAdminOrder } from './api';

const instance: JsonAdminOrder = {
    id,
    placedAt,
    status,
    lines,
    itemsPrice,
    shippingMethod,
    shippingCost,
    totalPrice,
    currency,
    deliveryAddress,
    billingAddress,
    paymentDeadline,
    customer,
    statusHistory,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
