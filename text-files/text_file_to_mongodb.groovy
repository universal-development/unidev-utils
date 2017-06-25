import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import com.mongodb.Mongo
import com.mongodb.MongoClientURI
import org.apache.commons.lang3.StringUtils

@GrabResolver(name = 'repository', root = 'http://ci.decafdev.local/nexus/content/groups/public')
@Grab('com.gmongo:gmongo:1.5')
@Grab(group='org.mongodb', module='mongo-java-driver', version='3.4.2')
@Grab(group='org.apache.commons', module='commons-lang3', version='3.6')

def uri = "mongodb://mongodev/text-files.test-collection";


MongoClientURI mongoURI = new MongoClientURI(uri);
Mongo mongo = new Mongo(mongoURI);
String collectionName = mongoURI.getCollection();
DBCollection collection = mongo.getDB(mongoURI.getDatabase()).getCollection(collectionName);
collection.createIndex("file")

String inputFile = "/deployments2/success-jun-25-2017/Success1.txt";
String fileEncoding = "WINDOWS-1251";
File file = new File(inputFile);

BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), fileEncoding));

while(true) {
    String line = bufferedReader.readLine()
    if (line == null) {
        break;
    }

    if (StringUtils.isBlank(line)) {
        continue;
    }

    try {
        String url = line.split("Result:")[0]
        URL parsedUrl = new URL(url);
        String host = parsedUrl.getHost();

        BasicDBObject record = new BasicDBObject();
        record.put("_id", line.hashCode());
        record.put("line", line);
        record.put("host", host);
        record.put("file", file.getName());

        collection.save(record)

    }catch (Exception e) {

    }

}
