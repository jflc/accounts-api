# Accounts API

API to transfer money between accounts.

## Dependencies
* Java 1.8

## How to run
```shell script
$ ./gradlew run
```
Application will be listening at `http://localhost:8080`

## API Operations

### List accounts
Endpoint to list all accounts.

**Request:** 

`GET /accounts`

**Response:**

```json
{ 
  "results": [
    {
      "id": "uuid"
    } 
  ]
}
```

`Status Code: 200 OK`


### Get account
Endpoint to get account details.

**Request:** 

`GET /accounts/<id>`

**Response:**

```json
{
  "id": "uuid",
  "name": "string",
  "balance": 0.00
}
```

`Status Code: 200 OK`


### Transfer money
Endpoint to transfer money between accounts.

**Request:** 

`POST /accounts/transfer`
```json
{
  "requestId": "uuid",
  "fromAccountId": "uuid",
  "toAccountId": "uuid",
  "amount": 0.00
}
```

**Response:**

```json
{
  "requestId": "027abd9f-3e04-4ed6-8410-4b373a1e61e5",
  "at": "2019-08-11T10:43:24.690Z"
}
```

`Status Code: 201 Created`

## Testing data
This API doesn't include any endpoint to create accounts. Although the follwoing account ids are available to use:
* aaee2b13-8a5e-4aed-a30b-5d8535c8ab20
* 1ce455f7-f30c-4f55-81e2-7df2e8f88c7d
* fb789eb9-a5a9-4ebe-a808-a9cd59b19772

Use the `Get account` endpoint to get more information about each one and feel free to transfer money between them using `Transfer money` endpoint


## Tech Stack
* Language: Kotlin
* Build Tool: Gradle
* Web Framework: Ktor (lightweight)
* Server Engine: Netty
* Database: H2 (in-memory)
* Testing: Junit & Postman
* SQL Library: Exposed
* Mock Library: MockK

## TODO
* Improve error handling
* Improve input data validation
* Add transactions endpoint to retrieve all account transactions (e.g. `GET /accounts/<id>/transactions`)
* Include database migration tool (e.g. Liquibase)
