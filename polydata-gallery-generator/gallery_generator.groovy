/**
 * Copyright (c) 2017 Denis O <denis.o@linux.com>
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
 * Script to produce gallery files from folder "input" to "output".
 * Produced collection contain scalled thumbnail images in separated folders.
 * Required programs: ImageMagic convert
 */

import com.unidev.polydata.FlatFileStorage
import com.unidev.polydata.FlatFileStorageMapper

@Grab('com.unidev.polydata:polydata-flat-file-storage:2.2.0')

import groovy.json.*

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.util.Random
import com.unidev.polydata.domain.BasicPoly;
import com.unidev.polydata.domain.BasicPolyList;
import com.unidev.polydata.domain.bucket.BasicPolyBucket;


Random rand = new Random();

def thumbnailWidth=350
def imageWidth=1024

def output = "output"

println "Scanning input folder"

FlatFileStorage index = new FlatFileStorage();

def folders = new File("input").listFiles();
folders.sort();

for(File file : folders) { 
    def name = file.getName()
    println "name : $name"

    FlatFileStorage gallery = new FlatFileStorage();
    gallery.metadata()['title'] = name.replaceAll("_", " ").capitalize();
    gallery.metadata()['dir'] = name

    def outDir = "output/$name"
    new File(outDir).mkdirs();

    new File("$outDir/imgs").mkdirs();
    new File("$outDir/thumbs").mkdirs();

    def files = new File("input/$name").listFiles()
    files.sort();
    println files

    gallery.metadata()["count"]=files.length

    // generate cover
    def coverSource = files[0]

    def command = ["convert", "$coverSource", "-resize", "$thumbnailWidth", "$outDir/thumb.jpg"]
    exec(command)

    gallery.metadata()["thumb"]="thumb.jpg"

    // convert all images

    for(scaleImage in files) {
        def extension = scaleImage.name.tokenize('.').last()
        def destFile = cleanFile(scaleImage.getName()) + "." + extension;
        println "scaleImage: " +scaleImage
        println "file: " +destFile

        command = ["convert", "$scaleImage", "-resize", "$imageWidth", "$outDir/imgs/$destFile"]
        exec(command)

        command = ["convert", "$scaleImage" ,"-resize", "$thumbnailWidth" ,"$outDir/thumbs/$destFile"]
        exec(command)

        BasicPoly basicPoly = new BasicPoly();
        basicPoly.put("_id", destFile);
        basicPoly.put("img", "imgs/" + destFile);
        basicPoly.put("thumb", "thumbs/" + destFile);

        gallery.polys().add(basicPoly);
    }
    new FlatFileStorageMapper().saveSource(new File("$outDir/index.json")).save(gallery)
    BasicPoly galleryPoly = BasicPoly.newPoly("$name/index.json");
    galleryPoly.put("metadata", gallery.metadata());
    galleryPoly.put("polys", gallery.polys());
    index.polys().add(galleryPoly)

}

new FlatFileStorageMapper().saveSource(new File("$output/index.json")).save(index)

def cleanFile(file) {
    def dictionary = "1234567890abcdefgh"
    def random = new java.security.SecureRandom(file.getBytes())
    int count = random.nextInt(5) + 3
    def name = "";
    for(int i = 0;i<count;i++) {
        name = name + dictionary.charAt(random.nextInt( dictionary.length() ))
    }
    return name;
}

def exec(command) {
    def proc = command.execute()
    proc.waitFor()
}


