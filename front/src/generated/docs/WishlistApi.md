# WishlistApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getWishlist**](#getwishlist) | **GET** /api/wishlist | Retrieve the products the signed-in customer saved to their wishlist, newest first|
|[**removeProductFromWishlist**](#removeproductfromwishlist) | **DELETE** /api/wishlist/{productId} | Remove a product from the wishlist of the signed-in customer|
|[**saveProductToWishlist**](#saveproducttowishlist) | **PUT** /api/wishlist/{productId} | Save a product to the wishlist of the signed-in customer, once per product|

# **getWishlist**
> Array<JsonProduct> getWishlist()


### Example

```typescript
import {
    WishlistApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new WishlistApi(configuration);

let acceptLanguage: string; //The language of the customer (en, fr); the products not translated into it are in en (optional) (default to undefined)

const { status, data } = await apiInstance.getWishlist(
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

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The products saved to the wishlist |  -  |
|**401** | Not signed in |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **removeProductFromWishlist**
> removeProductFromWishlist()


### Example

```typescript
import {
    WishlistApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new WishlistApi(configuration);

let productId: string; // (default to undefined)

const { status, data } = await apiInstance.removeProductFromWishlist(
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
|**204** | Removed (or already absent) |  -  |
|**401** | Not signed in |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **saveProductToWishlist**
> saveProductToWishlist()


### Example

```typescript
import {
    WishlistApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new WishlistApi(configuration);

let productId: string; // (default to undefined)

const { status, data } = await apiInstance.saveProductToWishlist(
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
|**204** | Saved (already there, or saved now) |  -  |
|**401** | Not signed in |  -  |
|**404** | Unknown product |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

