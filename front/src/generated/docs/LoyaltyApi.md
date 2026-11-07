# LoyaltyApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getLoyaltyPoints**](#getloyaltypoints) | **GET** /api/loyalty | Retrieve the loyalty points of the signed-in customer, one for each whole euro or dollar of their paid orders|

# **getLoyaltyPoints**
> JsonLoyaltyPoints getLoyaltyPoints()


### Example

```typescript
import {
    LoyaltyApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new LoyaltyApi(configuration);

const { status, data } = await apiInstance.getLoyaltyPoints();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**JsonLoyaltyPoints**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The loyalty points of the customer |  -  |
|**401** | Not signed in |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

