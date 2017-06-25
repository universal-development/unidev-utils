import groovy.lang.GrabResolver;
@GrabResolver(name = 'repository', root = 'http://ci.decafdev.local/nexus/content/groups/public')

String inputFile = "/deployments2/success-jun-25-2017/all.txt";
String fileEncoding = "WINDOWS-1251";

BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), fileEncoding));

List<String> allowPatterns = Arrays.asList("registered");
HashSet<String> hosts = new HashSet<>();

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
        try {
            String url = line.split("Result:")[0]
            URL parsedUrl = new URL(url);
            String host = parsedUrl.getHost();
            if (hosts.contains(host)) {
                continue;
            }
            hosts.add(host);
            println url
        }catch (Exception e) {

        }
    }
}
