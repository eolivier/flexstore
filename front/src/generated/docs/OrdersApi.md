# OrdersApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getOrder**](#getorder) | **GET** /api/orders/{id} | Retrieve an order of the signed-in customer|
|[**getOrders**](#getorders) | **GET** /api/orders | Retrieve the orders of the signed-in customer, newest first|
|[**placeOrder**](#placeorder) | **POST** /api/orders | Place an order from the cart of the signed-in customer; it awaits its payment|

# **getOrder**
> JsonOrder getOrder()


### Example

```typescript
import {
    OrdersApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new OrdersApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.getOrder(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**JsonOrder**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The order |  -  |
|**404** | No order with this id for the signed-in customer |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getOrders**
> Array<JsonOrder> getOrders()


### Example

```typescript
import {
    OrdersApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new OrdersApi(configuration);

const { status, data } = await apiInstance.getOrders();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<JsonOrder>**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The orders |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **placeOrder**
> JsonOrder placeOrder(jsonOrderAddresses)

The order is delivered and billed to the given addresses (the delivery address when no billing address is given), which become addresses of the customer when they are new. The cart is kept until the order is paid (POST /api/orders/{id}/payment) 

### Example

```typescript
import {
    OrdersApi,
    Configuration,
    JsonOrderAddresses
} from './api';

const configuration = new Configuration();
const apiInstance = new OrdersApi(configuration);

let jsonOrderAddresses: JsonOrderAddresses; //

const { status, data } = await apiInstance.placeOrder(
    jsonOrderAddresses
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonOrderAddresses** | **JsonOrderAddresses**|  | |


### Return type

**JsonOrder**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | Order placed |  -  |
|**401** | Not signed in |  -  |
|**400** | Incomplete delivery or billing address |  -  |
|**409** | Not enough units in stock for a product of the cart |  -  |
|**403** | The account is SUSPENDED, LOCKED or DISABLED: { error, status } |  -  |
|**422** | The cart is empty |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

