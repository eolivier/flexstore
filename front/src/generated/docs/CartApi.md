# CartApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**addItem**](#additem) | **POST** /api/cart/save | Add an item to the cart, or change the quantity of an item (0 removes it)|
|[**getCartItems**](#getcartitems) | **GET** /api/cart/cart-items | Retrieve the items of the cart of the customer or of the visitor|
|[**mergeCarts**](#mergecarts) | **POST** /api/cart/merge | Merge the cart of the visitor (X-Cart-Id) into the cart of the signed-in customer|

# **addItem**
> JsonSaveItemResponse addItem(jsonItem)

The cart of the signed-in customer (bearer token), otherwise the cart of the visitor given by X-Cart-Id; without X-Cart-Id, a visitor cart is created and its id is returned.

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
let xCartId: string; //Id of the visitor cart (a UUID), returned by POST /api/cart/save (optional) (default to undefined)

const { status, data } = await apiInstance.addItem(
    jsonItem,
    xCartId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonItem** | **JsonItem**|  | |
| **xCartId** | [**string**] | Id of the visitor cart (a UUID), returned by POST /api/cart/save | (optional) defaults to undefined|


### Return type

**JsonSaveItemResponse**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Saved |  -  |
|**404** | Unknown product |  -  |
|**422** | A new item needs a positive quantity |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getCartItems**
> JsonCartItems getCartItems()

The cart of the signed-in customer (bearer token), otherwise the cart of the visitor given by X-Cart-Id (empty without it).

### Example

```typescript
import {
    CartApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new CartApi(configuration);

let xCartId: string; //Id of the visitor cart (a UUID), returned by POST /api/cart/save (optional) (default to undefined)

const { status, data } = await apiInstance.getCartItems(
    xCartId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **xCartId** | [**string**] | Id of the visitor cart (a UUID), returned by POST /api/cart/save | (optional) defaults to undefined|


### Return type

**JsonCartItems**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **mergeCarts**
> mergeCarts()

All the items are kept; for a product in both carts, the larger quantity is kept. The visitor cart is then deleted.

### Example

```typescript
import {
    CartApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new CartApi(configuration);

let xCartId: string; //Id of the visitor cart (a UUID), returned by POST /api/cart/save (optional) (default to undefined)

const { status, data } = await apiInstance.mergeCarts(
    xCartId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **xCartId** | [**string**] | Id of the visitor cart (a UUID), returned by POST /api/cart/save | (optional) defaults to undefined|


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
|**204** | Merged |  -  |
|**401** | Not signed in |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

