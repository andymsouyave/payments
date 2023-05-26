# Payments Transfer System Spring Boot Project

## Getting Started

### Prerequisites

* Git
* JDK 17 (Minimum required by Spring Boot 3)

### Swagger

Swagger should be available for use on the root context path of the server.

## Information

### Decisions

 * Went with Spring WebFlux instead of typical MVC as I felt like a transactional system (similar to the FX Trading platforms I am used to) should be as responsive and 'live' as possible
 * Decided against a 'NEW' status on the accounts, as didn't end up having the time to add a nightly cron to go through and change them based on a configurable period of time.

### Assumptions

 * Account id's are not provided and are automatically generated
 * The account currencies require matching, assuming FX conversion is out of the scope of this project 

### Challenges

 * Lombok Builder annotations were causing default values to fail which was due to it clashing with some other anonotations
 * Have gotten used to relational databases, so it was difficult to flip my head around storing the data in data structures instead and how that would work. 

### Things to be aware of

  * As WebFlux is used instead of traditional MVC, all the endpoints are there for streaming, but to conform to the requirements of JSON return value I have not added the streaming MediaType to the rest endpoints (ie produces = MediaType.TEXT_EVENT_STREAM_VALUE), and they should all just return JSON according to pure request-response design.

