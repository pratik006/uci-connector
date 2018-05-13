cd ../api
mvn clean install
cd ../client
mvn clean package
java -jar target/uci-client-jar-with-dependencies.jar