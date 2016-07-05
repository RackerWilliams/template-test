package com.rackspace.papi.tests

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers._

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.test._

import java.io.ByteArrayOutputStream
import javax.xml.transform.stream._


@RunWith(classOf[JUnitRunner])
class TemplateTestSpec extends BaseWADLSpec {

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("foo", "http://www.foo.com/foo")
  register ("bar", "http://www.bar.com/bar")

  feature ("Remove link components") {
    scenario ("from element based on an index") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link>http://www.rackspace.com/v1.0/37778/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link", Map("foo" -> "http://www.foo.com/foo"),
                               Some(1), /* index */
                               None,    /* URI marker */
                               None,    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:link = 'http://www.rackspace.com/v1.0/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("from element based on a marker") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link>http://www.rackspace.com/v1.0/37778/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link", Map("foo" -> "http://www.foo.com/foo"),
                               None, /* index */
                               Some("v1.0"),    /* URI marker */
                               None,    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:link = 'http://www.rackspace.com/v1.0/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }


    scenario ("drop element based on a bad index") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link>http://www.rackspace.com/v1.0/37778/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link", Map("foo" -> "http://www.foo.com/foo"),
                               Some(55), /* index */
                               None,    /* URI marker */
                               None,    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"empty(/foo:foo/foo:link)")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("drop element based on a bad marker") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link>http://www.rackspace.com/v1.0/37778/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link", Map("foo" -> "http://www.foo.com/foo"),
                               None, /* index */
                               Some("WoogaBooga"),    /* URI marker */
                               None,    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"empty(/foo:foo/foo:link)")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("from attribute based on an index") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link href="http://www.rackspace.com/v1.0/37778/path/to/widget"/>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link/@href", Map("foo" -> "http://www.foo.com/foo"),
                               Some(1), /* index */
                               None,    /* URI marker */
                               None,    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:link/@href = 'http://www.rackspace.com/v1.0/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("from attribute based on a marker") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link href="http://www.rackspace.com/v1.0/37778/path/to/widget"/>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link/@href", Map("foo" -> "http://www.foo.com/foo"),
                               None, /* index */
                               Some("v1.0"),    /* URI marker */
                               None,    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:link/@href = 'http://www.rackspace.com/v1.0/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("drop attribute based on a bad index") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link href="http://www.rackspace.com/v1.0/37778/path/to/widget"/>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link/@href", Map("foo" -> "http://www.foo.com/foo"),
                               Some(55), /* index */
                               None,    /* URI marker */
                               None,    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"empty(/foo:foo/foo:link/@href)")
      assert (xmlOut,"/foo:foo/foo:link")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("drop attribute based on a bad marker") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link href="http://www.rackspace.com/v1.0/37778/path/to/widget"/>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link/@href", Map("foo" -> "http://www.foo.com/foo"),
                               None, /* index */
                               Some("WoogaBooga"),    /* URI marker */
                               None,    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"empty(/foo:foo/foo:link/@href)")
      assert (xmlOut,"/foo:foo/foo:link")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("from element and attribute based on an index") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link href="http://www.rackspace.com/v1.0/37778/path/to/widget">http://www.rackspace.com/v1.0/37778/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link|foo:link/@href", Map("foo" -> "http://www.foo.com/foo"),
                               Some(1), /* index */
                               None,    /* URI marker */
                               None,    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:link = 'http://www.rackspace.com/v1.0/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:link/@href = 'http://www.rackspace.com/v1.0/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("from element and attribute based on a marker") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link href="http://www.rackspace.com/v1.0/37778/path/to/widget">http://www.rackspace.com/v1.0/37778/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link|foo:link/@href", Map("foo" -> "http://www.foo.com/foo"),
                               None, /* index */
                               Some("v1.0"),    /* URI marker */
                               None,    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:link = 'http://www.rackspace.com/v1.0/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:link/@href = 'http://www.rackspace.com/v1.0/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }
  }


  feature ("Add link components") {
    scenario ("from element based on an index") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link>http://www.rackspace.com/v1.0/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link", Map("foo" -> "http://www.foo.com/foo"),
                               Some(1), /* index */
                               None,    /* URI marker */
                               Some("37778"), /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:link = 'http://www.rackspace.com/v1.0/37778/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("from element based on a marker") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link>http://www.rackspace.com/v1.0/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link", Map("foo" -> "http://www.foo.com/foo"),
                               None, /* index */
                               Some("v1.0"),    /* URI marker */
                               Some("37778"),    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:link = 'http://www.rackspace.com/v1.0/37778/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }


    scenario ("drop element based on bad index") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link>http://www.rackspace.com/v1.0/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link", Map("foo" -> "http://www.foo.com/foo"),
                               Some(55), /* index */
                               None,    /* URI marker */
                               Some("37778"), /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"empty(/foo:foo/foo:link)")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("drop element based on bad marker") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link>http://www.rackspace.com/v1.0/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link", Map("foo" -> "http://www.foo.com/foo"),
                               None, /* index */
                               Some("WoogaBooga"),    /* URI marker */
                               Some("37778"),    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"empty(/foo:foo/foo:link)")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("from attribute based on an index") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link href="http://www.rackspace.com/v1.0/path/to/widget"/>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link/@href", Map("foo" -> "http://www.foo.com/foo"),
                               Some(1), /* index */
                               None,    /* URI marker */
                               Some("37778"),    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:link/@href = 'http://www.rackspace.com/v1.0/37778/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("from attribute based on a marker") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link href="http://www.rackspace.com/v1.0/path/to/widget"/>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link/@href", Map("foo" -> "http://www.foo.com/foo"),
                               None, /* index */
                               Some("v1.0"),    /* URI marker */
                               Some("37778"),    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:link/@href = 'http://www.rackspace.com/v1.0/37778/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }


    scenario ("drop attribute based on a bad index") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link href="http://www.rackspace.com/v1.0/path/to/widget"/>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link/@href", Map("foo" -> "http://www.foo.com/foo"),
                               Some(55), /* index */
                               None,    /* URI marker */
                               Some("37778"),    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"empty(/foo:foo/foo:link/@href)")
      assert (xmlOut,"/foo:foo/foo:link")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("drop attribute based on a bad marker") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link href="http://www.rackspace.com/v1.0/path/to/widget"/>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link/@href", Map("foo" -> "http://www.foo.com/foo"),
                               None, /* index */
                               Some("WoogaBooga"),    /* URI marker */
                               Some("37778"),    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"empty(/foo:foo/foo:link/@href)")
      assert (xmlOut,"/foo:foo/foo:link")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("from element and attribute based on an index") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link href="http://www.rackspace.com/v1.0/path/to/widget">http://www.rackspace.com/v1.0/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link|foo:link/@href", Map("foo" -> "http://www.foo.com/foo"),
                               Some(1), /* index */
                               None,    /* URI marker */
                               Some("37778"),    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:link = 'http://www.rackspace.com/v1.0/37778/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:link/@href = 'http://www.rackspace.com/v1.0/37778/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("from element and attribute based on a marker") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link href="http://www.rackspace.com/v1.0/path/to/widget">http://www.rackspace.com/v1.0/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:link|foo:link/@href", Map("foo" -> "http://www.foo.com/foo"),
                               None, /* index */
                               Some("v1.0"),    /* URI marker */
                               Some("37778"),    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:link = 'http://www.rackspace.com/v1.0/37778/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:link/@href = 'http://www.rackspace.com/v1.0/37778/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }
  }

  feature ("fail on miss") {
    scenario ("should not fail on miss if feature not set") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link>http://www.rackspace.com/v1.0/37778/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.updateLinks("foo:missit", Map("foo" -> "http://www.foo.com/foo"),
                               Some(1), /* index */
                               None,    /* URI marker */
                               None,    /* new Component */
                               false,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:link = 'http://www.rackspace.com/v1.0/37778/path/to/widget'")
      assert (xmlOut,"/foo:foo/foo:baz")
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
    }

    scenario ("should fail on miss if feature is set") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link>http://www.rackspace.com/v1.0/37778/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      val thrown = intercept[Exception] {
      TemplateTest.updateLinks("foo:missit", Map("foo" -> "http://www.foo.com/foo"),
                               Some(1), /* index */
                               None,    /* URI marker */
                               None,    /* new Component */
                               true,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))
      }
      assert (thrown.getMessage() == "Could not match on XPATH \"foo:missit\"")
    }

  }


  feature ("fail on bad/missing params") {
    scenario ("should fail if neither index or marker is specified") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link>http://www.rackspace.com/v1.0/37778/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      val thrown = intercept[Exception] {
      TemplateTest.updateLinks("foo:missit", Map("foo" -> "http://www.foo.com/foo"),
                               None, /* index */
                               None,    /* URI marker */
                               None,    /* new Component */
                               true,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))
      }
      assert (thrown.getMessage() == "You must specify a uriIndex or a uriMarker")
    }


    scenario ("should fail if both index and mark are specificed") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <link>http://www.rackspace.com/v1.0/37778/path/to/widget</link>
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      val thrown = intercept[Exception] {
      TemplateTest.updateLinks("foo:missit", Map("foo" -> "http://www.foo.com/foo"),
                               Some(1), /* index */
                               Some("v1.0"), /* URI marker */
                               None,    /* new Component */
                               true,   /* Fail on miss */
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))
      }
      assert (thrown.getMessage() == "You cannot specify both uriIndex and uriMarker")
    }

  }

}
