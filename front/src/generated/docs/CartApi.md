# CartApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**addItem**](#additem) | **POST** /api/cart/save | Add an item to the cart|
|[**getCartItems**](#getcartitems) | **GET** /api/cart/cart-items | Retrieve all cart items|

# **addItem**
> object addItem(jsonItem)


### Example

```typescript
import {
    CartApi,
    Configuration,
    JsonItem
} from './api';

const configuration = new Configuration();
const apiInstance = new CartApi(configuration);

let jsonItem: JsonItem; //

const { status, data } = await apiInstance.addItem(
    jsonItem
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonItem** | **JsonItem**|  | |


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | Created |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getCartItems**
> JsonCartItems getCartItems()

Returns the full list of items in the cart

### Example

```typescript
import {
    CartApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new CartApi(configuration);

const { status, data } = await apiInstance.getCartItems();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**JsonCartItems**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

