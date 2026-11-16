# ProductsApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**create**](#create) | **POST** /api/products | Create a product|
|[**getCatalogPage**](#getcatalogpage) | **GET** /api/catalog | Retrieve a page of the catalog, searched, filtered by category and sorted|
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

# **getCatalogPage**
> Array<JsonProduct> getCatalogPage()

The total number of products that match is in the header X-Total-Count; a size above 100 is brought back to 100. The name and the description are read, and searched, in the language of the customer.

### Example

```typescript
import {
    ProductsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new ProductsApi(configuration);

let acceptLanguage: string; //The language of the customer (en, fr); the products not translated into it are in en (optional) (default to undefined)
let page: number; //The number of the page, from 0 (a negative number is brought back to 0) (optional) (default to 0)
let size: number; //The number of items of a page (a size out of 1 to 100 is brought back to the nearest limit) (optional) (default to 20)
let search: string; //Words that each must be part of the name, the description or the category of the products, whatever the case (optional) (default to '')
let category: string; //Only the products of this category (CLOTHING, ELECTRONICS, ACCESSORIES...); all of them without it, none for a category that does not exist (optional) (default to '')
let sort: 'name' | 'priceAsc' | 'priceDesc'; //By name, or by the price to pay now, the lowest (priceAsc) or the highest (priceDesc) first; a sort that does not exist is the sort by name (optional) (default to 'name')

const { status, data } = await apiInstance.getCatalogPage(
    acceptLanguage,
    page,
    size,
    search,
    category,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **acceptLanguage** | [**string**] | The language of the customer (en, fr); the products not translated into it are in en | (optional) defaults to undefined|
| **page** | [**number**] | The number of the page, from 0 (a negative number is brought back to 0) | (optional) defaults to 0|
| **size** | [**number**] | The number of items of a page (a size out of 1 to 100 is brought back to the nearest limit) | (optional) defaults to 20|
| **search** | [**string**] | Words that each must be part of the name, the description or the category of the products, whatever the case | (optional) defaults to ''|
| **category** | [**string**] | Only the products of this category (CLOTHING, ELECTRONICS, ACCESSORIES...); all of them without it, none for a category that does not exist | (optional) defaults to ''|
| **sort** | [**&#39;name&#39; | &#39;priceAsc&#39; | &#39;priceDesc&#39;**]**Array<&#39;name&#39; &#124; &#39;priceAsc&#39; &#124; &#39;priceDesc&#39;>** | By name, or by the price to pay now, the lowest (priceAsc) or the highest (priceDesc) first; a sort that does not exist is the sort by name | (optional) defaults to 'name'|


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
|**200** | A page of the products of the catalog |  * X-Total-Count -  <br>  |

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

