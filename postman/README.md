# Saving Planner Postman Notes

## Current state of this folder

At the moment, this `postman/` folder contains documentation only.

If you want to keep Postman assets in the repository later, this is the right place for files such as:

- `saving-planner.postman_collection.json`
- `saving-planner.local.postman_environment.json`

## Current default auth

The API currently uses **Basic Auth** with the credentials defined in `SecurityConfig`:

- username: `<username>`
- password: `<password>`

If you change the credentials in `SecurityConfig`, update your Postman environment as well.

## Base URLs

Use the base URL that matches how the backend is running:

- local Gradle / IntelliJ run: `http://localhost:8080`
- Docker Compose app run: `http://localhost:8081`

## Recommended request order

### Users

1. `POST /api/v1/users/user`
2. `GET /api/v1/users`
3. `GET /api/v1/users/{userId}`
4. `GET /api/v1/users/allinfo/{userId}`
5. `PUT /api/v1/users/{userId}`
6. `DELETE /api/v1/users/{userId}`

### Personal Finance

1. `POST /api/v1/finance/overview/create`
2. `GET /api/v1/finance/overview`
3. `GET /api/v1/finance/overview/{financeId}`
4. `PUT /api/v1/finance/overview/{financeId}`
5. `DELETE /api/v1/finance/overview/{financeId}`

## Notes

- All endpoints currently require authentication.
- User creation returns a JSON message wrapper.
- Some other write endpoints return plain success strings.
- `CreateUserRequest` accepts `password` and alias `passwordHash`.
- `CreateUserRequest` accepts `telephoneNumber` and alias `phoneNumber`.
- Finance create requests expect `monthlyExpenses.insurances` and `monthlyExpenses.subscriptions` as arrays.
