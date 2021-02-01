# CDC -> Hazelcast IMap, Portable PoC
Shows how to push changes from Postgres to Hazelcast IMap

## What is it good for?
It creates a highly scalable read-only automatically updated view into a relational database. 
Relational databases are notoriously hard to scale. Hazelcast makes it simple.

## What is unique about it?
It populates Hazelcast IMap with entries in the Portable format even the server does not have 
domain object bytecode on its classpath. 

Clients can query the IMap, register event listeners, creates Continous Query
Cache, etc. 

Not relying on User Code Deployment for Domain Objects
greatly simplifies schema evolution and job lifecycle.

## Project Structure:
The project consists of multiple modules: 
- jetjob - The data pipeline definition. Push changes from Postgres to Hazelcast IMap
- clientwithoutdomainobject - Illustrates how a client with no domain object on a classpath can still work with the IMap
- clientwithdomainobject - Shows how a client with a domain object on a classpath can work with the resulting IMap
- utils - Various utility methods

## How to use it
The demo is based on [this demo](https://jet-start.sh/docs/next/tutorials/cdc-postgres) except it populates IMap
with proper Portable objects instead of just strings. You can follow the linked demo to setup infrastructure.

Simplified steps are here: 

1. Use Docker to start Postgres database:  
   `docker run -it --rm --name postgres -p 5432:5432     -e POSTGRES_DB=postgres -e POSTGRES_USER=postgres     -e POSTGRES_PASSWORD=postgres debezium/example-postgres:1.2`
2. Start Postgres client: `docker exec -it postgres psql -U postgres` and select a default schema: `SET search_path TO inventory;`
3. Start Hazelcast Jet 4.4: `./bin/jet-start`
4. Build the jetjob Maven module: `mvn clean install`
5. Deploy the resulting JAR: `jet submit ./jetjob/target/jetjob-1.0-SNAPSHOT.jar`
6. Run the [`NoPortableMain`](https://github.com/jerrinot/cdcportable/blob/9c0ec43435444d048c57d411708c8361a6f5c6f6/clientwithoutdomainobject/src/main/java/info/jerrinot/cdcportable/client/client/NoPortableMain.java#L13) inside the clientwithoutdomainobject module
7. Run the [`PortableMain`](https://github.com/jerrinot/cdcportable/blob/9c0ec43435444d048c57d411708c8361a6f5c6f6/clientwithdomainobject/src/main/java/info/jerrinot/cdcportable/client/PortableMain.java#L16) inside the clientwithdomainobject module
8. Change a Postgres table by pasting this into the Postgres client started in the step #2: `update customers set last_name = 'NotThomas' where id=1001;` and watch output of NonPortableMain / PortableMain programs  

## How does it work
The data pipeline contains a [field mapping definition](https://github.com/jerrinot/cdcportable/blob/e045ca7c127301c1a29df4f5c2e62e5508ce9367/jetjob/src/main/java/info/jerrinot/cdcportable/server/job/JetJobMain.java#L21-L24) between `ChangeRecord` objects, which are emitted by the CDC source, and resulting Portable. 

This mapping is then [passed](https://github.com/jerrinot/cdcportable/blob/e045ca7c127301c1a29df4f5c2e62e5508ce9367/jetjob/src/main/java/info/jerrinot/cdcportable/server/job/JetJobMain.java#L39) to a generic [ChangeRecordMapper](https://github.com/jerrinot/cdcportable/blob/e045ca7c127301c1a29df4f5c2e62e5508ce9367/jetjob/src/main/java/info/jerrinot/cdcportable/server/job/mapping/ChangeRecordMapper.java#L15) which constructs the Portables.
The mapper uses programmatic construction of the Portable entries thus it does not need bytecode of the Portable objects.

It stores the resulting Portable entries in IMap. This means any client can query the IMap, you can push it over WAN, etc. 
Again, all this is possible even when server does not have bytecode of the portable entries available.   

## Prerequisites
1. JDK 15
2. Hazelcast Jet 4.4

## TODO
- HotRestart - allows the job to survive a complete cluster meltdown
- WAN - allows to replicate the resulting IMap to another Hazelcast cluster, 
  create hybrid on-prem - Cluster deployments, etc.
- Continous Query Cache example - that's pretty good as it pushes changes all the way up to a client.