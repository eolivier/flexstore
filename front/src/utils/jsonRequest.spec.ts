import { describe, expect, it } from 'vitest';
import { jsonRequest } from './jsonRequest';

describe('jsonRequest', () => {
  it.each(['POST', 'PUT', 'DELETE'])('should use the %s method', (method) => {
    // Act
    const request = jsonRequest(method, {});
    // Assert
    expect(request.method).toBe(method);
  });

  it('should declare a JSON content type', () => {
    // Act
    const request = jsonRequest('POST', { code: '123456' });
    // Assert
    expect(request.headers).toEqual({ 'Content-Type': 'application/json' });
  });

  it('should serialize the body as JSON', () => {
    // Arrange
    const body = { rating: 4, tags: ['a', 'b'], nested: { comment: 'nice' } };
    // Act
    const request = jsonRequest('PUT', body);
    // Assert
    expect(JSON.parse(request.body as string)).toEqual(body);
  });
});
