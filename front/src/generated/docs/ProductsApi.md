# ProductsApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**create**](#create) | **POST** /api/products | Create a product|
|[**getProducts**](#getproducts) | **GET** /api/products/ | Retrieve all products|

# **create**
> JsonProduct create(draftJsonProduct)


### Example

```typescript
import {
    ProductsApi,
    Configuration,
    DraftJsonProduct
} from './api';

const configuration = new Configuration();
const apiInstance = new ProductsApi(configuration);

let draftJsonProduct: DraftJsonProduct; //

const { status, data } = await apiInstance.create(
    draftJsonProduct
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **draftJsonProduct** | **DraftJsonProduct**|  | |


### Return type

**JsonProduct**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | Product created |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getProducts**
> Array<JsonProduct> getProducts()

Returns the full list of available products

### Example

```typescript
import {
    ProductsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new ProductsApi(configuration);

const { status, data } = await apiInstance.getProducts();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<JsonProduct>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Successfully retrieved the list of products |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

