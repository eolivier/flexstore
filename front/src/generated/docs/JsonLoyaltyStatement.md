# JsonLoyaltyStatement


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**points** | **number** | The total of the points collected | [default to undefined]
**purchases** | [**Array&lt;JsonLoyaltyPurchase&gt;**](JsonLoyaltyPurchase.md) | The paid orders with the points they earned, newest first | [default to undefined]

## Example

```typescript
import { JsonLoyaltyStatement } from './api';

const instance: JsonLoyaltyStatement = {
    points,
    purchases,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
