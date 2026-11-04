# JsonPromotion

The percentage off the price of a product, from a moment (included) to a later one (excluded)

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**percentage** | **number** | From more than 0 to 100 | [default to undefined]
**startsAt** | **string** | ISO-8601 instant, to the minute | [default to undefined]
**endsAt** | **string** | ISO-8601 instant, to the minute, after startsAt | [default to undefined]

## Example

```typescript
import { JsonPromotion } from './api';

const instance: JsonPromotion = {
    percentage,
    startsAt,
    endsAt,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
