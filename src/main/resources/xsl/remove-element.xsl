<xsl:transform version="2.0"
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:xslout="http://www.rackspace.com/xslout"
               xmlns:xs="http://www.w3.org/2001/XMLSchema"
               xmlns:param="http://www.rackspace.com/repose/params"
               xmlns:rax="http://docs.rackspace.com/api"
               exclude-result-prefixes="param">

    <!-- The XPATH to inject -->
    <xsl:param name="xpaths" as="node()">
        <param:xpaths>
            <param:xpath>/foo:foo/@href</param:xpath>
            <param:xpath>/bar:bar/@href</param:xpath>
        </param:xpaths>
    </xsl:param>

    <!-- A list of namespaces to add to the generated XSL -->
    <xsl:param name="namespaces" as="node()">
        <param:namespaces>
            <param:ns prefix="foo" uri="http://www.foo.com/foo"/>
            <param:ns prefix="bar" uri="http://www.bar.com/bar"/>
        </param:namespaces>
    </xsl:param>

    <!-- Fail if the XPath doesn't match anything? -->
    <xsl:param name="failOnMiss" as="xs:boolean" select="false()"/>

    <!-- DROP ELEMENT CODE: if the result of rax:process-url is this we drop what's in the path -->
    <xsl:variable name="DROPCODE" as="xs:string" select="'[[DROP]]'"/>

    <!-- When you see xslout actually output xsl -->
    <xsl:namespace-alias stylesheet-prefix="xslout" result-prefix="xsl"/>

    <xsl:template match="/">
        <!-- generate the actual xsl -->
        <xslout:transform version="2.0">
            <xsl:apply-templates mode="ns" select="$namespaces"/>


            <xsl:if test="$failOnMiss">
                <xsl:variable name="allPaths" as="xs:string*" select="$xpaths//param:xpath"/>
                <xsl:variable name="allPathsList" as="xs:string"><xsl:value-of separator=", " select="for $p in $allPaths return concat(&quot;&apos;&quot;,$p,&quot;&apos;&quot;)"/></xsl:variable>
                <xsl:variable name="allPathColStr" as="xs:string" select="concat('(',$allPathsList,')')"/>
                <xslout:template match="/">
                    <xslout:variable name="matches" as="xs:string*">
                        <xslout:apply-templates select="node()" mode="matches"/>
                    </xslout:variable>
                    <xslout:if test="count($matches) &lt; {count($allPaths)}">
                        <xslout:message terminate="yes">[SE] Could not match on XPATH(s) <xslout:value-of separator=", " select="for $m in {$allPathColStr} return if ($m = $matches) then () else concat('&quot;',$m,'&quot;')"/></xslout:message>
                    </xslout:if>
                    <xslout:copy>
                        <xslout:apply-templates select="node()"/>
                    </xslout:copy>
                </xslout:template>
                <xslout:template match="@*|node()" mode="matches"><xslout:apply-templates select="@*|node()" mode="matches"/></xslout:template>
                <xsl:apply-templates mode="failOnMiss" select="$xpaths"/>
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
            <xsl:apply-templates mode="xpath" select="$xpaths"/>
        </xslout:transform>
    </xsl:template>

    <xsl:template match="text()" mode="ns failOnMiss xpath"/>
    <xsl:template match="param:ns" mode="ns">
        <xsl:namespace name="{@prefix}" select="@uri"/>
    </xsl:template>
    <xsl:template match="param:xpath" mode="failOnMiss">
        <xsl:variable name="xpath" select="." as="xs:string"/>
        <xslout:template match="{$xpath}" mode="matches"><xsl:value-of select="$xpath"/></xslout:template>
    </xsl:template>
    <xsl:template match="param:xpath" mode="xpath">
        <xsl:variable name="xpath" select="." as="xs:string"/>
        <xslout:template match="{$xpath}">
            <xslout:variable name="processed" as="xs:string" select="rax:process-url(.)"/>
            <xslout:if test="$processed != '{$DROPCODE}'">
                <xslout:choose>
                    <xslout:when test=". instance of attribute()">
                        <xslout:attribute>
                            <xsl:attribute name="name">{local-name()}</xsl:attribute>
                            <xslout:value-of select="$processed"/>
                        </xslout:attribute>
                    </xslout:when>
                    <xslout:otherwise>
                        <xslout:copy>
                            <xslout:apply-templates select="@*"/>
                            <xslout:value-of select="$processed"/>
                        </xslout:copy>
                    </xslout:otherwise>
                </xslout:choose>
            </xslout:if>
        </xslout:template>
    </xsl:template>
</xsl:transform>
