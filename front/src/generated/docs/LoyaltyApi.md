# LoyaltyApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getLoyaltyStatement**](#getloyaltystatement) | **GET** /api/loyalty | Retrieve the loyalty statement of the signed-in customer, a line for each paid order with its points (one for each whole euro or dollar), and the total|

# **getLoyaltyStatement**
> JsonLoyaltyStatement getLoyaltyStatement()


### Example

```typescript
import {
    LoyaltyApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new LoyaltyApi(configuration);

const { status, data } = await apiInstance.getLoyaltyStatement();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**JsonLoyaltyStatement**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The loyalty statement of the customer |  -  |
|**401** | Not signed in |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

