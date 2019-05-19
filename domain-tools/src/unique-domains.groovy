/**
 * Copyright (c) 2016 Denis O <denis@universal-development.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 1.1.0
 * Filter url list  by unique domain
 * groovy script.groovy <pattern> <outputfile>
 */


@GrabResolver(name = 'repository', root = 'http://ci.decafdev.local/nexus/content/groups/public')

@Grab('com.unidev.platform:unidev-spring:3.2.3-SNAPSHOT')
@Grab('com.unidev.platform:unidev-http-client:3.2.3-SNAPSHOT')
@Grab('ch.qos.logback:logback-classic:1.1.7')
@Grab('org.apache.ant:ant:1.9.5')

Set<String> hostCache = new HashSet<>()

import com.unidev.platform.common.utils.StringUtils;

import com.unidev.platform.StatisticsManager
import org.apache.tools.ant.DirectoryScanner
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.support.ClassPathXmlApplicationContext

ctx = new ClassPathXmlApplicationContext("classpath:/unidev-simple.xml");
StatisticsManager statisticsManager = new StatisticsManager();
StringUtils stringUtils = ctx.getBean(com.unidev.platform.common.utils.StringUtils.class);

Logger LOG = LoggerFactory.getLogger("")

String pattern = args[0];
String outputFileName = args[1];
LOG.info("scanning  {} sending unique rows to {}", pattern, outputFileName)

File rootFile = new File(".");

DirectoryScanner scanner = new DirectoryScanner();
scanner.setBasedir(rootFile)
scanner.setIncludes(pattern);
scanner.scan();

List<String> scannedFiles = Arrays.asList(scanner.getIncludedFiles());

LOG.info("Scanned files {}", scannedFiles.size())
LOG.info("Output file {}", outputFileName)

LOG.info("***Hit enter to continue***")

BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
br.readLine();

outputFile = new File(outputFileName);

long start = System.currentTimeMillis();

for(String fileName : scannedFiles) {

    LOG.info("Processing {}", fileName);
    File file = new File(fileName);

    file.eachLine { line ->

        String uriStr = line;
        // some url links generated by some programs can't be readed with URI/URL so we just parse value between // - /
        String host = stringUtils.substringBetween(uriStr, "//", "/");

        if (host == null) {
            host = stringUtils.substringBetween(uriStr, "//", "\t");
        }

        if (host == null) {
            host = stringUtils.substringBetween(uriStr, "//", " ");
        }

        if (host == null) {
            LOG.info("Failed to extract domain from {}", line);
            return;
        }

        if (hostCache.contains(host)) {
            statisticsManager.add("same-host");
            return;
        }
        hostCache.add(host);

        outputFile.append(line + "\n")
        statisticsManager.add("ok-lines");

    }
}

long end = System.currentTimeMillis();

LOG.info("done in {} ms", end - start)
LOG.info("Statistics {}", statisticsManager)

