import groovy.lang.GrabResolver;
@GrabResolver(name = 'repository', root = 'http://ci.decafdev.local/nexus/content/groups/public')

String inputFile = "/deployments2/success-jun-25-2017/all.txt";
String fileEncoding = "WINDOWS-1251";

BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), fileEncoding));

List<String> allowPatterns = Arrays.asList("posted");

while(true) {
    String line = bufferedReader.readLine()
    if (line == null) {
        break;
    }

    boolean allow = false;
    for(String pattern : allowPatterns) {
        if (line.contains(pattern)) {
            allow = true;
            break;
        }
    }
    if (allow) {
        println line.split("Result:")[0]
    }
}
