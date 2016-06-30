package com.rackspace.papi.tests

import javax.xml.transform._
import javax.xml.transform.stream._
import javax.xml.transform.dom._

import com.rackspace.cloud.api.wadl.Converters._

import net.sf.saxon.TransformerFactoryImpl

import scala.xml._

object TemplateTest {
  //
  //  Use SAXON HE (for extra credit you can have a config that lets users choose SAXON EE if they have a license.
  //  SAXON EE compiles stylesheets directly to bytecode so it's faster, HE essentially implements an interpreter)
  //
  val saxonTransformFactory = {
    val f = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", this.getClass.getClassLoader)
    val cast = f.asInstanceOf[TransformerFactoryImpl]
    cast.getConfiguration.getDynamicLoader.setClassLoader(this.getClass.getClassLoader)
    f
  }

  //
  //  Compile the remove-element transform...
  //
  val setupTemplate = saxonTransformFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/remove-element.xsl").toString))

  def removeItems (xpath : String, /* The XPATH as a string */
                   namespaces : Map[String, String], /* Namespaces prefix -> URI */
                   source : Source, /* The XML Source */
                   result : Result  /* The XML Result -- with items removed */
                 ) = {

    //
    //  This bit should be done at start up only, we're creating and
    //  compiling a new xslt based on the xpath and namespaces.
    //
    val setupTransformer = setupTemplate.newTransformer
    setupTransformer.setParameter("xpath", xpath)
    setupTransformer.setParameter("namespaces", new StreamSource(
      <namespaces xmlns="http://www.rackspace.com/repose/params">
         {
           for ((prefix, uri) <- namespaces) yield
             <ns prefix={prefix} uri={uri}/>
         }
      </namespaces>
    ))
    val removeXPathXSLTDomResult = new DOMResult()
    setupTransformer.transform (new StreamSource(<ignore-input />), removeXPathXSLTDomResult)

    //
    //  We now get saxon to compile the xslt we generated...
    //
    val removeXPathTransform = saxonTransformFactory.newTemplates(new DOMSource(removeXPathXSLTDomResult.getNode()))


    //
    //  This bit should be done at every request.....

    //  Note that we are create a new tranformer here. It's important
    //  to note that transformes are *NOT* threadsafe.  However
    //  transforms can be reused.

    //  If you want to be super efficent you can pool tranformers
    //  which is what api-checker does. There's a transformer pool
    //  here that you can borrow from the util package:
    //  https://github.com/Rackerlabs/api-checker/blob/master/core/src/main/scala/com/rackspace/com/papi/components/checker/util/TransformPool.scala
    //

    removeXPathTransform.newTransformer.transform(source, result)
  }
}
