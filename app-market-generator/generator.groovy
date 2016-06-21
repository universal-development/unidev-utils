/**
 * Copyright (c) 2015,2016 Denis O <denis@universal-development.com>
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
import com.unidev.customcms.robotstxt.service.RobotsTxtService
import com.unidev.platform.common.utils.StringUtils
import com.unidev.platform.template.TemplateBuilder
import freemarker.template.Template
import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.filefilter.IOFileFilter
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@GrabResolver(name = 'nexus', root = 'http://dev/nexus/content/groups/public')
@Grab('com.unidev.platform.spring:simple-config:2.0.0-SNAPSHOT')
@Grab('com.unidev.platform.components:template-freemarker:2.0.0-SNAPSHOT')
@Grab('com.unidev.customcms:customcms-robotstxt:1.1.0')
//@Grab('com.unidev.customcms:customcms-sitemap:1.1.0')

@Grapes([
        @Grab(group='org.xerial',module='sqlite-jdbc',version='3.7.2'),
        @GrabConfig(systemClassLoader=true)
])

@GrabExclude('xml-apis:xml-apis')

import java.sql.*
import org.sqlite.SQLite
import groovy.sql.Sql

LOG = LoggerFactory.getLogger("")
LOG.info("DB loading...")
Class.forName("org.sqlite.JDBC")

String dbFile = "/tmp/db" + System.currentTimeMillis() + ".db"

def sql = Sql.newInstance("jdbc:sqlite:" + dbFile, "org.sqlite.JDBC")

sql.execute("drop table if exists app")
sql.execute("create table app (app_package  string, category string, creation_date  DATE )")
def appdb = sql.dataSet("app")

def ctx = new ClassPathXmlApplicationContext("classpath:/unidev-simple.xml");

FILES_DIR = "./files";
THEME_DIR = "./theme"
OUTPUT_DIR = "./output"

def jsonSlurper = new JsonSlurper()
def market = jsonSlurper.parse(new File("market.json"));

LOG.info("*** Market generator ***")
LOG.info("*** Configurations ***\n$FILES_DIR\n$THEME_DIR\n$OUTPUT_DIR\n")

Map<String, Object> apps = new HashMap<>();
Map<String, File> appFolders = new HashMap<>();
List categories = new ArrayList();
List<String> links = new ArrayList();

IOFileFilter folderFilter = new IOFileFilter() {

    @Override
    boolean accept(File file) {
        return true;
    }

    @Override
    boolean accept(File file, String s) {
        return true;
    }
};

ignoredExtensions = ["ftl", "json", "md"]

IOFileFilter marketFileFilter =  new IOFileFilter() {

    @Override
    boolean accept(File file) {
        String extension = FilenameUtils.getExtension(file.getName())
        if (ignoredExtensions.contains(extension)) {
            return false;
        }
        return true;
    }

    @Override
    boolean accept(File dir, String name) {
        String extension = FilenameUtils.getExtension(name)
        if (ignoredExtensions.contains(extension)) {
            return false;
        }
        return true;
    }
};

new File(FILES_DIR).listFiles().each {
    if (!it.isDirectory()) {
        return;
    }
    LOG.info("Folder {}", it.getName())

    File appFile = new File(it.getPath() + "/" + "app.json");
    def app = jsonSlurper.parseText(appFile.text);
    LOG.info("Processing {}", app)

    String outputFolder = app.package;


    app.outputFile = "app/" + outputFolder + "/" + "index.html"
    app.outputFolder = "app/" + outputFolder
    LOG.info("Market object {}", app)

    apps.put(app.package, app);
    appFolders.put(app.package, it);

    //app_package  string, category string, creation_date  DATE
    appdb.add(app_package:app.package, category: app.category, creation_date: new Date(it.lastModified()))

}

sql.eachRow("select distinct category from app order by category asc") {
    println("id=${it.category} ")

    String category = it.category
    String c = (category + "").toLowerCase();
    c = c.replaceAll("[^A-Za-z0-9]", "-")
    categories.add("title" : category + "", "link" : c);

}

LOG.info("Categories {}", categories)

LOG.info("Rendering app pages");

for(Object app : apps.values()) {
    TemplateBuilder templateBuilder;
    File appFolder = appFolders.get(app.package);

    if (app.template != null) {
        templateBuilder = TemplateBuilder.newFilePathTemplate(appFolder.getAbsolutePath(), app.template)
    } else {
        templateBuilder = TemplateBuilder.newFilePathTemplate(THEME_DIR, "app.ftl")
    }

    String appOutputFile = templateBuilder
            .addVariable("app", app)
            .addVariable("market", market)
            .addVariable("categories", categories)
            .build();

    writeOutput(app.outputFolder, "index.html", appOutputFile)
    links.add(app.outputFolder + "");

    Collection<File> files = FileUtils.listFiles(appFolder,marketFileFilter,folderFilter);

    File appParentFile = appFolder

    files.forEach( {
        LOG.info("Copy file {}", it)
        copyFileToOutput(appParentFile, it, app.outputFolder);
    })

}


LOG.info("Loaded apps {}", apps)

LOG.info("Rendering categories page");

for(Object category : categories) {

    List orderedAppList = new ArrayList();

    sql.eachRow("select app_package from app WHERE category='"+category['title']+"' order by creation_date desc") {
        println("app_package=${it.app_package} ")

        orderedAppList.add(apps.get(it.app_package));
    }


    String appList = templateBuilder = TemplateBuilder.newFilePathTemplate(THEME_DIR, "applist.ftl")
            .addVariable("market", market)
            .addVariable("apps",orderedAppList)
            .addVariable("packages", apps.keySet())
            .addVariable("categories", categories)
            .addVariable("category", category)
            .build();

    writeOutput(category.link,  "/index.html", appList)
    links.add(category.link + "");

}

LOG.info("Rendering index")

List orderedAppList = new ArrayList();

sql.eachRow("select app_package from app order by creation_date desc") {
    println("app_package=${it.app_package} ")

    orderedAppList.add(apps.get(it.app_package));
}


String appList = templateBuilder = TemplateBuilder.newFilePathTemplate(THEME_DIR, "applist.ftl")
        .addVariable("market", market)
        .addVariable("apps",orderedAppList)
        .addVariable("packages", apps.keySet())
        .addVariable("categories", categories)
        .build();

writeOutput(".", "index.html", appList)


LOG.info("Copy theme static resources")
Collection<File> files = FileUtils.listFiles(new File(THEME_DIR),marketFileFilter,folderFilter);
files.forEach( {
    LOG.info("Copy theme file {}", it)
    copyFileToOutput(new File(THEME_DIR), it, "");
})

LOG.info("Generating SEO files")
RobotsTxtService robotsTxtService = ctx.getBean(RobotsTxtService.class)
String reobotsTxt = robotsTxtService.buildsRobotsFile();
writeOutput(".", "robots.txt", reobotsTxt)

def writeOutput(String path, String file, String content) {
    new File(OUTPUT_DIR + "/" + path).mkdirs();
    File outputFile = new File(OUTPUT_DIR + "/" + path + "/" + file)
    outputFile.text = content;
    return outputFile;
}

def copyFileToOutput(File inFilePathParent, File inFile, String outputFolder) {
    File outputFolderFile = new File(OUTPUT_DIR + "/" + outputFolder)
    outputFolderFile.mkdirs();

    String relativePath = StringUtils.replace(inFile.getParentFile().getAbsolutePath(), inFilePathParent.getAbsolutePath(), "");
    File copyLocation = new File(outputFolderFile.getAbsolutePath() + relativePath);
    copyLocation.mkdirs();

    FileUtils.copyFileToDirectory(inFile, copyLocation);
}
