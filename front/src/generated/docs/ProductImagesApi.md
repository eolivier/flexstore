# ProductImagesApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**addProductImage**](#addproductimage) | **PUT** /api/admin/products/{productId}/image | Give the image of a product, 2 MB at most (JPEG, PNG or WebP), kept resized (ADMIN role)|
|[**getProductImage**](#getproductimage) | **GET** /api/products/{productId}/image | Retrieve the image of a product, in the small (cards, cart) or large (page) format|
|[**getProductsWithImage**](#getproductswithimage) | **GET** /api/product-images | Retrieve the ids of the products that have an image|
|[**removeProductImage**](#removeproductimage) | **DELETE** /api/admin/products/{productId}/image | Remove the image of a product (ADMIN role)|

# **addProductImage**
> addProductImage()

The image is kept in two formats, as JPEG: LARGE (1600 px, 500 KB at most) for the page of the product, SMALL (400 px, 60 KB at most) for the cards of the catalog and the cart. It replaces the previous one 

### Example

```typescript
import {
    ProductImagesApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new ProductImagesApi(configuration);

let productId: string; // (default to undefined)
let image: File; // (default to undefined)

const { status, data } = await apiInstance.addProductImage(
    productId,
    image
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **productId** | [**string**] |  | defaults to undefined|
| **image** | [**File**] |  | defaults to undefined|


### Return type

void (empty response body)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**204** | The image is kept |  -  |
|**403** | The signed-in user is not an administrator |  -  |
|**404** | Unknown product |  -  |
|**413** | The image is larger than 2 MB |  -  |
|**415** | The file is not a JPEG, PNG or WebP image |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getProductImage**
> File getProductImage()

A JPEG, with an ETag; the browser asks again with If-None-Match and gets 304 when unchanged

### Example

```typescript
import {
    ProductImagesApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new ProductImagesApi(configuration);

let productId: string; // (default to undefined)
let format: 'small' | 'large'; // (optional) (default to 'large')

const { status, data } = await apiInstance.getProductImage(
    productId,
    format
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **productId** | [**string**] |  | defaults to undefined|
| **format** | [**&#39;small&#39; | &#39;large&#39;**]**Array<&#39;small&#39; &#124; &#39;large&#39;>** |  | (optional) defaults to 'large'|


### Return type

**File**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/jpeg


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The image |  -  |
|**304** | The image did not change |  -  |
|**404** | The product has no image |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getProductsWithImage**
> Array<string> getProductsWithImage()


### Example

```typescript
import {
    ProductImagesApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new ProductImagesApi(configuration);

const { status, data } = await apiInstance.getProductsWithImage();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<string>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The ids of the products with an image |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **removeProductImage**
> removeProductImage()


### Example

```typescript
import {
    ProductImagesApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new ProductImagesApi(configuration);

let productId: string; // (default to undefined)

const { status, data } = await apiInstance.removeProductImage(
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
|**204** | The image is removed |  -  |
|**404** | The product has no image |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

