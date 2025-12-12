# UsersApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**createUser**](#createuser) | **POST** /api/users | Create a user|
|[**deleteUser**](#deleteuser) | **DELETE** /api/users/{id} | Delete a user by ID|
|[**getAllUsers**](#getallusers) | **GET** /api/users | Retrieve all users|
|[**getUser**](#getuser) | **GET** /api/users/{id} | Retrieve a user by ID|
|[**loginUser**](#loginuser) | **POST** /api/users/login | Authenticate a user|
|[**updateUser**](#updateuser) | **PUT** /api/users/{id} | Update a user by ID|

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

No authorization required

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

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**204** | Successfully deleted the user |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAllUsers**
> JsonUser getAllUsers()

Returns the full list of users

### Example

```typescript
import {
    UsersApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new UsersApi(configuration);

const { status, data } = await apiInstance.getAllUsers();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**JsonUser**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Successfully retrieved the list of users |  -  |

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

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Successfully retrieved the user |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **loginUser**
> JsonUser loginUser(jsonLoginRequest)

Validates user credentials and returns user information on success

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

**JsonUser**

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

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateUser**
> updateUser(jsonUser)


### Example

```typescript
import {
    UsersApi,
    Configuration,
    JsonUser
} from './api';

const configuration = new Configuration();
const apiInstance = new UsersApi(configuration);

let id: string; // (default to undefined)
let jsonUser: JsonUser; //

const { status, data } = await apiInstance.updateUser(
    id,
    jsonUser
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonUser** | **JsonUser**|  | |
| **id** | [**string**] |  | defaults to undefined|


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
|**200** | Successfully updated the user |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

