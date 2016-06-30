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

    <!-- When you see xslout actually output xsl -->
    <xsl:namespace-alias stylesheet-prefix="xslout" result-prefix="xsl"/>

    <xsl:template match="/">
        <xslout:transform version="2.0">
            <xsl:apply-templates mode="ns" select="$namespaces"/>
            <!-- Copy everything -->
            <xslout:template match="@*|node()">
                <xslout:copy>
                    <xslout:apply-templates select="@*|node()"/>
                </xslout:copy>
            </xslout:template>

            <!-- Except this -->
            <xslout:template match="{$xpath}"/>
        </xslout:transform>
    </xsl:template>

    <xsl:template match="text()" mode="ns"/>
    <xsl:template match="param:ns" mode="ns">
        <xsl:namespace name="{@prefix}" select="@uri"/>
    </xsl:template>

</xsl:transform>
