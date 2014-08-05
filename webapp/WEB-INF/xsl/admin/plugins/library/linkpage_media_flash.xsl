<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="false" omit-xml-declaration="yes" />

	<xsl:template match="media">
		<object 
			data="{src}" 
			type="application/x-shockwave-flash" 
			width="{width}" 
			height="{height}"
			align="{align}"
			classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
			codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0"
		>
			<param name="Autostart" value="{autostart}" />
			<param name="Quality" value="{quality}" />
			<param name="Loop" value="{loop}" />
			<param name="menu" value="{menu}" />
			<param name="allowScriptAccess" value="samedomain" />
			<param name="movie" value="{src}" />
			<EMBED 
				src="{src}" 
				type="application/x-shockwave-flash"
				width="{width}" 
				height="{height}"
				align="{align}"
				quality="{quality}"
				loop="{loop}"
				menu="{menu}"
				allowScriptAccess="samedomain"
				pluginspace="http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash">
			</EMBED>
		</object>
	</xsl:template>

</xsl:stylesheet>