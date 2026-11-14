# JsonOrderAddresses


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**deliveryAddress** | [**JsonPostalAddress**](JsonPostalAddress.md) |  | [default to undefined]
**billingAddress** | [**JsonPostalAddress**](JsonPostalAddress.md) |  | [optional] [default to undefined]
**shippingMethod** | **string** | The id of the shipping method chosen, STANDARD when not given (409 when it is disabled) | [optional] [default to undefined]
**discountCode** | **string** | The discount code entered, if any (404 when unknown, 410 when expired or used up) | [optional] [default to undefined]
**termsOfSaleId** | **string** | The id of the version of the terms of sale the customer read and accepted | [default to undefined]

## Example

```typescript
import { JsonOrderAddresses } from './api';

const instance: JsonOrderAddresses = {
    deliveryAddress,
    billingAddress,
    shippingMethod,
    discountCode,
    termsOfSaleId,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
