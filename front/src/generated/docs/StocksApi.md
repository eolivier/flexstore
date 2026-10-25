# StocksApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**changeStock**](#changestock) | **PUT** /api/admin/stocks/{productId} | Give the number of units of a product that the shop has (ADMIN role)|
|[**getStock**](#getstock) | **GET** /api/stocks/{productId} | Retrieve the stock of a product (0 when it was never given)|
|[**getStocks**](#getstocks) | **GET** /api/stocks | Retrieve the stocks of the products (a product without stock has none)|

# **changeStock**
> JsonStock changeStock(jsonStockQuantity)


### Example

```typescript
import {
    StocksApi,
    Configuration,
    JsonStockQuantity
} from './api';

const configuration = new Configuration();
const apiInstance = new StocksApi(configuration);

let productId: string; // (default to undefined)
let jsonStockQuantity: JsonStockQuantity; //

const { status, data } = await apiInstance.changeStock(
    productId,
    jsonStockQuantity
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonStockQuantity** | **JsonStockQuantity**|  | |
| **productId** | [**string**] |  | defaults to undefined|


### Return type

**JsonStock**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The new stock of the product |  -  |
|**400** | Negative stock |  -  |
|**403** | The signed-in user is not an administrator |  -  |
|**404** | Unknown product |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getStock**
> JsonStock getStock()


### Example

```typescript
import {
    StocksApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new StocksApi(configuration);

let productId: string; // (default to undefined)

const { status, data } = await apiInstance.getStock(
    productId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **productId** | [**string**] |  | defaults to undefined|


### Return type

**JsonStock**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The stock of the product |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getStocks**
> Array<JsonStock> getStocks()


### Example

```typescript
import {
    StocksApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new StocksApi(configuration);

const { status, data } = await apiInstance.getStocks();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<JsonStock>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The stocks |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

