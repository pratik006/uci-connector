mvn clean package

rm -rf server/dist/uci-connector-server-jar-with-dependencies.jar
mv server/target/uci-connector-server-jar-with-dependencies.jar server/dist

rm -rf client/dist/uci-client-jar-with-dependencies.jar
mv client/target/uci-client-jar-with-dependencies.jar client/dist
