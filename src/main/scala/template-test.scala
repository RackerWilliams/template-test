package com.rackspace.papi.tests

import javax.xml.transform._
import javax.xml.transform.stream._
import javax.xml.transform.dom._

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.util.LogErrorListener

import net.sf.saxon.TransformerFactoryImpl
import net.sf.saxon.Controller

import org.xml.sax.SAXParseException

import scala.language.reflectiveCalls

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
    // Recover silently from recoverable errors. These may occur depending on XPath passed in.
    f.setAttribute("http://saxon.sf.net/feature/recoveryPolicyName","recoverSilently")
    f
  }

  //
  //  Compile the remove-element transform...
  //
  val setupTemplate = saxonTransformFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/remove-element.xsl").toString))

  val FIRST_INDEX : Int = 3

  def getIndex(in : Array[String], uriIndex : Option[Int], uriMarker : Option[String]) : Int = {
    if (uriIndex != None) {
      uriIndex.get+FIRST_INDEX
    } else {
      val idx = in.indexOf(uriMarker.get, FIRST_INDEX)
      if (idx == -1) {
        idx
      } else {
        idx+1
      }
    }
  }

  def removeComponent(in : String, uriIndex : Option[Int], uriMarker : Option[String]) : String = {
    val uriComponents = in.split("/")
    val index = getIndex(uriComponents, uriIndex, uriMarker)

    if (index == -1) {
      in
    } else {
      val a1 = uriComponents.slice (0, index)
      val a2 = uriComponents.slice (index+1, in.size)
      (a1 ++ a2).mkString("/")
    }
  }


  def addComponent(in : String, uriIndex : Option[Int], uriMarker : Option[String], newComp : String) : String = {
    val uriComponents = in.split("/")
    val index = getIndex(uriComponents, uriIndex, uriMarker)

    if (index == -1) {
      in
    } else {
      val a1 = uriComponents.slice (0, index)
      val a2 = Array(newComp)
      val a3 = uriComponents.slice(index, in.size)
      (a1 ++ a2 ++ a3).mkString("/")
    }
  }

  def updateLinks (xpath : String, /* The XPATH as a string */
                   namespaces : Map[String, String], /* Namespaces prefix -> URI */
                   uriIndex : Option[Int], /* An integer pointing to the index to replace or add */
                   uriMarker : Option[String], /* The marker after which you remove/add a URI component */
                   newComponent : Option[String], /* If set, we are adding the string as a new componet, otherwise we are removing */
                   failOnMiss : Boolean = false, /* Fail if the XPath does't match anything */
                   source : Source, /* The XML Source */
                   result : Result  /* The XML Result -- with items removed */
                 ) = {

    //
    //  Do a bit of error checking
    //
    if ((uriIndex == None) && (uriMarker == None)) {
      throw new SAXParseException("You must specify a uriIndex or a uriMarker", null)
    }
    if ((uriIndex != None) && (uriMarker != None)) {
      throw new SAXParseException("You cannot specify both uriIndex and uriMarker", null)
    }

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
    setupTransformer.setParameter("failOnMiss", failOnMiss)

    //
    //  Because the XSLT can produce errors now, we need to add an error listener.
    //  Here we use the one from WADL tools, it looks at the format of the message
    //  based on how it's written it logs the error using slf4j and if it's an actual
    //  error throws a SAXParseException
    //
    //  https://github.com/rackerlabs/wadl-tools/blob/master/src/main/scala/util/log-error-listener.scala

    setupTransformer.asInstanceOf[Controller].addLogErrorListener


    val updateXPathXSLTDomResult = new DOMResult()
    setupTransformer.transform (new StreamSource(<ignore-input />), updateXPathXSLTDomResult)

    //
    //  We now get saxon to compile the xslt we generated..., but
    //  before we do we insert knowledge of our custom scala call.
    //
    val config = saxonTransformFactory.asInstanceOf[TransformerFactoryImpl].getConfiguration()
    if (newComponent == None) {
      config.registerExtensionFunction(new StringExtension("process-url","rax", "http://docs.rackspace.com/api",
                                                         removeComponent(_,uriIndex,uriMarker)))
    } else {
      config.registerExtensionFunction(new StringExtension("process-url","rax", "http://docs.rackspace.com/api",
                                                         addComponent(_,uriIndex,uriMarker,newComponent.get)))
    }

    val updateXPathTransform = saxonTransformFactory.newTemplates(new DOMSource(updateXPathXSLTDomResult.getNode()))


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

    val updateXPathTransformer = updateXPathTransform.newTransformer
    updateXPathTransformer.asInstanceOf[Controller].addLogErrorListener
    updateXPathTransformer.transform (source, result)
  }
}
