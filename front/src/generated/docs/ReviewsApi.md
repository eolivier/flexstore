# ReviewsApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**addReview**](#addreview) | **POST** /api/products/{id}/reviews | Review a product bought by the signed-in customer, once per product|
|[**getRatings**](#getratings) | **GET** /api/product-ratings | Retrieve what the customers think of each product that has reviews|
|[**getReviews**](#getreviews) | **GET** /api/products/{id}/reviews | Retrieve the reviews of a product, newest first|

# **addReview**
> JsonReview addReview(jsonNewReview)


### Example

```typescript
import {
    ReviewsApi,
    Configuration,
    JsonNewReview
} from './api';

const configuration = new Configuration();
const apiInstance = new ReviewsApi(configuration);

let id: string; // (default to undefined)
let jsonNewReview: JsonNewReview; //

const { status, data } = await apiInstance.addReview(
    id,
    jsonNewReview
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonNewReview** | **JsonNewReview**|  | |
| **id** | [**string**] |  | defaults to undefined|


### Return type

**JsonReview**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | The review written |  -  |
|**400** | Invalid rating or comment |  -  |
|**403** | The customer did not buy (and pay for) the product |  -  |
|**404** | Product not found |  -  |
|**409** | The customer already reviewed the product |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getRatings**
> Array<JsonRatingSummary> getRatings()


### Example

```typescript
import {
    ReviewsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new ReviewsApi(configuration);

const { status, data } = await apiInstance.getRatings();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<JsonRatingSummary>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The average rating and the number of reviews of each reviewed product |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getReviews**
> Array<JsonReview> getReviews()


### Example

```typescript
import {
    ReviewsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new ReviewsApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.getReviews(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**Array<JsonReview>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The reviews of the product |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

