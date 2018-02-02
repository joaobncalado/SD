@echo on
title A simple batch suite
echo Welcome Hackerman!
cd T13-Komparator
start cmd /k mvn install -DskipITs
timeout 20
cd supplier-ws
start cmd /k mvn exec:java
timeout 15
start cmd /k mvn exec:java -Dws.i=2
timeout 15
cd ..
cd mediator-ws
start cmd /k mvn exec:java
timeout 12
cd ..
cd mediator-ws-cli
start cmd /k mvn exec:java
timeout 50