# AccountsApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**changeAddress**](#changeaddress) | **PUT** /api/accounts/me/addresses/{position} | Change an address of the signed-in user (0 for the first one); the orders keep theirs|
|[**getAccount**](#getaccount) | **GET** /api/accounts/me | Retrieve the account of the signed-in user, with their phone number and postal addresses|
|[**getAddresses**](#getaddresses) | **GET** /api/accounts/me/addresses | Retrieve the postal addresses of the signed-in user, in their order|
|[**registerAccount**](#registeraccount) | **POST** /api/accounts | Create the account of a customer and send them a verification link, valid for 1 hour|
|[**removeAddress**](#removeaddress) | **DELETE** /api/accounts/me/addresses/{position} | Remove an address of the signed-in user (0 for the first one); the orders keep theirs|
|[**sendVerificationEmail**](#sendverificationemail) | **POST** /api/accounts/me/verification-email | Send a new verification link (valid for 1 hour) to the signed-in customer|
|[**verifyEmail**](#verifyemail) | **POST** /api/accounts/email-verification | Verify the email of an account with the token of the link sent by email|

# **changeAddress**
> Array<JsonPostalAddress> changeAddress(jsonPostalAddress)


### Example

```typescript
import {
    AccountsApi,
    Configuration,
    JsonPostalAddress
} from './api';

const configuration = new Configuration();
const apiInstance = new AccountsApi(configuration);

let position: number; // (default to undefined)
let jsonPostalAddress: JsonPostalAddress; //

const { status, data } = await apiInstance.changeAddress(
    position,
    jsonPostalAddress
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonPostalAddress** | **JsonPostalAddress**|  | |
| **position** | [**number**] |  | defaults to undefined|


### Return type

**Array<JsonPostalAddress>**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The addresses, with the changed one at its place |  -  |
|**400** | Incomplete address |  -  |
|**404** | No address at this position |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

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

# **getAddresses**
> Array<JsonPostalAddress> getAddresses()


### Example

```typescript
import {
    AccountsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AccountsApi(configuration);

const { status, data } = await apiInstance.getAddresses();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<JsonPostalAddress>**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The addresses |  -  |

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

# **removeAddress**
> Array<JsonPostalAddress> removeAddress()


### Example

```typescript
import {
    AccountsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AccountsApi(configuration);

let position: number; // (default to undefined)

const { status, data } = await apiInstance.removeAddress(
    position
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **position** | [**number**] |  | defaults to undefined|


### Return type

**Array<JsonPostalAddress>**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The remaining addresses |  -  |
|**404** | No address at this position |  -  |

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

