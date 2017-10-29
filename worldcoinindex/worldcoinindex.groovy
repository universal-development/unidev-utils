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

HTTPClient httpClient = ctx.getBean(HTTPClient.class)
httpClient.init(HTTPClientUtils.USER_AGENTS)

log.info("Downloading page...")

String page = httpClient.get("https://www.worldcoinindex.com/coin/bitcoincash")
page = stringUtils.cleanPage(page)


String market = stringUtils.substringBetween(page, "<table id=\"market-table\"", "</table>")

String coinPrice = StringUtils.substringBetween(market, "<td class=\"coinprice\" style=\"font-size: 18pt; width: 160px;\">", "</td>" )
String coinPercentage = StringUtils.substringBetween(market, "<td class=\"coin-percentage\" style=\"font-size: 18pt; color: green; padding-right: 10px;\"><span>", "</span>" )
String coinHigh = StringUtils.substringBetween(market, "<td class=\"coin-high\">", "</span>" )
String coinLow = StringUtils.substringBetween(market, "<td class=\"col-hide-coin-info coin-low\">", "</span>" )


log.info("coinPrice {}", clean(stringUtils, coinPrice))
log.info("coinPercentage {}", clean(stringUtils, coinPercentage))
log.info("coinHigh {}", clean(stringUtils, coinHigh))
log.info("coinLow {}", clean(stringUtils, coinLow))

String markets = stringUtils.substringBetween(page, "<div class=\"coinmarketname\"><span>bitcoincash Markets</span></div>", "</table>")
log.info("{}", markets)
List records = []

while (true) {
    String record = stringUtils.substringBetween(markets, "data-symbol=", "</tr>")
    if (stringUtils.isBlank(record)) {
        break;
    }
    markets = markets.replace("data-symbol=" + record, "")
    String symbol = stringUtils.substringBetween(record, "\"", "\"")
    String pair = stringUtils.substringBetween(record, "\"mob-pair\">", "</td>")
    String exchangeName = stringUtils.substringBetween(record, "rel=\"nofollow\">", "</a>")
    String echangeUrl = stringUtils.substringBetween(record, "href=\"", "\"")
    String price = stringUtils.substringBetween(record, "class=\"number price\" style=\"text-align: right;\">", "</td>")
    String volume = stringUtils.substringBetween(record, "class=\"markets-volume\" style=\"text-align: right;\">", "</td>")

    records.add([ symbol: clean(stringUtils, symbol),
                  exchange: clean(stringUtils, exchangeName),
                  exchangeUrl: clean(stringUtils, echangeUrl),
                  pair: clean(stringUtils, pair),
                  price: clean(stringUtils, price),
                  volume: clean(stringUtils, volume),
            ])
}

records.each({
    log.info("===")
    log.info("symbol: {}", it.symbol)
    log.info("exchange: {}", it.exchange)
    log.info("exchangeUrl: {}", it.exchangeUrl)
    log.info("pair: {}", it.pair)
    log.info("price: {}", it.price)
    log.info("24h volume: {}", it.volume)
})

def clean(stringUtils, str) {
    return stringUtils.isBlank(str) ? "" : stringUtils.replace(stringUtils.cleanPage(str), "\r", "")
}
