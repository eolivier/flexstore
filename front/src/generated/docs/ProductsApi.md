# ProductsApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**create**](#create) | **POST** /api/products | Create a product|
|[**getProduct**](#getproduct) | **GET** /api/products/{id} | Retrieve a product by ID|
|[**getProducts**](#getproducts) | **GET** /api/products/ | Retrieve all products|
|[**updateProduct**](#updateproduct) | **PUT** /api/products/{id} | Edit a product (SKU, translations, category, price)|

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

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | Product created |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getProduct**
> JsonProduct getProduct()


### Example

```typescript
import {
    ProductsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new ProductsApi(configuration);

let id: string; // (default to undefined)
let acceptLanguage: string; //The language of the customer (en, fr); the products not translated into it are in en (optional) (default to undefined)

const { status, data } = await apiInstance.getProduct(
    id,
    acceptLanguage
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|
| **acceptLanguage** | [**string**] | The language of the customer (en, fr); the products not translated into it are in en | (optional) defaults to undefined|


### Return type

**JsonProduct**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Successfully retrieved the product |  -  |
|**404** | Product not found |  -  |

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

let acceptLanguage: string; //The language of the customer (en, fr); the products not translated into it are in en (optional) (default to undefined)

const { status, data } = await apiInstance.getProducts(
    acceptLanguage
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **acceptLanguage** | [**string**] | The language of the customer (en, fr); the products not translated into it are in en | (optional) defaults to undefined|


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

# **updateProduct**
> JsonProduct updateProduct(editedJsonProduct)

Reserved to an administrator

### Example

```typescript
import {
    ProductsApi,
    Configuration,
    EditedJsonProduct
} from './api';

const configuration = new Configuration();
const apiInstance = new ProductsApi(configuration);

let id: string; // (default to undefined)
let editedJsonProduct: EditedJsonProduct; //

const { status, data } = await apiInstance.updateProduct(
    id,
    editedJsonProduct
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **editedJsonProduct** | **EditedJsonProduct**|  | |
| **id** | [**string**] |  | defaults to undefined|


### Return type

**JsonProduct**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The edited product |  -  |
|**400** | Malformed SKU |  -  |
|**401** | Not signed in |  -  |
|**403** | Not an administrator |  -  |
|**404** | Unknown product |  -  |
|**409** | SKU used by another product |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

