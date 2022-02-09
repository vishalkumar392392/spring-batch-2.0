CURRENT_DATE=`date '+%Y/%m/%d/%S'`
LESSON=$(basename $PWD)
mvn clean package -Dmaven.test.skip=true;
java -jar ./target/*-0.0.1-SNAPSHOT.jar "item=light" "run.date(date)=$CURRENT_DATE" "lesson=$LESSON";
read;