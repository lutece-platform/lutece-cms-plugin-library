<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="current-space-id" select="current-space-id" />
    <xsl:variable name="current-space">
        <xsl:value-of select="current-space-id" />
    </xsl:variable>
    <xsl:template match="spaces">
        <ul>
            <xsl:apply-templates select="space" />
        </ul>
    </xsl:template>
    
    <xsl:template match="space">
        <li>
            <img src="{space-icon-url}" alt="" hspace="5" />
             <xsl:if test="space-is-valid='true'">
             	 <input type="radio" name="space_id" value="{space-id}" />         
             </xsl:if> 
            &#160;<xsl:value-of select="name" />
            <xsl:apply-templates select="child-spaces" />
        </li>  
    </xsl:template>
    <xsl:template match="child-spaces">
       <xsl:if test="space">
        	<ul id="spaces">
            	<xsl:apply-templates select="space" />
        	</ul>
       </xsl:if> 
    </xsl:template>
</xsl:stylesheet>