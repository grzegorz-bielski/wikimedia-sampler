package wikimediasampler

import io.circe.literal.*
import io.circe.syntax.*

import WikiMediaClient.*

class WikiMediaSerialisationSpec extends munit.FunSuite:
  test("WikiMediaMessage should be decoded successfully"):
    assert(messages.forall(_.as[WikiMediaMessage].isRight))

  val messages = Vector(
    json"""
        {
            "$$schema": "/mediawiki/recentchange/1.0.0",
            "meta": {
                "uri": "https://war.wikipedia.org/wiki/Kaarangay:CS1_errors:_dates",
                "request_id": "d98ebf1e-7f25-4159-b50b-d772c7ca1d5e",
                "id": "347db172-0fc0-4f1c-9637-dc858e3ecd0e",
                "dt": "2023-05-25T13:39:42Z",
                "domain": "war.wikipedia.org",
                "stream": "mediawiki.recentchange",
                "topic": "eqiad.mediawiki.recentchange",
                "partition": 0,
                "offset": 4654276312
            },
            "id": 30732158,
            "type": "categorize",
            "namespace": 14,
            "title": "Kaarangay:CS1 errors: dates",
            "comment": "[[:Allocosmia sugii]] removed from category, [[Special:WhatLinksHere/Allocosmia sugii|this page is included within other pages]]",
            "timestamp": 1685021982,
            "user": "Rich Farmbrough",
            "bot": false,
            "server_url": "https://war.wikipedia.org",
            "server_name": "war.wikipedia.org",
            "server_script_path": "/w",
            "wiki": "warwiki",
            "parsedcomment": "<a href=\"/wiki/Allocosmia_sugii\" title=\"Allocosmia sugii\">Allocosmia sugii</a> removed from category, <a href=\"/wiki/Pinaurog:AnoAnNasumpayDinhi/Allocosmia_sugii\" title=\"Pinaurog:AnoAnNasumpayDinhi/Allocosmia sugii\">this page is included within other pages</a>"
        }
    """,
    json"""
        {
            "$$schema": "/mediawiki/recentchange/1.0.0",
            "meta": {
                "uri": "https://commons.wikimedia.org/wiki/File:Wissensturm_Linz,_Eingangsbereich_-_Panorama_(06).jpg",
                "request_id": "ed385c0d-116e-4a58-b31e-e60ea995db48",
                "id": "c118d26b-d9a7-45bf-a25a-32a6821adf66",
                "dt": "2023-05-25T14:10:53Z",
                "domain": "commons.wikimedia.org",
                "stream": "mediawiki.recentchange",
                "topic": "eqiad.mediawiki.recentchange",
                "partition": 0,
                "offset": 4654334057
            },
            "id": 2202323770,
            "type": "log",
            "namespace": 6,
            "title": "File:Wissensturm Linz, Eingangsbereich - Panorama (06).jpg",
            "comment": "== {{int:filedesc}} ==\n{{Information\n|Description={{de|1=[[:de:Wissensturm|Wissensturm]], [[:de:Linz|Linz]] - Eingangsbereich - Panorama.}} \n|Source={{own}}\n|Date=2023-05-13\n|Author=© [[:de:Benutzer:1971markus|1971markus]]\n|Permission={{self|Cc-by-sa-4.0|author=© [[:de:User:1971markus|1971markus]]|attribution=© 1971markus@wikipedia.de / <small>[https://creativecommons.org/licenses/by-sa/4.0/deed.de Cc-by-sa-4.0]</small>}}\n|other_versions=\n<gallery style=\"float:left;\" mode=\"packed-hover\" >\nWissensturm Linz, Eingangsbereich - Panorama (01).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (02).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (03).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (04).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (05).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (07).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (08).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (09).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (10).jpg|Wissensturm Linz, Eingangsbereich\n</gallery>\n}}\n\n{{Pano360}}\n\n{{User:1971markus/licence}}\n\n[[Category:Wissensturm]]\n[[Category:360° panoramas of Austria]]\n[[Category:2023 in Linz]]\n[[Category:Taken with Xiaomi Yi 360]]",
            "timestamp": 1685023853,
            "user": "1971markus",
            "bot": false,
            "log_id": 339161257,
            "log_type": "upload",
            "log_action": "upload",
            "log_params": {
            "img_sha1": "n7p91uyt4hcqqq8s0snqrsfdfc75xfx",
            "img_timestamp": "20230525141053"
            },
            "log_action_comment": "uploaded &quot;[[File:Wissensturm Linz, Eingangsbereich - Panorama (06).jpg]]&quot;: == {{int:filedesc}} ==\n{{Information\n|Description={{de|1=[[:de:Wissensturm|Wissensturm]], [[:de:Linz|Linz]] - Eingangsbereich - Panorama.}} \n|Source={{own}}\n|Date=2023-05-13\n|Author=© [[:de:Benutzer:1971markus|1971markus]]\n|Permission={{self|Cc-by-sa-4.0|author=© [[:de:User:1971markus|1971markus]]|attribution=© 1971markus@wikipedia.de / <small>[https://creativecommons.org/licenses/by-sa/4.0/deed.de Cc-by-sa-4.0]</small>}}\n|other_versions=\n<gallery style=\"float:left;\" mode=\"packed-hover\" >\nWissensturm Linz, Eingangsbereich - Panorama (01).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (02).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (03).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (04).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (05).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (07).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (08).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (09).jpg|Wissensturm Linz, Eingangsbereich\nWissensturm Linz, Eingangsbereich - Panorama (10).jpg|Wissensturm Linz, Eingangsbereich\n</gallery>\n}}\n\n{{Pano360}}\n\n{{User:1971markus/licence}}\n\n[[Category:Wissensturm]]\n[[Category:360° panoramas of Austria]]\n[[Category:2023 in Linz]]\n[[Category:Taken with Xiaomi Yi 360]]",
            "server_url": "https://commons.wikimedia.org",
            "server_name": "commons.wikimedia.org",
            "server_script_path": "/w",
            "wiki": "commonswiki",
            "parsedcomment": "== {{int:filedesc}} == {{Information |Description={{de|1=<a href=\"https://de.wikipedia.org/wiki/Wissensturm\" class=\"extiw\" title=\"de:Wissensturm\">Wissensturm</a>, <a href=\"https://de.wikipedia.org/wiki/Linz\" class=\"extiw\" title=\"de:Linz\">Linz</a> - Eingangsbereich - Panorama.}}  |Source={{own}} |Date=2023-05-13 |Author=© <a href=\"https://de.wikipedia.org/wiki/Benutzer:1971markus\" class=\"extiw\" title=\"de:Benutzer:1971markus\">1971markus</a> |Permission={{self|Cc-by-sa-4.0|author=© <a href=\"https://de.wikipedia.org/wiki/User:1971markus\" class=\"extiw\" title=\"de:User:1971markus\">1971markus</a>|attribution=© 1971markus@wikipedia.de / &lt;small&gt;[https://creativecommons.org/licenses/by-sa/4.0/deed.de Cc-by-sa-4.0]&lt;/small&gt;}} |other_versions= &lt;gallery style=&quot;float:left;&quot; mode=&quot;packed-hover&quot; &gt; Wissensturm Linz, Eingangsbereich - Panorama (01).jpg|Wissensturm Linz, Eingangsbereich Wissensturm Linz, Eingangsbereich - Panorama (02).jpg|Wissensturm Linz, Eingangsbereich Wissensturm Linz, Eingangsbereich - Panorama (03).jpg|Wissensturm Linz, Eingangsbereich Wissensturm Linz, Eingangsbereich - Panorama (04).jpg|Wissensturm Linz, Eingangsbereich Wissensturm Linz, Eingangsbereich - Panorama (05).jpg|Wissensturm Linz, Eingangsbereich Wissensturm Linz, Eingangsbereich - Panorama (07).jpg|Wissensturm Linz, Eingangsbereich Wissensturm Linz, Eingangsbereich - Panorama (08).jpg|Wissensturm Linz, Eingangsbereich Wissensturm Linz, Eingangsbereich - Panorama (09).jpg|Wissensturm Linz, Eingangsbereich Wissensturm Linz, Eingangsbereich - Panorama (10).jpg|Wissensturm Linz, Eingangsbereich &lt;/gallery&gt; }}  {{Pano360}}  {{User:1971markus/licence}}  <a href=\"/wiki/Category:Wissensturm\" title=\"Category:Wissensturm\">Category:Wissensturm</a> <a href=\"/wiki/Category:360%C2%B0_panoramas_of_Austria\" title=\"Category:360° panoramas of Austria\">Category:360° panoramas of Austria</a> <a href=\"/wiki/Category:2023_in_Linz\" title=\"Category:2023 in Linz\">Category:2023 in Linz</a> <a href=\"/wiki/Category:Taken_with_Xiaomi_Yi_360\" title=\"Category:Taken with Xiaomi Yi 360\">Category:Taken with Xiaomi Yi 360</a>"
        }  
    """
  )
