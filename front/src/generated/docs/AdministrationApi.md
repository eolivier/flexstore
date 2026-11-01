# AdministrationApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**advanceOrderStatus**](#advanceorderstatus) | **POST** /api/admin/orders/{id}/status/advance | Move an order to the next status of its life cycle, PROCESSING then SHIPPED then DELIVERED (ADMIN role)|
|[**createDiscountCode**](#creatediscountcode) | **POST** /api/admin/discount-codes | Give a discount code (ADMIN role)|
|[**deleteDiscountCode**](#deletediscountcode) | **DELETE** /api/admin/discount-codes/{code} | Delete a discount code; the orders that used it keep their discount (ADMIN role)|
|[**deleteReview**](#deletereview) | **DELETE** /api/admin/reviews/{id} | Delete a review; the rating of its product no longer counts it (ADMIN role)|
|[**getAdminProduct**](#getadminproduct) | **GET** /api/admin/products/{id} | Retrieve a product with its translations in every language, to edit it|
|[**getAllOrders**](#getallorders) | **GET** /api/admin/orders | Retrieve the orders of all the customers, newest first, with their customer (ADMIN role)|
|[**getAnyOrder**](#getanyorder) | **GET** /api/admin/orders/{id} | Retrieve any order, with its customer (ADMIN role)|
|[**getDiscountCodes**](#getdiscountcodes) | **GET** /api/admin/discount-codes | Retrieve the discount codes, newest first (ADMIN role)|
|[**getSalesDashboard**](#getsalesdashboard) | **GET** /api/admin/sales-dashboard | Retrieve the revenue, the orders per day and the best sellers of the last 7, 30 or 90 days (ADMIN role)|
|[**getSeller**](#getseller) | **GET** /api/admin/seller | Retrieve the seller written on the next invoices (ADMIN role)|
|[**proposeSku**](#proposesku) | **GET** /api/admin/products/sku-proposal | Propose a SKU free in the catalog, from the category and the name of a product (ADMIN role)|
|[**refundOrder**](#refundorder) | **POST** /api/admin/orders/{id}/refund | Cancel a paid order before it is shipped, refund its payment and put its units back in stock (ADMIN role)|
|[**updateSeller**](#updateseller) | **PUT** /api/admin/seller | Change the seller written on the next invoices; the invoices issued keep theirs (ADMIN role)|

# **advanceOrderStatus**
> JsonAdminOrder advanceOrderStatus(jsonOrderStatusAdvance)


### Example

```typescript
import {
    AdministrationApi,
    Configuration,
    JsonOrderStatusAdvance
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

let id: string; // (default to undefined)
let jsonOrderStatusAdvance: JsonOrderStatusAdvance; //

const { status, data } = await apiInstance.advanceOrderStatus(
    id,
    jsonOrderStatusAdvance
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonOrderStatusAdvance** | **JsonOrderStatusAdvance**|  | |
| **id** | [**string**] |  | defaults to undefined|


### Return type

**JsonAdminOrder**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The order at its next status |  -  |
|**403** | The signed-in user is not an administrator |  -  |
|**404** | No order with this id |  -  |
|**409** | The order no longer has the given status, or it is delivered (no next status) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createDiscountCode**
> JsonDiscountCode createDiscountCode(jsonNewDiscountCode)


### Example

```typescript
import {
    AdministrationApi,
    Configuration,
    JsonNewDiscountCode
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

let jsonNewDiscountCode: JsonNewDiscountCode; //

const { status, data } = await apiInstance.createDiscountCode(
    jsonNewDiscountCode
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonNewDiscountCode** | **JsonNewDiscountCode**|  | |


### Return type

**JsonDiscountCode**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | The discount code given |  -  |
|**400** | Invalid code, reduction or usage limit |  -  |
|**409** | The code is already given |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteDiscountCode**
> deleteDiscountCode()


### Example

```typescript
import {
    AdministrationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

let code: string; // (default to undefined)

const { status, data } = await apiInstance.deleteDiscountCode(
    code
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **code** | [**string**] |  | defaults to undefined|


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
|**204** | Deleted |  -  |
|**404** | Unknown discount code |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteReview**
> deleteReview()


### Example

```typescript
import {
    AdministrationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.deleteReview(
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
|**204** | The review is deleted |  -  |
|**404** | Unknown review |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAdminProduct**
> JsonAdminProduct getAdminProduct()


### Example

```typescript
import {
    AdministrationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.getAdminProduct(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**JsonAdminProduct**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The product with its translations |  -  |
|**404** | Product not found |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAllOrders**
> Array<JsonAdminOrder> getAllOrders()


### Example

```typescript
import {
    AdministrationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

const { status, data } = await apiInstance.getAllOrders();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<JsonAdminOrder>**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The orders of all the customers |  -  |
|**403** | The signed-in user is not an administrator |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAnyOrder**
> JsonAdminOrder getAnyOrder()


### Example

```typescript
import {
    AdministrationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.getAnyOrder(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**JsonAdminOrder**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The order and its customer |  -  |
|**403** | The signed-in user is not an administrator |  -  |
|**404** | No order with this id |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getDiscountCodes**
> Array<JsonDiscountCode> getDiscountCodes()


### Example

```typescript
import {
    AdministrationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

const { status, data } = await apiInstance.getDiscountCodes();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<JsonDiscountCode>**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The discount codes |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getSalesDashboard**
> JsonSalesDashboard getSalesDashboard()


### Example

```typescript
import {
    AdministrationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

let days: number; //The number of days, the day of the request included (7, 30 or 90) (optional) (default to 30)

const { status, data } = await apiInstance.getSalesDashboard(
    days
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **days** | [**number**] | The number of days, the day of the request included (7, 30 or 90) | (optional) defaults to 30|


### Return type

**JsonSalesDashboard**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The sales of the period; only the paid orders count (not the cancelled nor the refunded ones) |  -  |
|**400** | A number of days other than 7, 30 or 90 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getSeller**
> JsonSeller getSeller()


### Example

```typescript
import {
    AdministrationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

const { status, data } = await apiInstance.getSeller();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**JsonSeller**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The current seller |  -  |
|**403** | Not an administrator |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **proposeSku**
> JsonSkuProposal proposeSku()

The first three letters of the category, the first two words of the name (eight characters at most each, without accents), then the first number from 0001 that no product has: CLO-ORGANIC-COTTON-0001

### Example

```typescript
import {
    AdministrationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

let category: string; // (default to undefined)
let name: string; //The name of the product in the default language (en), maybe not entered yet (optional) (default to undefined)

const { status, data } = await apiInstance.proposeSku(
    category,
    name
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **category** | [**string**] |  | defaults to undefined|
| **name** | [**string**] | The name of the product in the default language (en), maybe not entered yet | (optional) defaults to undefined|


### Return type

**JsonSkuProposal**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The SKU proposed |  -  |
|**403** | The signed-in user is not an administrator |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **refundOrder**
> JsonAdminOrder refundOrder(jsonOrderRefund)

The order must be PAID or PROCESSING, and still have the status given; it becomes REFUNDED and its customer is told by email.

### Example

```typescript
import {
    AdministrationApi,
    Configuration,
    JsonOrderRefund
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

let id: string; // (default to undefined)
let jsonOrderRefund: JsonOrderRefund; //

const { status, data } = await apiInstance.refundOrder(
    id,
    jsonOrderRefund
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonOrderRefund** | **JsonOrderRefund**|  | |
| **id** | [**string**] |  | defaults to undefined|


### Return type

**JsonAdminOrder**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The order, refunded |  -  |
|**403** | The signed-in user is not an administrator |  -  |
|**404** | No order with this id |  -  |
|**409** | The order no longer has the given status, or it cannot be refunded (not paid, or shipped) |  -  |
|**502** | The payment provider refused the refund; the order is unchanged |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateSeller**
> JsonSeller updateSeller(jsonSeller)


### Example

```typescript
import {
    AdministrationApi,
    Configuration,
    JsonSeller
} from './api';

const configuration = new Configuration();
const apiInstance = new AdministrationApi(configuration);

let jsonSeller: JsonSeller; //

const { status, data } = await apiInstance.updateSeller(
    jsonSeller
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jsonSeller** | **JsonSeller**|  | |


### Return type

**JsonSeller**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The seller, now current |  -  |
|**400** | A name or an address missing, a SIRET or a VAT number malformed |  -  |
|**403** | Not an administrator |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

