<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:x="urn:schemas-microsoft-com:office:excel"
    xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">

    <xsl:template match="/">
        <xsl:processing-instruction name="mso-application"> progid="Excel.Sheet"</xsl:processing-instruction>
        <Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet">
            <xsl:copy-of select="document('styles.xml')"   />
            <xsl:apply-templates select="/report/sheet" />
        </Workbook>
    </xsl:template>

    <xsl:template match="sheet">
        <Worksheet>
            <xsl:attribute name="ss:Name" >
                <xsl:value-of select="@name" />
            </xsl:attribute>

            <xsl:variable name="columnCount" select="count(column)" />

            <Table  ss:ExpandedRowCount="65000">
                <xsl:attribute name="ss:ExpandedColumnCount">
                    <xsl:value-of select="$columnCount" />
                </xsl:attribute>

                <xsl:for-each select="column">
                    <Column>
                        <xsl:attribute name="ss:Width">
                            <xsl:value-of select="@width" />
                        </xsl:attribute>
                    </Column>
                </xsl:for-each>

                <xsl:apply-templates select="head" />
                <xsl:apply-templates select="table" />
                <xsl:apply-templates select="footer" />
            </Table>
        </Worksheet>
    </xsl:template>

    <xsl:template match="head">
        <xsl:variable name="columnCount" select="count(../column)" />

        <Row ss:AutoFitHeight="1">
            <Cell ss:StyleID="h1">
                <xsl:attribute name="ss:MergeAcross"><xsl:value-of select="$columnCount - 1" /></xsl:attribute>
                <Data ss:Type="String"><xsl:value-of select="name" /></Data>
            </Cell>
        </Row>

        <Row ss:AutoFitHeight="1">
            <Cell ss:StyleID="h4">
                <xsl:attribute name="ss:MergeAcross"><xsl:value-of select="$columnCount - 1" /></xsl:attribute>
                <Data ss:Type="String"><xsl:value-of select="concat('за период с ', period/@start-date, ' по ', period/@end-date)" /></Data>
            </Cell>
        </Row>

        <Row ss:AutoFitHeight="1">
            <Cell ss:StyleID="h4">
                <xsl:attribute name="ss:MergeAcross"><xsl:value-of select="$columnCount - 1" /></xsl:attribute>
                <Data ss:Type="String"><xsl:value-of select="energy-object/@name" /></Data>
            </Cell>
        </Row>
   </xsl:template>

    <xsl:template match="table">
        <Row />
        <xsl:apply-templates select="head" />
        <xsl:apply-templates select="body" />
        <xsl:apply-templates select="footer" />
    </xsl:template>

    <xsl:template match="table/head">
        <xsl:if test="count(column[@name]) &gt; 0">
            <Row ss:AutoFitHeight="1">
                <xsl:for-each select="column">
                    <Cell ss:StyleID="th">
                        <Data ss:Type="String">
                            <xsl:value-of select="@name" />
                        </Data>
                    </Cell>
                </xsl:for-each>
            </Row>
        </xsl:if>
    </xsl:template>

    <xsl:template match="table/body">
        <xsl:apply-templates select="division" />
    </xsl:template>

    <xsl:template match="table/body/division">
        <xsl:if test="position() &gt; 1">
            <Row/>
        </xsl:if>

        <xsl:if test="@is-title='true'">
            <Row>
                <Cell ss:StyleID="tddc"><Data ss:Type="String"><xsl:value-of select="@name"/></Data></Cell>
            </Row>
        </xsl:if>

        <xsl:apply-templates select="section" />

        <xsl:if test="@is-total='true'">
            <Row>
                <xsl:for-each select="total/attr">
                    <Cell>
                        <xsl:if test="@type='number'">
                            <xsl:attribute name="ss:StyleID">
                                <xsl:value-of select="concat('n', @precision, '-strong')" />
                            </xsl:attribute>
                        </xsl:if>

                        <xsl:if test="@type='string'">
                            <xsl:attribute name="ss:StyleID">c-strong</xsl:attribute>
                        </xsl:if>

                        <xsl:if test="not(@type='empty')">
                            <Data ss:Type="String">
                                <xsl:if test="@type='number'">
                                    <xsl:attribute name="ss:Type">Number</xsl:attribute>
                                </xsl:if>

                                <xsl:if test="@type='string'">
                                    <xsl:attribute name="ss:Type">String</xsl:attribute>
                                </xsl:if>

                                <xsl:value-of select="." />
                            </Data>
                        </xsl:if>
                    </Cell>
                </xsl:for-each>
            </Row>
        </xsl:if>
    </xsl:template>

    <xsl:template match="table/body/division/section">
        <xsl:if test="position() &gt; 1">
            <Row/>
        </xsl:if>

        <xsl:if test="@is-title='true'">
            <Row>
                <Cell ss:StyleID="tddc"><Data ss:Type="String"><xsl:value-of select="@name"/></Data></Cell>
            </Row>
        </xsl:if>

        <xsl:apply-templates select="row" />

        <xsl:if test="@is-total='true'">
            <Row>
                <xsl:for-each select="total/attr">
                    <Cell>
                        <xsl:if test="@type='number'">
                            <xsl:attribute name="ss:StyleID">
                                <xsl:value-of select="concat('n', @precision, '-strong')" />
                            </xsl:attribute>
                        </xsl:if>

                        <xsl:if test="@type='string'">
                            <xsl:attribute name="ss:StyleID">c-strong</xsl:attribute>
                        </xsl:if>

                        <xsl:if test="not(@type='empty')">
                            <Data ss:Type="String">
                                <xsl:if test="@type='number'">
                                    <xsl:attribute name="ss:Type">Number</xsl:attribute>
                                </xsl:if>

                                <xsl:if test="@type='string'">
                                    <xsl:attribute name="ss:Type">String</xsl:attribute>
                                </xsl:if>

                                <xsl:value-of select="." />
                            </Data>
                        </xsl:if>
                    </Cell>
                </xsl:for-each>
            </Row>
        </xsl:if>

    </xsl:template>

    <xsl:template match="table/body/division/section/row">
        <Row>
            <xsl:for-each select="attr">
                <Cell>
                    <xsl:if test="@type='number'">
                        <xsl:attribute name="ss:StyleID">
                            <xsl:value-of select="concat('tdn', @precision)" />
                        </xsl:attribute>
                    </xsl:if>

                    <xsl:if test="@type='string'">
                        <xsl:attribute name="ss:StyleID">tdc</xsl:attribute>
                    </xsl:if>

                    <Data ss:Type="String">
                        <xsl:if test="@type='number'">
                            <xsl:attribute name="ss:Type">Number</xsl:attribute>
                        </xsl:if>

                        <xsl:if test="@type='string'">
                            <xsl:attribute name="ss:Type">String</xsl:attribute>
                        </xsl:if>

                        <xsl:value-of select="." />
                    </Data>
                </Cell>
            </xsl:for-each>
        </Row>
    </xsl:template>

    <xsl:template match="table/footer">
        <xsl:apply-templates select="division" />
    </xsl:template>

    <xsl:template match="table/footer/division">
        <xsl:apply-templates select="row" />
    </xsl:template>

    <xsl:template match="table/footer/division/row">
        <xsl:variable name="strong">
            <xsl:if test="position()=1">-strong</xsl:if>
        </xsl:variable>

        <xsl:if test="position()=1">
            <Row/>
        </xsl:if>

        <Row>
            <xsl:for-each select="attr">
                <Cell>
                    <xsl:if test="@type='number'">
                        <xsl:attribute name="ss:StyleID">
                            <xsl:value-of select="concat('n', @precision, $strong)" />
                        </xsl:attribute>
                    </xsl:if>

                    <xsl:if test="@type='string'">
                        <xsl:attribute name="ss:StyleID"><xsl:value-of select="concat('c', $strong)" /></xsl:attribute>
                    </xsl:if>

                    <xsl:if test="not(@type='empty')">
                        <Data>
                            <xsl:if test="@type='number'">
                                <xsl:attribute name="ss:Type">Number</xsl:attribute>
                            </xsl:if>

                            <xsl:if test="@type='string'">
                                <xsl:attribute name="ss:Type">String</xsl:attribute>
                            </xsl:if>

                            <xsl:value-of select="." />
                        </Data>
                    </xsl:if>
                </Cell>
            </xsl:for-each>
        </Row>
    </xsl:template>
</xsl:stylesheet>
