package wikimediasampler.producer

import io.circe.derivation.*

given Configuration = Configuration.default

// event: message
// id: [{"topic":"eqiad.mediawiki.recentchange","partition":0,"timestamp":1685006633001},{"topic":"codfw.mediawiki.recentchange","partition":0,"offset":-1}]
// data: {"$schema":"/mediawiki/recentchange/1.0.0","meta":{"uri":"https://ce.wikipedia.org/wiki/%D0%9C%D0%B0%D0%BA%D0%B0%D1%80%D0%BE%D0%B2%D1%81%D0%BA%D0%B8_(%D0%98%D0%B2%D0%B0%D0%BD%D0%BE%D0%B2%D0%BD_%D0%BE%D0%B1%D0%BB%D0%B0%D1%81%D1%82%D1%8C)","request_id":"d02b2497-05e4-433b-8a15-4de0b8bc20ee","id":"5692c611-972f-45ed-ae7b-5808f3bf6c7c","dt":"2023-05-25T09:23:53Z","domain":"ce.wikipedia.org","stream":"mediawiki.recentchange","topic":"eqiad.mediawiki.recentchange","partition":0,"offset":4653778330},"id":64150348,"type":"edit","namespace":0,"title":"Макаровски (Ивановн область)","comment":"/* Климат */clean up, replaced: Кхузахь климат барамера континенталан ю. Цуьнан амалехь ду гӀеххьа йовха аьхке а, барамера шийла Ӏа а using [[Project:AWB|AWB]]","timestamp":1685006633,"user":"CheWikibot","bot":true,"minor":true,"length":{"old":4887,"new":4889},"revision":{"old":9787967,"new":9869291},"server_url":"https://ce.wikipedia.org","server_name":"ce.wikipedia.org","server_script_path":"/w","wiki":"cewiki","parsedcomment":"<span dir=\"auto\"><span class=\"autocomment\"><a href=\"/wiki/%D0%9C%D0%B0%D0%BA%D0%B0%D1%80%D0%BE%D0%B2%D1%81%D0%BA%D0%B8_(%D0%98%D0%B2%D0%B0%D0%BD%D0%BE%D0%B2%D0%BD_%D0%BE%D0%B1%D0%BB%D0%B0%D1%81%D1%82%D1%8C)#Климат\" title=\"Макаровски (Ивановн область)\">→‎Климат</a>: </span>clean up, replaced: Кхузахь климат барамера континенталан ю. Цуьнан амалехь ду гӀеххьа йовха аьхке а, барамера шийла Ӏа а using <a href=\"/w/index.php?title=%D0%92%D0%B8%D0%BA%D0%B8%D0%BF%D0%B5%D0%B4%D0%B8:AWB&amp;action=edit&amp;redlink=1\" class=\"new\" title=\"Википеди:AWB (иштта агӀо йоцуш йу)\">AWB</a></span>"}

/** Partial representation of the MediaWiki RecentChange event according to schema at:
* https://schema.wikimedia.org/repositories/primary/jsonschema/mediawiki/recentchange/1.0.0
*/
final case class WikiMediaMessage(
    title: Option[String],
    `type`: Option[String],
    bot: Option[Boolean],
    comment: Option[String],
    id: Option[Long],
    length: Option[Length],
    meta: Meta,
    namespace: Option[Int],
    revision: Option[Revision],
    timestamp: Option[Int],
    user: Option[String],
    wiki: Option[String]
) derives ConfiguredCodec

/** Length of old and new change
*/
final case class Length(
    `new`: Option[Int],
    old: Option[Int]
) derives ConfiguredCodec

/** @param domain
*   Domain the event or entity pertains to
* @param dt
*   UTC event datetime, in ISO-8601 format
* @param id
*   Unique ID of this event
* @param request_id
*   Unique ID of the request that caused the event
* @param stream
*   Name of the stream/queue/dataset that this event belongs in
* @param uri
*   Unique URI identifying the event or entity
*/
final case class Meta(
    domain: Option[String],
    dt: String,
    id: Option[String],
    request_id: Option[String],
    stream: String,
    uri: Option[String]
) derives ConfiguredCodec

final case class Revision(
    `new`: Option[Int],
    old: Option[Int]
) derives ConfiguredCodec
