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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.siteVerification.SiteVerification
import com.google.api.services.siteVerification.SiteVerificationScopes
import com.google.api.services.siteVerification.model.SiteVerificationWebResourceGettokenRequest
import com.google.api.services.siteVerification.model.SiteVerificationWebResourceGettokenResponse
import com.google.api.services.webmasters.Webmasters
import com.google.api.services.webmasters.WebmastersScopes

/**
 * 1.0.0
 * Script for batch adding files to google webmaster console and extract verification meta token in separated file
 * input: sites.txt - line by line domains
 * output: verifications/<domain> - meta verification code
 */

@Grab('com.google.apis:google-api-services-webmasters:v3-rev15-1.21.0')
@Grab('com.google.apis:google-api-services-siteVerification:v1-rev45-1.21.0')
@Grab('org.apache.commons:commons-lang3:3.4')
@Grab('commons-io:commons-io:2.4')

GoogleCredential credential = new google_login().login(Arrays.asList(WebmastersScopes.WEBMASTERS, SiteVerificationScopes.SITEVERIFICATION))

HttpTransport httpTransport = new NetHttpTransport();
JsonFactory jsonFactory = new JacksonFactory();

println "Processing..."

Webmasters webmasters = new Webmasters.Builder(httpTransport, jsonFactory, credential)
        .setApplicationName("Webmaster tools client")
        .build();

SiteVerification verification = new SiteVerification.Builder(httpTransport, jsonFactory, credential).setApplicationName("Verification").build();

Set<String> domains = new HashSet<>();

new File("sites.txt").eachLine { String line ->
    line = line.trim();
    if (line.isEmpty()) return;
    domains.add(line)
}

println "Processing sites..."

for(String domain : domains) {

    System.out.println(domain + "")

    Webmasters.Sites.Add addRequest = webmasters.sites().add("http://" + domain);
    addRequest.execute()

    def token = fetchToken(domain, verification);

    new File("verifications/" + domain).text = token
}

println "Done..."


def fetchToken(String siteUrl, SiteVerification siteVerification) {

    def String META_VERIFICATION_METHOD = "meta";
    def String SITE_TYPE = "SITE";

    SiteVerificationWebResourceGettokenRequest request =
            new SiteVerificationWebResourceGettokenRequest();
    request.setVerificationMethod(META_VERIFICATION_METHOD);
    request.setSite(new SiteVerificationWebResourceGettokenRequest.Site().setIdentifier(siteUrl).setType(SITE_TYPE));
    SiteVerificationWebResourceGettokenResponse response =
            siteVerification.webResource().getToken(request).execute();
    return response.getToken();
}