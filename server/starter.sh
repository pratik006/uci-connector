cd ../api
mvn clean install
cd ../server
mvn clean package
java -jar target/uci-connector-jar-with-dependencies.jar