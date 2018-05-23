cd ../api
mvn clean install
cd ../client
mvn clean package
java -Dlog4j.configuration="file:./log4j.properties" -jar uci-client-jar-with-dependencies.jar