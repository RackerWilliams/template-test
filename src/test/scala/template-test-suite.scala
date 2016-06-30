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

  feature ("Remove XPATH elements...") {
    scenario ("element name") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.removeItems("foo:baz",Map("foo" -> "http://www.foo.com/foo"),
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
      assert (xmlOut,"empty(/foo:foo/foo:baz)")
    }


    scenario ("full path") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.removeItems("/foo:foo/foo:baz",Map("foo" -> "http://www.foo.com/foo"),
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
      assert (xmlOut,"empty(/foo:foo/foo:baz)")
    }

    scenario ("multiple element names") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.removeItems("foo:baz|foo:bang",Map("foo" -> "http://www.foo.com/foo"),
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"empty(/foo:foo/foo:bang)")
      assert (xmlOut,"empty(/foo:foo/foo:baz)")
    }


    scenario ("no match...(differnt namespace)") {
      val inInput = <foo xmlns="http://www.foo.com/foo">
           <baz />
           <biz />
           <bang />
      </foo>
      val bytesOut = new ByteArrayOutputStream()

      TemplateTest.removeItems("foo:baz|foo:bang",Map("foo" -> "http://www.foo.com/foo/v2.0"),
                               new StreamSource(inInput),
                               new StreamResult(bytesOut))

      val xmlOut = XML.loadString(bytesOut.toString())
      assert (xmlOut,"/foo:foo/foo:biz")
      assert (xmlOut,"/foo:foo/foo:bang")
      assert (xmlOut,"/foo:foo/foo:baz")
    }

  }


}
