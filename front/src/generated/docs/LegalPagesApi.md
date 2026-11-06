# LegalPagesApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getLegalPage**](#getlegalpage) | **GET** /api/legal-pages/{kind} | Retrieve the current version of a legal page|

# **getLegalPage**
> JsonLegalPage getLegalPage()


### Example

```typescript
import {
    LegalPagesApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new LegalPagesApi(configuration);

let kind: 'legal-notice' | 'terms-of-sale' | 'privacy-policy'; //The legal notice, the terms of sale or the privacy policy (default to undefined)

const { status, data } = await apiInstance.getLegalPage(
    kind
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **kind** | [**&#39;legal-notice&#39; | &#39;terms-of-sale&#39; | &#39;privacy-policy&#39;**]**Array<&#39;legal-notice&#39; &#124; &#39;terms-of-sale&#39; &#124; &#39;privacy-policy&#39;>** | The legal notice, the terms of sale or the privacy policy | defaults to undefined|


### Return type

**JsonLegalPage**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The current version of the page; its text is empty until the administrators write it |  -  |
|**404** | No such legal page |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

