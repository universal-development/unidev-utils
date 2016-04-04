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
 * 1.0.0
 * Script for logging into google webservices, login will require user action
 */

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory


// google services dependencies
@Grab('com.google.apis:google-api-services-webmasters:v3-rev15-1.21.0')
@Grab('com.google.apis:google-api-services-siteVerification:v1-rev45-1.21.0')


/**
 * Perform login request for provided scopes
 * Return google credentials object
 */
def GoogleCredential login(Collection<String> scopes)  {

    String CLIENT_ID = "client-id-here";
    String CLIENT_SECRET = "client-secret-here";

    String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();

    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, scopes).setAccessType("online").setApprovalPrompt("auto")
            .build();

    String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
    System.out.println("Google login url: ");
    System.out.println(url);
    System.out.println("Enter login code:");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String code = br.readLine();

    GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
    GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response);

    return credential;
}

