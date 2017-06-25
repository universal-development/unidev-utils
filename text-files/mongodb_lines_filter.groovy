import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.DBObject
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

File outputFile = new File("output.txt");

long total = collection.count()
List<String> allowPatterns = Arrays.asList("registered");
HashSet<String> hosts = new HashSet<>();

long currentLine = 0;
IncrementalStatistics stats = new IncrementalStatistics();


DBCursor dbCursor = collection.find()
while(dbCursor.hasNext()) {
    currentLine++;
    if (currentLine % 1000 == 0) {
        println "$currentLine / $total"
        println stats.toString()
    }
    DBObject dbObject = dbCursor.next();

    String host = dbObject.get("host") + ""
    String line = dbObject.get("line") + ""

    if (hosts.contains(host)) {
        stats.add("same-host")
        continue;
    }

    boolean allow = false;
    for(String pattern : allowPatterns) {
        if (line.contains(pattern)) {
            allow = true;
            break;
        }
    }

    if (allow) {
        stats.add("matches");
        outputFile.append(line + "\n");
        hosts.add(host);
    } else {
        stats.add("not-matching");
    }



}


