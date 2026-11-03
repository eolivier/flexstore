# JsonOrderTracking

Where the parcel of a shipped order is followed. Absent before the order is shipped, and when it was shipped without tracking

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**carrier** | **string** |  | [default to undefined]
**trackingNumber** | **string** | Letters, digits and hyphens, 40 at most | [default to undefined]
**trackingUrl** | **string** | The tracking page of the carrier for this parcel; none for the carrier OTHER | [optional] [default to undefined]

## Example

```typescript
import { JsonOrderTracking } from './api';

const instance: JsonOrderTracking = {
    carrier,
    trackingNumber,
    trackingUrl,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
