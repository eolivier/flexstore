# CookiesApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getCookieConsent**](#getcookieconsent) | **GET** /api/cookie-consent | Retrieve the cookie consent of the signed-in customer|
|[**recordCookieConsent**](#recordcookieconsent) | **PUT** /api/cookie-consent | Record the choice of the signed-in customer about the preference cookies, for 6 months|

# **getCookieConsent**
> JsonCookieConsent getCookieConsent()

UNDECIDED, without expiry, when the customer never chose or when their choice is 6 months old.

### Example

```typescript
import {
    CookiesApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new CookiesApi(configuration);

const { status, data } = await apiInstance.getCookieConsent();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**JsonCookieConsent**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The cookie consent |  -  |
|**401** | Not signed in |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **recordCookieConsent**
> JsonCookieConsent recordCookieConsent(jsonCookieChoice)


### Example

```typescript
import {
    CookiesApi,
    Configuration,
    JsonCookieChoice
} from './api';

const configuration = new Configuration();
const apiInstance = new CookiesApi(configuration);

let jsonCookieChoice: JsonCookieChoice; //

const { status, data } = await apiInstance.recordCookieConsent(
    jsonCookieChoice
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonCookieChoice** | **JsonCookieChoice**|  | |


### Return type

**JsonCookieConsent**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The recorded cookie consent, with its expiry |  -  |
|**401** | Not signed in |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

