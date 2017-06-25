import com.mongodb.DBCollection
import com.mongodb.Mongo
import com.mongodb.MongoClientURI

@GrabResolver(name = 'repository', root = 'http://ci.decafdev.local/nexus/content/groups/public')
@Grab('com.gmongo:gmongo:1.5')
@Grab(group='org.mongodb', module='mongo-java-driver', version='3.4.2')

def uri = "mongodb://dev/text-files.test-collection";


MongoClientURI mongoURI = new MongoClientURI(uri);
Mongo mongo = new Mongo(mongoURI);
String collectionName = mongoURI.getCollection();
DBCollection collection = mongo.getDB(mongoURI.getDatabase()).getCollection(collectionName);

println collection