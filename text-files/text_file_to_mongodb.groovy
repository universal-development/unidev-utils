import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import com.mongodb.Mongo
import com.mongodb.MongoClientURI
import com.unidev.components.statistics.IncrementalStatistics
import org.apache.commons.lang3.StringUtils

@GrabResolver(name = 'repository', root = 'http://ci.decafdev.local/nexus/content/groups/public')
@Grab('com.gmongo:gmongo:1.5')
@Grab(group='org.mongodb', module='mongo-java-driver', version='3.4.2')
@Grab(group='org.apache.commons', module='commons-lang3', version='3.6')
@Grab('com.unidev.components.statistics:simple-statistics:0.0.2-SNAPSHOT')

def uri = "mongodb://seo-warehouse/xrfiles.success_pack_2017_06";


MongoClientURI mongoURI = new MongoClientURI(uri);
Mongo mongo = new Mongo(mongoURI);
String collectionName = mongoURI.getCollection();
DBCollection collection = mongo.getDB(mongoURI.getDatabase()).getCollection(collectionName);
collection.createIndex("file")

String inputFile = "/deployments2/success-jun-25-2017/all.txt";
String fileEncoding = "WINDOWS-1251";
File file = new File(inputFile);

def proc = "wc -l $inputFile".execute()
def output = new StringBuffer()
proc.consumeProcessErrorStream(output)
long totalLineCount = Long.parseLong(proc.text.split(" ")[0])
println "Total: $totalLineCount"

long currentLine = 0;
IncrementalStatistics stats = new IncrementalStatistics();

BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), fileEncoding));
while(true) {
    String line = bufferedReader.readLine()
    if (line == null) {
        break;
    }
    currentLine++;

    if (currentLine % 1000 == 0) {
        println "Progress: $currentLine / $totalLineCount"
        println stats.toString()
    }

    if (StringUtils.isBlank(line)) {
        stats.add("dropped-lines")
        continue;
    }

    int id = line.hashCode();
    if (collection.findOne(id) != null) {
        stats.add("existing-line");
        continue;
    }

    try {
        String url = line.split("Result:")[0]
        URL parsedUrl = new URL(url);
        String host = parsedUrl.getHost();

        BasicDBObject record = new BasicDBObject();
        record.put("_id", id);
        record.put("line", line);
        record.put("host", host);
        record.put("file", file.getName());

        collection.save(record)

        stats.add("accepted-lines")
    }catch (Exception e) {
        stats.add("error-lines")
    }

}
