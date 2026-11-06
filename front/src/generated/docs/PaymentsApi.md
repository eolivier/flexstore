# PaymentsApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**payOrder**](#payorder) | **POST** /api/orders/{id}/payment | Start the payment of an order of the signed-in customer, then open its checkout page|
|[**stripeNotification**](#stripenotification) | **POST** /api/payments/stripe/webhook | Notification of Stripe: a checkout session is completed (called by Stripe, not by the front)|
|[**verifyOrderPayment**](#verifyorderpayment) | **POST** /api/orders/{id}/payment/verification | Ask the payment provider whether an order of the signed-in customer was paid|

# **payOrder**
> JsonPaymentSession payOrder()

With Stripe, the customer pays on the checkout page of Stripe, which confirms the payment later with its webhook (outcome PENDING). Without a Stripe key, the payment is simulated and confirmed at once (outcome PAID). Once paid, the cart is emptied and the order confirmed to the customer by email 

### Example

```typescript
import {
    PaymentsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new PaymentsApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.payOrder(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**JsonPaymentSession**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | The payment is started |  -  |
|**404** | No order with this id for the signed-in customer |  -  |
|**409** | The order is already paid |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **stripeNotification**
> stripeNotification(body)

Authenticated by its Stripe-Signature header, verified with the secret of the webhook

### Example

```typescript
import {
    PaymentsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new PaymentsApi(configuration);

let stripeSignature: string; // (default to undefined)
let body: object; //

const { status, data } = await apiInstance.stripeNotification(
    stripeSignature,
    body
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **body** | **object**|  | |
| **stripeSignature** | [**string**] |  | defaults to undefined|


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Notification handled |  -  |
|**400** | Invalid signature |  -  |
|**404** | No Stripe key configured (simulated payments) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **verifyOrderPayment**
> verifyOrderPayment()

Called when the customer comes back from the checkout page: if the provider says the order was paid, it is paid here too (cart emptied, invoice issued, confirmation email), even when the notification of the provider did not arrive. Nothing changes when the order was not paid 

### Example

```typescript
import {
    PaymentsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new PaymentsApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.verifyOrderPayment(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

void (empty response body)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**204** | The payment was verified |  -  |
|**404** | No order with this id for the signed-in customer |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

