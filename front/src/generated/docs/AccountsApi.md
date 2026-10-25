# AccountsApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getAccount**](#getaccount) | **GET** /api/accounts/me | Retrieve the account of the signed-in user, with their phone number and postal addresses|
|[**registerAccount**](#registeraccount) | **POST** /api/accounts | Create the account of a customer and send them a verification link, valid for 1 hour|
|[**sendVerificationEmail**](#sendverificationemail) | **POST** /api/accounts/me/verification-email | Send a new verification link (valid for 1 hour) to the signed-in customer|
|[**verifyEmail**](#verifyemail) | **POST** /api/accounts/email-verification | Verify the email of an account with the token of the link sent by email|

# **getAccount**
> JsonAccount getAccount()


### Example

```typescript
import {
    AccountsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AccountsApi(configuration);

const { status, data } = await apiInstance.getAccount();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**JsonAccount**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The account (never the password) |  -  |
|**401** | Not signed in |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **registerAccount**
> JsonUser registerAccount(jsonAccountRegistration)

Public. The account waits for the verification of the email (status PENDING_VERIFICATION); the customer can already sign in and order.

### Example

```typescript
import {
    AccountsApi,
    Configuration,
    JsonAccountRegistration
} from './api';

const configuration = new Configuration();
const apiInstance = new AccountsApi(configuration);

let jsonAccountRegistration: JsonAccountRegistration; //

const { status, data } = await apiInstance.registerAccount(
    jsonAccountRegistration
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonAccountRegistration** | **JsonAccountRegistration**|  | |


### Return type

**JsonUser**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | Account created |  -  |
|**409** | The email is already used |  -  |
|**422** | A required field is missing or the password has fewer than 8 characters |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **sendVerificationEmail**
> sendVerificationEmail()

The previous links stop working

### Example

```typescript
import {
    AccountsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AccountsApi(configuration);

const { status, data } = await apiInstance.sendVerificationEmail();
```

### Parameters
This endpoint does not have any parameters.


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
|**204** | Link sent |  -  |
|**401** | Not signed in |  -  |
|**409** | The email is already verified |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **verifyEmail**
> verifyEmail(jsonEmailVerification)

Public. The account becomes ACTIVE and the link cannot be used again.

### Example

```typescript
import {
    AccountsApi,
    Configuration,
    JsonEmailVerification
} from './api';

const configuration = new Configuration();
const apiInstance = new AccountsApi(configuration);

let jsonEmailVerification: JsonEmailVerification; //

const { status, data } = await apiInstance.verifyEmail(
    jsonEmailVerification
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonEmailVerification** | **JsonEmailVerification**|  | |


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**204** | Email verified |  -  |
|**404** | Unknown link, or link already used |  -  |
|**410** | Link older than 1 hour |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

