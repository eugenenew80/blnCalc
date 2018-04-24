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
            <Cell ss:StyleID="tddn2"><Data ss:Type="Number"><xsl:value-of select="$total" /></Data></Cell>
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
                    <Column ss:Width="26.25"/>
                    <Column ss:Width="72"/>
                    <Column ss:Width="186"/>
                    <Column ss:Width="74.25"/>
                    <Column ss:Width="9"/>
                    <Column ss:Width="60"/>
                    <Column ss:Width="78.75"/>
                    <Column ss:Width="72"/>
                    <Column ss:Width="66.75"/>
                    <Column ss:Width="60.75"/>

                    <xsl:apply-templates select="/report-result/head" />

                    <xsl:apply-templates select="/report-result/division" />

                    <xsl:apply-templates select="/report-result/footer" />
                </Table>
            </Worksheet>
        </Workbook>
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

        <Row />

        <Row ss:AutoFitHeight="1">
            <Cell ss:StyleID="th"><Data ss:Type="String">п/п</Data></Cell>
            <Cell ss:StyleID="th"><Data ss:Type="String">№ счетчиков</Data></Cell>
            <Cell ss:StyleID="th"><Data ss:Type="String">Наименование объектов</Data></Cell>
            <Cell ss:StyleID="th"><Data ss:Type="String">Показ. на конец периода</Data></Cell>
            <Cell ss:StyleID="th"><Data ss:Type="String">Показ. на начала периода</Data></Cell>
            <Cell ss:StyleID="th"><Data ss:Type="String">Коэф-т счетчиков</Data></Cell>
            <Cell ss:StyleID="th"><Data ss:Type="String">К-во э/э,  учтенн. Счетчиком, кВт*час</Data></Cell>
            <Cell ss:StyleID="th"><Data ss:Type="String">Доля полученной (отпущенной) электроэнергии</Data></Cell>
            <Cell ss:StyleID="th"><Data ss:Type="String">Средне-квадратичная  погрешность</Data></Cell>
            <Cell ss:StyleID="th"><Data ss:Type="String">Допустимый небаланс</Data></Cell>
        </Row>
    </xsl:template>

    <xsl:template match="division">
        <xsl:call-template name="show_title">
            <xsl:with-param name="title" select = "concat(@code, ' ', @name)" />
        </xsl:call-template>

        <xsl:apply-templates select="section" />

        <Row />

        <xsl:call-template name="show_total">
            <xsl:with-param name="title" select = "concat('Всего по разделу',  ' ', @code)" />
            <xsl:with-param name="total" select = "sum(section/mp[@total='true']/amount)" />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="section">
        <Row />

        <xsl:call-template name="show_title">
            <xsl:with-param name="title" select = "concat(@code, ' ', @name)" />
        </xsl:call-template>

        <xsl:apply-templates select="mp" />

        <xsl:call-template name="show_total">
            <xsl:with-param name="title" select = "concat('Итого по подразделу',  ' ', @code)" />
            <xsl:with-param name="total" select = "sum(mp[@total='true']/amount)" />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="mp">
        <Row>
            <Cell ss:StyleID="tdc"><Data ss:Type="String"><xsl:value-of select="@num" /></Data></Cell>
            <Cell ss:StyleID="tdc"><Data ss:Type="String"><xsl:value-of select="serial" /></Data></Cell>
            <Cell ss:StyleID="tdc"><Data ss:Type="String"><xsl:value-of select="name" /></Data></Cell>
            <Cell ss:StyleID="tdn2"><Data ss:Type="Number"><xsl:value-of select="amount" /></Data></Cell>>
            <Cell ss:StyleID="tdn2"><Data ss:Type="Number"><xsl:value-of select="amount" /></Data></Cell>
            <Cell ss:StyleID="tdn4"><Data ss:Type="Number"><xsl:value-of select="rate" /></Data></Cell>
            <Cell ss:StyleID="tdn2"><Data ss:Type="Number"><xsl:value-of select="amount" /></Data></Cell>
            <Cell ss:StyleID="tdn4"><Data ss:Type="Number"><xsl:value-of select="proportion" /></Data></Cell>
            <Cell ss:StyleID="tdn4"><Data ss:Type="Number"><xsl:value-of select="error" /></Data></Cell>
            <Cell ss:StyleID="tdn4"><Data ss:Type="Number"><xsl:value-of select="under-count" /></Data></Cell>
        </Row>
    </xsl:template>

    <xsl:template match="footer">
    </xsl:template>
</xsl:stylesheet>
