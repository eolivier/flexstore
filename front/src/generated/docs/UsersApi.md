# UsersApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**changeAccountStatus**](#changeaccountstatus) | **PUT** /api/users/{id}/status | Change the status of an account|
|[**changePassword**](#changepassword) | **POST** /api/users/{id}/password | Change the password of a user|
|[**createUser**](#createuser) | **POST** /api/users | Create a user|
|[**deleteUser**](#deleteuser) | **DELETE** /api/users/{id} | Delete a user by ID|
|[**getAllUsers**](#getallusers) | **GET** /api/users | Retrieve a page of the users, the first created first (ADMIN role)|
|[**getUser**](#getuser) | **GET** /api/users/{id} | Retrieve a user by ID|
|[**loginUser**](#loginuser) | **POST** /api/users/login | Authenticate a user|
|[**logoutUser**](#logoutuser) | **POST** /api/users/logout | Sign out, ending the session of a refresh token|
|[**refreshSession**](#refreshsession) | **POST** /api/users/refresh | Renew the access of a signed-in user with their refresh token|
|[**updateUser**](#updateuser) | **PUT** /api/users/{id} | Edit a user by ID (first name, last name, email, role; not the password)|

# **changeAccountStatus**
> JsonUser changeAccountStatus(jsonAccountStatus)

Reserved to an administrator, on another account than their own. ACTIVE, SUSPENDED, LOCKED or DISABLED; a suspended, locked or disabled account can neither sign in nor order.

### Example

```typescript
import {
    UsersApi,
    Configuration,
    JsonAccountStatus
} from './api';

const configuration = new Configuration();
const apiInstance = new UsersApi(configuration);

let id: string; // (default to undefined)
let jsonAccountStatus: JsonAccountStatus; //

const { status, data } = await apiInstance.changeAccountStatus(
    id,
    jsonAccountStatus
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonAccountStatus** | **JsonAccountStatus**|  | |
| **id** | [**string**] |  | defaults to undefined|


### Return type

**JsonUser**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The user with their new status |  -  |
|**403** | The editor is not an administrator |  -  |
|**422** | Own account, PENDING_VERIFICATION or unknown status |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **changePassword**
> changePassword(jsonChangePasswordRequest)


### Example

```typescript
import {
    UsersApi,
    Configuration,
    JsonChangePasswordRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new UsersApi(configuration);

let id: string; // (default to undefined)
let jsonChangePasswordRequest: JsonChangePasswordRequest; //

const { status, data } = await apiInstance.changePassword(
    id,
    jsonChangePasswordRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonChangePasswordRequest** | **JsonChangePasswordRequest**|  | |
| **id** | [**string**] |  | defaults to undefined|


### Return type

void (empty response body)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**204** | Password changed |  -  |
|**400** | Wrong current password |  -  |
|**401** | Missing or invalid token |  -  |
|**403** | Not the password of the signed-in user |  -  |
|**422** | New password too short |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createUser**
> JsonUser createUser(jsonUserCreate)


### Example

```typescript
import {
    UsersApi,
    Configuration,
    JsonUserCreate
} from './api';

const configuration = new Configuration();
const apiInstance = new UsersApi(configuration);

let jsonUserCreate: JsonUserCreate; //

const { status, data } = await apiInstance.createUser(
    jsonUserCreate
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonUserCreate** | **JsonUserCreate**|  | |


### Return type

**JsonUser**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | User created |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteUser**
> deleteUser()


### Example

```typescript
import {
    UsersApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new UsersApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.deleteUser(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


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
|**204** | Successfully deleted the user |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAllUsers**
> Array<JsonUser> getAllUsers()

The total number of users is in the header X-Total-Count; a size above 100 is brought back to 100

### Example

```typescript
import {
    UsersApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new UsersApi(configuration);

let page: number; //The number of the page, from 0 (a negative number is brought back to 0) (optional) (default to 0)
let size: number; //The number of items of a page (a size out of 1 to 100 is brought back to the nearest limit) (optional) (default to 20)

const { status, data } = await apiInstance.getAllUsers(
    page,
    size
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **page** | [**number**] | The number of the page, from 0 (a negative number is brought back to 0) | (optional) defaults to 0|
| **size** | [**number**] | The number of items of a page (a size out of 1 to 100 is brought back to the nearest limit) | (optional) defaults to 20|


### Return type

**Array<JsonUser>**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | A page of the users |  * X-Total-Count -  <br>  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getUser**
> JsonUser getUser()


### Example

```typescript
import {
    UsersApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new UsersApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.getUser(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**JsonUser**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Successfully retrieved the user |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **loginUser**
> JsonLoginResponse loginUser(jsonLoginRequest)

Validates user credentials and returns the token (valid for 15 minutes), the refresh token (valid for 7 days, it works once) and the user

### Example

```typescript
import {
    UsersApi,
    Configuration,
    JsonLoginRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new UsersApi(configuration);

let jsonLoginRequest: JsonLoginRequest; //

const { status, data } = await apiInstance.loginUser(
    jsonLoginRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonLoginRequest** | **JsonLoginRequest**|  | |


### Return type

**JsonLoginResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Successfully authenticated |  -  |
|**401** | Invalid credentials |  -  |
|**403** | The account is SUSPENDED, LOCKED or DISABLED: { error, status } |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **logoutUser**
> logoutUser(jsonRefreshRequest)

Signing out twice, or with an unknown refresh token, is no error

### Example

```typescript
import {
    UsersApi,
    Configuration,
    JsonRefreshRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new UsersApi(configuration);

let jsonRefreshRequest: JsonRefreshRequest; //

const { status, data } = await apiInstance.logoutUser(
    jsonRefreshRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonRefreshRequest** | **JsonRefreshRequest**|  | |


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
|**204** | The session is ended |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **refreshSession**
> JsonRefreshResponse refreshSession(jsonRefreshRequest)

The refresh token works once: the response holds a new token and a new refresh token that replaces it. A refresh token that was already used (or whose session was ended) ends all the sessions of its user. Public route: the refresh token is the credential.

### Example

```typescript
import {
    UsersApi,
    Configuration,
    JsonRefreshRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new UsersApi(configuration);

let jsonRefreshRequest: JsonRefreshRequest; //

const { status, data } = await apiInstance.refreshSession(
    jsonRefreshRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonRefreshRequest** | **JsonRefreshRequest**|  | |


### Return type

**JsonRefreshResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | A new token and the refresh token that replaces the one presented |  -  |
|**401** | Unknown, expired or already used refresh token |  -  |
|**403** | The account is SUSPENDED, LOCKED or DISABLED: { error, status } |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateUser**
> JsonUser updateUser(jsonUserUpdate)

Reserved to an administrator, checked by the domain (403 otherwise)

### Example

```typescript
import {
    UsersApi,
    Configuration,
    JsonUserUpdate
} from './api';

const configuration = new Configuration();
const apiInstance = new UsersApi(configuration);

let id: string; // (default to undefined)
let jsonUserUpdate: JsonUserUpdate; //

const { status, data } = await apiInstance.updateUser(
    id,
    jsonUserUpdate
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonUserUpdate** | **JsonUserUpdate**|  | |
| **id** | [**string**] |  | defaults to undefined|


### Return type

**JsonUser**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The edited user |  -  |
|**403** | The editor is not an administrator |  -  |
|**404** | Unknown user |  -  |
|**409** | The email is used by another user |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

