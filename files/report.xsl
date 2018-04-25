<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:x="urn:schemas-microsoft-com:office:excel"
    xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">

    <xsl:template name = "show_title" >
        <xsl:param name = "title" />

        <Row>
            <Cell ss:StyleID="tddc"><Data ss:Type="String"><xsl:value-of select="$title"/></Data></Cell>
            <Cell ss:StyleID="tddc"/>
            <Cell ss:StyleID="tddc"/>
            <Cell ss:StyleID="tddc"/>
            <Cell ss:StyleID="tddc"/>
            <Cell ss:StyleID="tddc"/>
            <Cell ss:StyleID="tddc"/>
            <Cell ss:StyleID="tddc"/>
            <Cell ss:StyleID="tddc"/>
            <Cell ss:StyleID="tddc"/>
        </Row>
    </xsl:template>

    <xsl:template name = "show_total" >
        <xsl:param name = "title" />
        <xsl:param name = "total" />

        <Row>
            <Cell ss:StyleID="tddc"><Data ss:Type="String"><xsl:value-of select="$title" /></Data></Cell>
            <Cell ss:StyleID="tddc"/>
            <Cell ss:StyleID="tddc"/>
            <Cell ss:StyleID="tddc"/>
            <Cell ss:StyleID="tddc"/>
            <Cell ss:StyleID="tddc"/>
            <Cell ss:StyleID="tddn0"><Data ss:Type="Number"><xsl:value-of select="$total" /></Data></Cell>
            <Cell ss:StyleID="tddc"/>
            <Cell ss:StyleID="tddc"/>
            <Cell ss:StyleID="tddc"/>
        </Row>
    </xsl:template>

    <xsl:template match="/">
        <xsl:processing-instruction name="mso-application"> progid="Excel.Sheet"</xsl:processing-instruction>
        <Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet">

            <xsl:copy-of select="document('styles.xml')"   />

            <Worksheet ss:Name="Акт">
                <Table ss:ExpandedColumnCount="10" ss:ExpandedRowCount="65000" x:FullColumns="1" x:FullRows="1">
                    <xsl:apply-templates select="/report-result/sheet" />

                    <xsl:apply-templates select="/report-result/head" />

                    <xsl:apply-templates select="/report-result/table" />

                    <xsl:apply-templates select="/report-result/footer" />
                </Table>
            </Worksheet>
        </Workbook>
    </xsl:template>

    <xsl:template match="sheet">
        <xsl:for-each select="column">
            <Column>
                <xsl:attribute name="ss:Width">
                    <xsl:value-of select="@width" />
                </xsl:attribute>
            </Column>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="head">
        <Row ss:AutoFitHeight="1">
            <Cell ss:MergeAcross="9" ss:StyleID="h1"><Data ss:Type="String">АКТ</Data></Cell>
        </Row>

        <Row ss:AutoFitHeight="1">
            <Cell ss:MergeAcross="9" ss:StyleID="h4">
                <Data ss:Type="String"><xsl:value-of select="concat('за период с ', period/@start-date, ' по ', period/@end-date)" /></Data>
            </Cell>
        </Row>

        <Row ss:AutoFitHeight="1">
            <Cell ss:MergeAcross="9" ss:StyleID="h4">
                <Data ss:Type="String"><xsl:value-of select="energy-object/@name" /></Data>
            </Cell>
        </Row>
   </xsl:template>

    <xsl:template match="table">
        <xsl:if test="position() &gt; 1">
            <Row />
        </xsl:if>

        <xsl:if test="count(column[@name]) &gt; 0">
            <Row />
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

        <xsl:apply-templates select="division" />
        <xsl:apply-templates select="footer" />
    </xsl:template>

    <xsl:template match="table/division">
        <xsl:call-template name="show_title">
            <xsl:with-param name="title" select = "concat(@code, ' ', @name)" />
        </xsl:call-template>

        <xsl:apply-templates select="section" />

        <Row />

        <xsl:call-template name="show_total">
            <xsl:with-param name="title" select = "concat('Всего по разделу',  ' ', @code)" />
            <xsl:with-param name="total" select = "sum(section/row[@total='true']/attr[@name='amount'])" />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="table/division/section">
        <Row />

        <xsl:call-template name="show_title">
            <xsl:with-param name="title" select = "concat(@code, ' ', @name)" />
        </xsl:call-template>

        <xsl:apply-templates select="row" />

        <xsl:call-template name="show_total">
            <xsl:with-param name="title" select = "concat('Итого по подразделу',  ' ', @code)" />
            <xsl:with-param name="total" select = "sum(row[@total='true']/attr[@name='amount'])" />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="table/division/section/row">
        <Row>
            <xsl:for-each select="attr">
                <Cell>
                    <xsl:if test="@type='number'">
                        <xsl:attribute name="ss:StyleID">
                            <xsl:value-of select="concat('tdn', @precision)" />
                        </xsl:attribute>
                    </xsl:if>

                    <xsl:if test="not(@type)">
                        <xsl:attribute name="ss:StyleID">tdc</xsl:attribute>
                    </xsl:if>

                    <Data ss:Type="String">
                        <xsl:if test="@type='number'">
                            <xsl:attribute name="ss:Type">Number</xsl:attribute>
                        </xsl:if>

                        <xsl:if test="not(@type)">
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

    <xsl:template match="table/footer/division/row[position()=1]">
        <Row/>
        <Row>
            <Cell ss:StyleID="c-strong"><Data ss:Type="String"><xsl:value-of select="attr[@name='name']" /></Data></Cell>
            <Cell/>
            <Cell/>
            <Cell/>
            <Cell/>
            <Cell ss:StyleID="c-strong"><Data ss:Type="String"><xsl:value-of select="attr[@name='unit']" /></Data></Cell>

            <Cell>
                <xsl:if test="attr[@name='amount']">
                    <xsl:attribute name="ss:StyleID">
                        <xsl:value-of select="concat('n', attr[@name='amount']/@precision, '-strong')" />
                    </xsl:attribute>
                    <Data ss:Type="Number"><xsl:value-of select="attr[@name='amount']" /></Data>
                </xsl:if>
            </Cell>
        </Row>
    </xsl:template>

    <xsl:template match="table/footer/division/row[position() &gt; 1]">
        <Row>
            <Cell ss:StyleID="c"><Data ss:Type="String"><xsl:value-of select="attr[@name='name']" /></Data></Cell>
            <Cell/>
            <Cell/>
            <Cell/>
            <Cell/>
            <Cell ss:StyleID="c"><Data ss:Type="String"><xsl:value-of select="attr[@name='unit']" /></Data></Cell>

            <Cell>
                <xsl:if test="attr[@name='amount']">
                    <xsl:attribute name="ss:StyleID">
                        <xsl:value-of select="concat('n', attr[@name='amount']/@precision)" />
                    </xsl:attribute>
                    <Data ss:Type="Number"><xsl:value-of select="attr[@name='amount']" /></Data>
                </xsl:if>
            </Cell>
        </Row>
    </xsl:template>
</xsl:stylesheet>
