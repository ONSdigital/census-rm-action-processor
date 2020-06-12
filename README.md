# census-rm-action-processor

[![Build Status](https://travis-ci.com/ONSdigital/census-rm-action-processor.svg?branch=master)](https://travis-ci.com/ONSdigital/census-rm-action-processor)

# Overview
This service processes all the case & uac-qid messages which are emitted from Case Processor, to maintain a replica of the data, for performance reasons during action rule processing, which will affect many millions of cases.

This service handles ad hoc fulfilment requests. These are real time requests for actions on a census case e.g. mailing out a questionnaire which was requested over the phone. These requests are queued in a `fulfilment_to_send` table.

The Action Processor is implemented with Java 11 & Spring Integration, it is event driven listening to rabbitmq and persisting data to a Postgres SQL DB.


#  MessageEndpoints

There are multiple entry points to this application, these can be found in the messaging folder/package, each 
class in here is a listener to a queue (defined in the application yml).  These classes are annotated 
@MessageEndpoint and each consists of a receiveMessage function bound to a queue and marked @Transactional.  The 
 @Transactional part wraps every queuing & database action under this function into 1 big transaction.  If any of this 
fails they all get rolled back and the original message will be returned unharmed to the rabbit queue.  After several
failures the MessageException Service is configured to place a bad message onto a backoff queue.

The Action Processor consumes the `CASE_CREATED`, `CASE_UPDATED`, `UAC_UPDATED` and `FULFILMENT_REQUEST` events from the fanout exchange.


# Testing

To test this service locally use:

```shell-script
mvn clean install
```   
This will run all of the unit tests, then if successful create a docker image for this application 
then bring up the required docker images from the test [docker compose YAML](src/test/resources/docker-compose.yml) (postgres and rabbit)
to run the Integration Tests.

# Debug    
 If you want to debug the application/Integration tests start the required docker images by navigating 
 to [src/test/resources/](src/test/resources/) and then run :
 
```shell-script
docker-compose up
```

You should then be able to run the tests from your IDE.

# Configuration
By default the src/main/resources/application.yml is configured for 
[census-rm-docker-dev](https://github.com/ONSdigital/census-rm-docker-dev)

For production the configuration is overridden by the K8S apply script.

The queues are defined in test [definitions.json](src/test/resources/definitions.json) for Integration Tests.
