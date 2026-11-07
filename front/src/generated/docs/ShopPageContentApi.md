# ShopPageContentApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getShopPageContents**](#getshoppagecontents) | **GET** /api/shop-page-contents | Retrieve the contents of the shop pages (key words, description, Google Analytics id), public|

# **getShopPageContents**
> JsonShopPageContents getShopPageContents()


### Example

```typescript
import {
    ShopPageContentApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new ShopPageContentApi(configuration);

const { status, data } = await apiInstance.getShopPageContents();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**JsonShopPageContents**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | One content for each type; its text is empty until the administrators write it |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

