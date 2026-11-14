# RestockSubscriptionsApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getRestockSubscribedProductIds**](#getrestocksubscribedproductids) | **GET** /api/restock-subscriptions | Retrieve the ids of the products the signed-in customer waits for|
|[**subscribeToRestock**](#subscribetorestock) | **PUT** /api/restock-subscriptions/{productId} | Ask to be notified by email when a product out of stock is back, once per product|

# **getRestockSubscribedProductIds**
> Array<string> getRestockSubscribedProductIds()


### Example

```typescript
import {
    RestockSubscriptionsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new RestockSubscriptionsApi(configuration);

const { status, data } = await apiInstance.getRestockSubscribedProductIds();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<string>**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The ids of the products the customer will be notified of by email |  -  |
|**401** | Not signed in |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **subscribeToRestock**
> subscribeToRestock()


### Example

```typescript
import {
    RestockSubscriptionsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new RestockSubscriptionsApi(configuration);

let productId: string; // (default to undefined)

const { status, data } = await apiInstance.subscribeToRestock(
    productId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **productId** | [**string**] |  | defaults to undefined|


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
|**204** | Subscribed (already, or now); the customer is notified once the product is back in stock |  -  |
|**401** | Not signed in |  -  |
|**404** | Unknown product |  -  |
|**409** | The product is in stock, there is nothing to wait for |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

