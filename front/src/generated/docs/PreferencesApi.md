# PreferencesApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getColorModePreference**](#getcolormodepreference) | **GET** /api/color-mode | Retrieve the display mode of the signed-in customer|
|[**getGuidedTourPreference**](#getguidedtourpreference) | **GET** /api/guided-tour | Retrieve whether the guided tour is shown to the signed-in customer|
|[**getLanguagePreference**](#getlanguagepreference) | **GET** /api/language | Retrieve the preferred language of the signed-in customer|
|[**recordColorModePreference**](#recordcolormodepreference) | **PUT** /api/color-mode | Record the display mode chosen by the signed-in customer|
|[**recordGuidedTourPreference**](#recordguidedtourpreference) | **PUT** /api/guided-tour | Record whether the guided tour is shown to the signed-in customer|
|[**recordLanguagePreference**](#recordlanguagepreference) | **PUT** /api/language | Record the preferred language chosen by the signed-in customer|

# **getColorModePreference**
> JsonColorModePreference getColorModePreference()

NONE when the customer never chose.

### Example

```typescript
import {
    PreferencesApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new PreferencesApi(configuration);

const { status, data } = await apiInstance.getColorModePreference();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**JsonColorModePreference**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The display mode |  -  |
|**401** | Not signed in |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getGuidedTourPreference**
> JsonGuidedTourPreference getGuidedTourPreference()

NONE when the customer never chose (the tour is shown then).

### Example

```typescript
import {
    PreferencesApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new PreferencesApi(configuration);

const { status, data } = await apiInstance.getGuidedTourPreference();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**JsonGuidedTourPreference**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Whether the guided tour is shown |  -  |
|**401** | Not signed in |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getLanguagePreference**
> JsonLanguagePreference getLanguagePreference()

NONE when the customer never chose.

### Example

```typescript
import {
    PreferencesApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new PreferencesApi(configuration);

const { status, data } = await apiInstance.getLanguagePreference();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**JsonLanguagePreference**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The preferred language |  -  |
|**401** | Not signed in |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **recordColorModePreference**
> JsonColorModePreference recordColorModePreference(jsonColorModeChoice)


### Example

```typescript
import {
    PreferencesApi,
    Configuration,
    JsonColorModeChoice
} from './api';

const configuration = new Configuration();
const apiInstance = new PreferencesApi(configuration);

let jsonColorModeChoice: JsonColorModeChoice; //

const { status, data } = await apiInstance.recordColorModePreference(
    jsonColorModeChoice
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonColorModeChoice** | **JsonColorModeChoice**|  | |


### Return type

**JsonColorModePreference**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The recorded display mode |  -  |
|**401** | Not signed in |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **recordGuidedTourPreference**
> JsonGuidedTourPreference recordGuidedTourPreference(jsonGuidedTourChoice)


### Example

```typescript
import {
    PreferencesApi,
    Configuration,
    JsonGuidedTourChoice
} from './api';

const configuration = new Configuration();
const apiInstance = new PreferencesApi(configuration);

let jsonGuidedTourChoice: JsonGuidedTourChoice; //

const { status, data } = await apiInstance.recordGuidedTourPreference(
    jsonGuidedTourChoice
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonGuidedTourChoice** | **JsonGuidedTourChoice**|  | |


### Return type

**JsonGuidedTourPreference**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The recorded choice |  -  |
|**401** | Not signed in |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **recordLanguagePreference**
> JsonLanguagePreference recordLanguagePreference(jsonLanguageChoice)


### Example

```typescript
import {
    PreferencesApi,
    Configuration,
    JsonLanguageChoice
} from './api';

const configuration = new Configuration();
const apiInstance = new PreferencesApi(configuration);

let jsonLanguageChoice: JsonLanguageChoice; //

const { status, data } = await apiInstance.recordLanguagePreference(
    jsonLanguageChoice
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonLanguageChoice** | **JsonLanguageChoice**|  | |


### Return type

**JsonLanguagePreference**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The recorded preferred language |  -  |
|**401** | Not signed in |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

