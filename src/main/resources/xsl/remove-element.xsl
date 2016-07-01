<xsl:transform version="2.0"
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:xslout="http://www.rackspace.com/xslout"
               xmlns:xs="http://www.w3.org/2001/XMLSchema"
               xmlns:param="http://www.rackspace.com/repose/params"
               exclude-result-prefixes="param">

    <!-- The XPATH to inject -->
    <xsl:param name="xpath" as="xs:string"/>

    <!-- A list of namespaces to add to the generated XSL -->
    <xsl:param name="namespaces" as="node()">
        <param:namespaces>
            <param:ns prefix="foo" uri="http://www.foo.com/foo"/>
            <param:ns prefix="bar" uri="http://www.bar.com/bar"/>
        </param:namespaces>
    </xsl:param>

    <!-- The zero based index of the uri compenent to remove/add -->
    <xsl:param name="uriIndex"   as="xs:integer?"/>

    <!-- The marker after which you remove/add the URI component -->
    <xsl:param name="uriMarker"  as="xs:string?"/>

    <!-- If set we are adding a new componet, if not set we are removing the component -->
    <xsl:param name="newComponent" as="xs:string?"/>

    <!-- Fail if the XPath doesn't match anything? -->
    <xsl:param name="failOnMiss" as="xs:boolean" select="false()"/>

    <!-- The index of the first path compenent if you tokenize an absolute URL by '/' -->
    <xsl:variable name="FIRST_INDEX" as="xs:integer" select="4"/>

    <!-- When you see xslout actually output xsl -->
    <xsl:namespace-alias stylesheet-prefix="xslout" result-prefix="xsl"/>

    <xsl:template match="/">
        <!-- Some error checking -->
        <xsl:choose>
            <xsl:when test="empty($uriIndex) and empty($uriMarker)">
                <xsl:message terminate="yes">[SE] You must specify a uriIndex or a uriMarker</xsl:message>
            </xsl:when>
            <xsl:when test="$uriIndex and $uriMarker">
                <xsl:message terminate="yes">[SE] You cannot specify both uriIndex and uriMarker</xsl:message>
            </xsl:when>
        </xsl:choose>

        <!-- generate the actual xsl -->
        <xslout:transform version="2.0">
            <xsl:apply-templates mode="ns" select="$namespaces"/>


            <xsl:if test="$failOnMiss">
                <xslout:template match="/">
                    <xslout:variable name="matches" as="xs:string*">
                        <xslout:apply-templates select="node()" mode="matches"/>
                    </xslout:variable>
                    <xslout:if test="empty($matches)">
                        <xslout:message terminate="yes">[SE] Could not match on XPATH "<xsl:value-of select="$xpath"/>"</xslout:message>
                    </xslout:if>
                    <xslout:copy>
                        <xslout:apply-templates select="node()"/>
                    </xslout:copy>
                </xslout:template>
                <xslout:template match="@*|node()" mode="matches"><xslout:apply-templates select="@*|node()" mode="matches"/></xslout:template>
                <xslout:template match="{$xpath}" mode="matches">yes</xslout:template>
            </xsl:if>

            <!-- Copy everything -->
            <xslout:template match="@*|node()">
                <xslout:copy>
                    <xslout:apply-templates select="@*|node()"/>
                </xslout:copy>
            </xslout:template>

            <!--
                Modify things
            -->
            <xslout:template match="{$xpath}">
                <xslout:variable name="URIComponents" as="xs:string*" select="tokenize(.,'/')"/>
                <xsl:variable name="index" as="xs:string">
                    <xsl:variable name="idx" as="xs:string*">
                        <xsl:choose>
                            <xsl:when test="not(empty($uriIndex))"><xsl:value-of select="$uriIndex + $FIRST_INDEX"/></xsl:when>
                            <xsl:otherwise>index-of($URIComponents,'<xsl:value-of select="$uriMarker"/>')[. >= <xsl:value-of select="$FIRST_INDEX"/>][1] + 1</xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:value-of select="normalize-space(string-join($idx,''))"/>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="empty($newComponent)">
                        <xslout:variable name="newComponents" as="xs:string*" select="remove($URIComponents, {$index})"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xslout:variable name="newComponents" as="xs:string*" select="insert-before($URIComponents, {$index},'{$newComponent}')"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xslout:choose>
                    <xslout:when test=". instance of attribute()">
                        <xslout:attribute>
                            <xsl:attribute name="name">{local-name()}</xsl:attribute>
                            <xslout:value-of select="string-join($newComponents,'/')"/>
                        </xslout:attribute>
                    </xslout:when>
                    <xslout:otherwise>
                        <xslout:copy>
                            <xslout:apply-templates select="@*"/>
                            <xslout:value-of select="string-join($newComponents,'/')"/>
                        </xslout:copy>
                    </xslout:otherwise>
                </xslout:choose>
            </xslout:template>
        </xslout:transform>
    </xsl:template>

    <xsl:template match="text()" mode="ns"/>
    <xsl:template match="param:ns" mode="ns">
        <xsl:namespace name="{@prefix}" select="@uri"/>
    </xsl:template>
</xsl:transform>
