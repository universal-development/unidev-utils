/**
 * Copyright (c) 2015 Denis O <denis@universal-development.com>
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
 * Script for converting files from directory into collection of json files
 */

@Grab('com.unidev.platform:platform-common:1.2.0')
@Grab('com.unidev.platform:platform-statistics-simple:1.0.0')
@GrabExclude('xml-apis:xml-apis')

import groovy.io.FileType
import groovy.json.*

import com.unidev.platform.statistics.StatisticsManager
import com.unidev.platform.utils.StringUtils

import org.springframework.context.annotation.ClassPathBeanDefinitionScanner
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.context.support.GenericApplicationContext



ctx = new ClassPathXmlApplicationContext("classpath:/platform-common-beans.xml");

StatisticsManager statisticsManager = ctx.getBean(StatisticsManager.class);

File inFile = new File(args[0]);
File outputFile = new File(args[1]);

println "in: " + inFile
println "out : " + outputFile

inFile.eachLine {line ->
    statisticsManager.add("total-lines");

    if (StringUtils.isBlank(line)) {
        return;
    }

    try {
        URI uri = new URI(line);
        String domainLine =  uri.getScheme() + "://" + uri.getHost();

        outputFile.append(domainLine + "\n");

        statisticsManager.add("ok-link");
    }catch (Throwable t) {
        t.printStackTrace();
        statisticsManager.add("invalid-link");
    }

}

println "=== Done ==="

println statisticsManager.toString();