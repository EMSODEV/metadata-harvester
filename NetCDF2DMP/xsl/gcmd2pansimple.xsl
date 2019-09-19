<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns="urn:pangaea.de:dataportals"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:date="http://exslt.org/dates-and-times" 
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xsl date java"  >
  <xsl:template match="metadata">
  <dataset> 
		<dc:title>
			<xsl:value-of select="idinfo/citation/citeinfo/title"/>
		</dc:title>
		<dc:date>
			<xsl:value-of select="substring (idinfo/citation/citeinfo/pubdate,1,4)"/>
		</dc:date>
		<dc:identifier>
			<xsl:value-of select="idinfo/citation/citeinfo/onlink[1]"/>
		</dc:identifier>	
		<dc:publisher>
			Marine Institute
		</dc:publisher>
		<dc:dataCenter>Marine Institute</dc:dataCenter>
		<linkage xsi:type="LinkType" type="metadata">
		<xsl:value-of select="idinfo/citation/citeinfo/onlink[1]"/>
		</linkage>
		<dc:type>Dataset</dc:type>
	<xsl:if test="contains(idinfo/citation/citeinfo/title,'Galway')">	
		<dc:coverage xsi:type="CoverageType">	
			<location>Galway Bay</location>
		</dc:coverage>
		<dc:subject  xsi:type="SubjectType" type="feature">Smartbay</dc:subject>
	</xsl:if>
	<xsl:for-each select="/metadata/idinfo/keywords/theme/themekey">
		<dc:subject><xsl:value-of select="."/></dc:subject>
	</xsl:for-each>
	</dataset>
	</xsl:template>
</xsl:stylesheet>
