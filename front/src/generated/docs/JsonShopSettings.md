# JsonShopSettings


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**orderConfirmationEmail** | **string** | The email sent to the customer when an order is placed | [default to undefined]
**paymentDelayMinutes** | **number** | The time to pay an order once placed, in minutes | [default to undefined]
**freeShippingThreshold** | **number** | The price of the items, in euros, from which the standard shipping is free (0 - always free) | [default to undefined]
**sellerSetup** | **string** | ENABLED once the setup assistant of the seller is finished (read only) | [default to undefined]
**abandonedCartReminder** | **string** | The email that reminds a customer of the items left in their cart | [default to undefined]
**abandonedCartDelayHours** | **number** | The time a cart is left before its customer is reminded, in hours | [default to undefined]

## Example

```typescript
import { JsonShopSettings } from './api';

const instance: JsonShopSettings = {
    orderConfirmationEmail,
    paymentDelayMinutes,
    freeShippingThreshold,
    sellerSetup,
    abandonedCartReminder,
    abandonedCartDelayHours,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
