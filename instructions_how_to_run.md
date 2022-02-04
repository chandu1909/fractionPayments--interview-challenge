#HOW-TO-RUN Payments Service  & Notification Service 

###As per the requirement I have created two microservices :
1. **Payments Service** : This exposes 4 APIs
    * **localhost:8090/fraction/get-all-users** - Allows Admin to see all the  users
    * **localhost:8090/fraction/get-balance** - Allows user to check the balance of their accounts 
    * **localhost:8090/fraction/draw-funds** - lets user make a debit transaction
    * **localhost:8090/fraction/pay-balance** - lets user make a credit transaction

2. **Notifications Service** : Does NOT expose any APIs. It only consumes the data from rabbit mq and process through 
a mock method that intermittently expects a HTTP 500 from third party services (MOCK) and responds accordingly. 

##Setting Up & Spining up Containers
1. unzip the folder fraction_challenge and extract the content and you will see two folders:
   * payments
   * NotificationService
2. open terminal / powershell to go into **payments** folder `$cd payments` 
3. Once you enter payments folder you should be able to see list of project files in the folder. 
4. run this command to build: `docker-compose build`
5. run this command to spinup containers: `docker compose up`

Boom ! you should see payments service , rabbitmq and postgres spinning up containers. Although, **you might see failures from payment service but don't worry it restarts automatically and
continue to connect with postgres whenever it is ready and healthy.**

This creates a network custom network in which all containers are running. You can check by running `docker netweok ls` if needed and we use this 
network to spin up notification service so that it can connect to the rabbitMQ.

once payments service is up, you will see a log `Payments Server Started successfully` and `Attempting to load the data to database from the utility class!!`

###Spinning up notification service
1. Go back the fraction folder where can see both payments and notificationService
2. go into the notificationService folder : `$cd notificationService`
3. similarly, run this command to build: `docker-compose build`
4. Next, run this command to spin up the container: `docker compose up`
5. You should see this container getting added to the existing network called fractionNetwork and connecting to RabbitMQ

_This concludes setting up the environment and now you have all services up and running_

-------

NOTES: THIS APPLICATION LOADS 10 RANDOM USERS AT THE BEGINNING OF THE APPLICATIONS WITH EACH 2 ACCOUNT AND 
AT LEAST 2 TRANSACTIONS 1ST ONE BEING INITIAL CREDIT TRANSACTION. 

##APIs

###### Note : please download and import the postman collection I have shared to make it easy

1. `localhost:8090/fraction/get-all-users`
    * This is a GET request and no parameters / headers needed. Prints all users loaded at the beginning which you need to use for further testing 
    
    
2. `localhost:8090/fraction/get-balance` 
    * payload : Requires a parameter in the request headers like below
    `userID: 76dc3884-5b79-486f-8db8-4692c0390650`  (one of the userID you copied from earlier result)
    

3. `localhost:8090/fraction/draw-funds`
   * requires no headers but below body
   ``` 
   {
    "userID":"76dc3884-5b79-486f-8db8-4692c0390650", //copy from the previous result
    "debit":650.00,
    "primary_account_number":"7958706d-70ff-4c6f-b5b0-23d09327241e", //copy from the previous result
    "idempotency-key":"san0121202453M"  //Should be unique for every new transaction (Frontend web server must send this)
   }
   ```
   
4. `localhost:8090/fraction/pay-balance`
    * requires NO headers but below body
   ```
   {
    "userID":"5f061da9-de20-4d32-a63e-3f7071205b8e",
    "credit":650.00,
    "primary_account_number":"17c88a1f-e485-4e14-a002-47c43868e1b8",
    "idempotency-key":"san0123202204AM" //must be unique
   }
   ```
   
