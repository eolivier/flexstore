# InvoicesApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getAnyInvoice**](#getanyinvoice) | **GET** /api/admin/orders/{id}/invoice | Download the invoice of any paid order (PDF, ADMIN role)|
|[**getInvoice**](#getinvoice) | **GET** /api/orders/{id}/invoice | Download the invoice of a paid order of the signed-in customer (PDF)|

# **getAnyInvoice**
> File getAnyInvoice()


### Example

```typescript
import {
    InvoicesApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new InvoicesApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.getAnyInvoice(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**File**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/pdf


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The invoice |  -  |
|**403** | Not an administrator |  -  |
|**404** | No such order, or the order is not paid (no invoice) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getInvoice**
> File getInvoice()


### Example

```typescript
import {
    InvoicesApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new InvoicesApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.getInvoice(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**File**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/pdf


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | The invoice, saved as facture-&lt;number of the order&gt;.pdf |  -  |
|**401** | Not signed in |  -  |
|**404** | No such order of the customer, or the order is not paid (no invoice) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

