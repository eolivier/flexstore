# SellerApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getSocialNetworks**](#getsocialnetworks) | **GET** /api/social-networks | Retrieve the accounts of the shop on the social networks; empty where it has none (public)|

# **getSocialNetworks**
> JsonSocialNetworks getSocialNetworks()


### Example

```typescript
import {
    SellerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new SellerApi(configuration);

const { status, data } = await apiInstance.getSocialNetworks();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**JsonSocialNetworks**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The accounts of the shop on the social networks |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

