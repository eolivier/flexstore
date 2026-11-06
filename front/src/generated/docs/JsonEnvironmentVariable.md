# JsonEnvironmentVariable


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **string** |  | [default to undefined]
**sensitivity** | **string** | A SECRET (password, key) is never shown | [default to undefined]
**necessity** | **string** | A REQUIRED variable is needed by the shop to work properly | [default to undefined]
**status** | **string** | DEFINED when the value is neither null, nor empty, nor blank | [default to undefined]
**value** | **string** | The value of a defined PLAIN variable; absent for a SECRET and for an undefined variable | [optional] [default to undefined]

## Example

```typescript
import { JsonEnvironmentVariable } from './api';

const instance: JsonEnvironmentVariable = {
    name,
    sensitivity,
    necessity,
    status,
    value,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
