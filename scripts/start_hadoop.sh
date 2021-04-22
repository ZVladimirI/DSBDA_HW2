echo "Starting hadoop cluster..."
docker run -dit --network hadoop --name cluster -p 50070:50070 vladimirnemo/hadoop-single-node-spark
echo "=================Hadoop cluster started======================"
echo "Compiling jar..."
cd ..
mvn clean package
echo "=================JAR compiled======================"
docker cp "$(pwd)"/target/Spark-1.0-SNAPSHOT-jar-with-dependencies.jar cluster:/


