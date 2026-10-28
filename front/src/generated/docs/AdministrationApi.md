# AdministrationApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**advanceOrderStatus**](#advanceorderstatus) | **POST** /api/admin/orders/{id}/status/advance | Move an order to the next status of its life cycle, PROCESSING then SHIPPED then DELIVERED (ADMIN role)|
|[**getAdminProduct**](#getadminproduct) | **GET** /api/admin/products/{id} | Retrieve a product with its translations in every language, to edit it|
|[**getAllOrders**](#getallorders) | **GET** /api/admin/orders | Retrieve the orders of all the customers, newest first, with their customer (ADMIN role)|
|[**getAnyOrder**](#getanyorder) | **GET** /api/admin/orders/{id} | Retrieve any order, with its customer (ADMIN role)|
|[**proposeSku**](#proposesku) | **GET** /api/admin/products/sku-proposal | Propose a SKU free in the catalog, from the category and the name of a product (ADMIN role)|

# **advanceOrderStatus**
> JsonAdminOrder advanceOrderStatus(jsonOrderStatusAdvance)


### Example

```typescript
import {
    AdministrationApi,
    Configuration,
    JsonOrderStatusAdvance
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

let id: string; // (default to undefined)
let jsonOrderStatusAdvance: JsonOrderStatusAdvance; //

const { status, data } = await apiInstance.advanceOrderStatus(
    id,
    jsonOrderStatusAdvance
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonOrderStatusAdvance** | **JsonOrderStatusAdvance**|  | |
| **id** | [**string**] |  | defaults to undefined|


### Return type

**JsonAdminOrder**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The order at its next status |  -  |
|**403** | The signed-in user is not an administrator |  -  |
|**404** | No order with this id |  -  |
|**409** | The order no longer has the given status, or it is delivered (no next status) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAdminProduct**
> JsonAdminProduct getAdminProduct()


### Example

```typescript
import {
    AdministrationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.getAdminProduct(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**JsonAdminProduct**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The product with its translations |  -  |
|**404** | Product not found |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAllOrders**
> Array<JsonAdminOrder> getAllOrders()


### Example

```typescript
import {
    AdministrationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

const { status, data } = await apiInstance.getAllOrders();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<JsonAdminOrder>**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The orders of all the customers |  -  |
|**403** | The signed-in user is not an administrator |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAnyOrder**
> JsonAdminOrder getAnyOrder()


### Example

```typescript
import {
    AdministrationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.getAnyOrder(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**JsonAdminOrder**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The order and its customer |  -  |
|**403** | The signed-in user is not an administrator |  -  |
|**404** | No order with this id |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **proposeSku**
> JsonSkuProposal proposeSku()

The first three letters of the category, the first two words of the name (eight characters at most each, without accents), then the first number from 0001 that no product has: CLO-ORGANIC-COTTON-0001

### Example

```typescript
import {
    AdministrationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

let category: string; // (default to undefined)
let name: string; //The name of the product in the default language (en), maybe not entered yet (optional) (default to undefined)

const { status, data } = await apiInstance.proposeSku(
    category,
    name
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **category** | [**string**] |  | defaults to undefined|
| **name** | [**string**] | The name of the product in the default language (en), maybe not entered yet | (optional) defaults to undefined|


### Return type

**JsonSkuProposal**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The SKU proposed |  -  |
|**403** | The signed-in user is not an administrator |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

