# JsonShippingMethod


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [default to undefined]
**label** | **string** |  | [default to undefined]
**cost** | **number** | In euros, 0 or more | [default to undefined]
**freeShippingEligibility** | **string** | ELIGIBLE - free from the threshold of the shop settings; NOT_ELIGIBLE - always at its cost | [default to undefined]
**status** | **string** | A disabled method is no longer proposed to the customers | [default to undefined]

## Example

```typescript
import { JsonShippingMethod } from './api';

const instance: JsonShippingMethod = {
    id,
    label,
    cost,
    freeShippingEligibility,
    status,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
