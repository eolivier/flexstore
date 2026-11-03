# JsonOrderStatusAdvance

The status of the order when the administrator asks to move it to the next one; moving a processed order to shipped may give the carrier and the tracking number, together or not at all

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**currentStatus** | **string** |  | [default to undefined]
**carrier** | **string** |  | [optional] [default to undefined]
**trackingNumber** | **string** | Letters, digits and hyphens, 40 at most | [optional] [default to undefined]

## Example

```typescript
import { JsonOrderStatusAdvance } from './api';

const instance: JsonOrderStatusAdvance = {
    currentStatus,
    carrier,
    trackingNumber,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
