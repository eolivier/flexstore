# JsonSalesDashboard


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**firstDay** | **string** |  | [default to undefined]
**lastDay** | **string** |  | [default to undefined]
**currency** | **string** |  | [default to undefined]
**revenue** | **number** | What the paid orders of the period cost their customers (VAT and shipping included) | [default to undefined]
**orderCount** | **number** |  | [default to undefined]
**days** | [**Array&lt;JsonDailySales&gt;**](JsonDailySales.md) | Every day of the period, oldest first, with 0 for a day without order | [default to undefined]
**bestSellers** | [**Array&lt;JsonBestSeller&gt;**](JsonBestSeller.md) | The 5 products with the most units sold | [default to undefined]

## Example

```typescript
import { JsonSalesDashboard } from './api';

const instance: JsonSalesDashboard = {
    firstDay,
    lastDay,
    currency,
    revenue,
    orderCount,
    days,
    bestSellers,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
