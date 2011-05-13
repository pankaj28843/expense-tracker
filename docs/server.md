#Server

Current Hosting: <xtrack.ep.io>

##API

  - Json based responses on get queries.

  - Login/Sync:

      - Query Format:

          - Login: server-domain/mobile-login/?u=username&p=password
          - Sync: server-domain/sync/?token=token_key

      - Response:
        user id|token|token id|projects(csv)|project_ids(csv)|type(csv)|locations(csv)|bills count

  - Expense:

      - Query:
        server-domain/add-expense/?q=authentication_token,location,amount,official/personal,project,category,bill_id

      - Response:
        No. of expenses added

##Models

###Locations

  - Name

###Organisation

  - Title
  - Locations
  - Users

###Project

  - Title
  - Organisation

###Auth Token
  - User
  - Organisation
  - Key

###Expense

  - Auth Token
  - Amount
  - City
  - Personal/Official
  - Project
  - Category
  - Billed/Unbilled
  - Bill Id
  - Bill Image
