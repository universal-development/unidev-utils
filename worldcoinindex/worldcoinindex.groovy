import com.unidev.platform.common.utils.RandomUtils
import com.unidev.platform.common.utils.StringUtils
import com.unidev.platform.components.http.HTTPClient
import com.unidev.platform.components.http.HTTPClientUtils
import com.unidev.platform.components.http.XTrustProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.support.ClassPathXmlApplicationContext

/**
 * Copyright (c) 2017 Denis O <denis@universal-development.com>
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
 * worldcoinindex parser
 */


@GrabResolver(name = 'decaf', root = 'http://ci.decafdev.local/nexus/content/groups/public')
@Grab('com.unidev.platform.spring:simple-config:2.0.0-SNAPSHOT')
@Grab('com.unidev.platform.components:http-client:2.0.0-SNAPSHOT')

def ctx = new ClassPathXmlApplicationContext("classpath:/unidev-simple.xml");

Logger log = LoggerFactory.getLogger("")

StringUtils stringUtils = ctx.getBean(StringUtils.class)
XTrustProvider.install()

HTTPClient httpClient = ctx.getBean(HTTPClient.class);
httpClient.init(HTTPClientUtils.USER_AGENTS)


log.info("Downloading page...")

String page = httpClient.get("https://www.worldcoinindex.com/coin/bitcoincash")

log.info("{}", page)