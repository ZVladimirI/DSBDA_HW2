echo "Preparing working space"
rm -r run 2>/dev/null
mkdir run

export SRC_DATA=run/data
export SRC_MAPPING=run/mapping
export DATA_FILE=/root/data
export MAP_FILE=/root/map
export HDFS_DATA=/user/root/flume_data
export HDFS_MAPPING=/user/root/flume_mapping
export HDFS_HOST
HDFS_HOST=$(docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' cluster)
export HDFS_PATH=hdfs://"$HDFS_HOST":9000
export HDFS_OUTPUT=/user/root/output


echo "Generating data"
python3 data_generator.py \
    --start-date 12.04.2021/09:00:00 \
    --end-date 13.04.2021/09:00:00  \
    --output $SRC_DATA \
    --posts 100 \
    --users 1000
python3 map_generator.py --output $SRC_MAPPING

echo "Data generated"

echo "Preparing dirs in HDFS"

docker exec cluster bash -c '"$HADOOP_HDFS_HOME"/bin/hdfs dfs -rm -r '"$HDFS_DATA" 2>/dev/null
docker exec cluster bash -c '"$HADOOP_HDFS_HOME"/bin/hdfs dfs -rm -r '"$HDFS_MAPPING" 2>/dev/null
docker exec cluster bash -c '"$HADOOP_HDFS_HOME"/bin/hdfs dfs -rm -r '"$HDFS_OUTPUT" 2>/dev/null
echo "Starting flume container"

docker run --network hadoop  --env FLUME_AGENT_NAME=docker_flume \
  --env HDFS_HOST="$HDFS_HOST" \
  --env FLUME_CONF_FILE=/opt/lib/flume/conf/flume.conf \
  --env DATA="$DATA_FILE" \
  --env MAPPING="$MAP_FILE" \
  --env HDFS_DATA="$HDFS_DATA" \
  --env HDFS_MAPPING="$HDFS_MAPPING" \
  --volume "$( dirname "$PWD" )"/config/flume.conf:/opt/lib/flume/conf/flume.conf \
  --volume "$(pwd)"/"$SRC_DATA":"$DATA_FILE" \
  --volume "$(pwd)"/"$SRC_MAPPING":"$MAP_FILE"  \
  --volume "$(pwd)"/stop_flume.sh:/root/stop_flume.sh \
  --detach  \
  --name flume \
  vladimirnemo/flume-python

echo "Waiting for new data in HDFS"
docker exec cluster bash -c '"$HADOOP_HDFS_HOME"/bin/hdfs dfs -ls "'"$HDFS_DATA"'"'  >/dev/null 2>&1
result=$?

while [ $result -ne 0 ]; do
    sleep 1
    docker exec cluster bash -c '"$HADOOP_HDFS_HOME"/bin/hdfs dfs -ls "'"$HDFS_DATA"'"'  >/dev/null 2>&1
    result=$?
  done

echo "New files created"
echo "Waiting for loading data completion"

docker exec cluster bash -c '"$HADOOP_HDFS_HOME"/bin/hdfs dfs -ls "'"$HDFS_DATA"'" | grep tmp' >/dev/null 2>&1
result=$?
while [ $result -eq 0 ]; do
    sleep 1
    docker exec cluster bash -c '"$HADOOP_HDFS_HOME"/bin/hdfs dfs -ls "'"$HDFS_DATA"'" | grep tmp' >/dev/null 2>&1
    result=$?
done

echo "Flume agent loaded data. Stopping Flume container"
docker exec flume bash /root/stop_flume.sh

while [ ! "$(docker ps -aq -f status=exited -f name=flume)" ]; do
    sleep 1
done
docker rm flume

echo "Flume container removed"

docker exec -it cluster bash -c 'spark-submit \
--class lab2.SparkApplication \
--master local \
--deploy-mode client \
--executor-memory 1g \
--name postcount \
--conf "spark.app.id=SparkApplication" \
Spark-1.0-SNAPSHOT-jar-with-dependencies.jar \
"'"$HDFS_DATA"'" "'"$HDFS_MAPPING"'" "'"$HDFS_OUTPUT"'"'

docker exec -it cluster bash