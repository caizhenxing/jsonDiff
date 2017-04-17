# jsonDiff
Creates a test diff from two provided JSON trough REST.
Toy project to implement Spring boot + Test suit.

Usage:

- POST to /v1/diff/{id}/left where {id} is a any long number. 
This enpoint is used to upload the data to be used to compare as "left side".

Format for data json should be:
{
  "binary": "<base64>"
}

Returns:

  - HTTP 201: if the resource was succesfully created.
  - HTTP 500: If some error happens when trying to persist the data.

- POST to /v1/diff/{id}/right where {id} is a any long number. 
This enpoint is used to upload the data to be used to compare as "right side".

Format for data json should be:
{
  "binary": "<base64>"
}

Returns:

  - HTTP 201: if the resource was succesfully created, containing the URI to it.
  - HTTP 500: If some error happens when trying to persist the data.

- GET to /v1/diff/{id} where {id} is a any long number already provided in the previous POST operations.
Note this endpoint needs both POST previous methods to be already called with the same and id, data in both, and in the same size to perform the diff.

Returns:
 
  - HTTP 200: "Data is exactly the same", if data size is the same and content is identical.
  - HTTP 200: "Data size is NOT the same" if the size of the data is different.
  - HTTP 200: The difference between the provided data.
  - HTTP 400: If one of the sides is missing for the given id.
  - HTTP 404: if the id that associate the data doesn't exist.
  - HTTP 500: If some error happens when trying to persist the data.
