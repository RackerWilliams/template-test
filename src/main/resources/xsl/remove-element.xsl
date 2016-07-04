<xsl:transform version="2.0"
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:xslout="http://www.rackspace.com/xslout"
               xmlns:xs="http://www.w3.org/2001/XMLSchema"
               xmlns:param="http://www.rackspace.com/repose/params"
               xmlns:rax="http://docs.rackspace.com/api"
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

    <!-- Fail if the XPath doesn't match anything? -->
    <xsl:param name="failOnMiss" as="xs:boolean" select="false()"/>

    <!-- When you see xslout actually output xsl -->
    <xsl:namespace-alias stylesheet-prefix="xslout" result-prefix="xsl"/>

    <xsl:template match="/">
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
                <xslout:choose>
                    <xslout:when test=". instance of attribute()">
                        <xslout:attribute>
                            <xsl:attribute name="name">{local-name()}</xsl:attribute>
                            <xslout:value-of select="rax:process-url(.)"/>
                        </xslout:attribute>
                    </xslout:when>
                    <xslout:otherwise>
                        <xslout:copy>
                            <xslout:apply-templates select="@*"/>
                            <xslout:value-of select="rax:process-url(.)"/>
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
