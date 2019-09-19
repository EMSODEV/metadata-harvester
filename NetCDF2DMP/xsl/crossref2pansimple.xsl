<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns="urn:pangaea.de:dataportals"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:date="http://exslt.org/dates-and-times" 
  xmlns:cr="http://datacite.org/schema/kernel-3"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xsl date cr java"
  >
  <xsl:template match="cr:resource">
  <dataset> 
		<dc:title>
			<xsl:value-of select="cr:titles/cr:title[1]"/>
		</dc:title>
		<dc:date>
			<xsl:value-of select="cr:publicationYear"/>
		</dc:date>
		<dc:identifier>
			<xsl:value-of select="cr:identifier"/>
		</dc:identifier>
	<xsl:for-each select="cr:creators/cr:creator">
			<dc:creator>
				<xsl:value-of select="cr:creatorName"></xsl:value-of>				
			</dc:creator>
		</xsl:for-each>
		<dc:publisher>
			<xsl:value-of select="cr:publisher"></xsl:value-of>
		</dc:publisher>
		<principalInvestigator>
			<xsl:value-of select="cr:creators/cr:creator[1]/cr:creatorName"/>
		</principalInvestigator>
		<dc:dataCenter>OGS Trieste</dc:dataCenter>
		<linkage xsi:type="LinkType" type="data">
		https://dx.doi.org/<xsl:value-of select="cr:identifier"/>
		</linkage>
		<linkage xsi:type="LinkType" type="metadata">
		http://nodc.ogs.trieste.it/nodc/metadata/doidetails?doi=<xsl:value-of select="cr:identifier"/>
		</linkage>
		<dc:type>Dataset</dc:type>
	<xsl:if test="contains(cr:titles/cr:title[1],'E2M3A')">	
		<dc:coverage xsi:type="CoverageType">	
			<location>Adriatic Sea</location>
		</dc:coverage>
		<dc:subject  xsi:type="SubjectType" type="feature">E2M3A</dc:subject>
	</xsl:if>
	</dataset>
	</xsl:template>
</xsl:stylesheet>
