<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:o="urn:schemas-microsoft-com:office:office"
    xmlns:x="urn:schemas-microsoft-com:office:excel"
    xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
    xmlns:html="http://www.w3.org/TR/REC-html40"
>
    <xsl:template match="/">
        <xsl:processing-instruction name="mso-application"> progid="Excel.Sheet"</xsl:processing-instruction>
        <Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet">
            <Styles>
                <Style ss:ID="Default" ss:Name="Normal">
                    <Alignment ss:Vertical="Bottom"/>
                    <Borders/>
                    <Font ss:FontName="Times New Roman" x:CharSet="204"/>
                    <Interior/>
                    <NumberFormat/>
                    <Protection/>
                </Style>
                <Style ss:ID="m382806864">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                </Style>
                <Style ss:ID="m382806884">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                    <NumberFormat ss:Format="#,##0"/>
                </Style>
                <Style ss:ID="m382806984">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman"
                          ss:Size="14" ss:Bold="1"/>
                    <Interior/>
                </Style>
                <Style ss:ID="m382807004">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                    <NumberFormat ss:Format="#,##0.0000"/>
                </Style>
                <Style ss:ID="m382807024">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                    <NumberFormat ss:Format="#,##0.0000"/>
                </Style>
                <Style ss:ID="m382807044">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                    <NumberFormat ss:Format="#,##0"/>
                </Style>
                <Style ss:ID="m382807064">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman"
                          ss:Size="14" ss:Bold="1"/>
                    <Interior/>
                </Style>
                <Style ss:ID="m382807084">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                    <NumberFormat ss:Format="#,##0.0000"/>
                </Style>
                <Style ss:ID="m382807104">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                    <NumberFormat ss:Format="#,##0.0000"/>
                </Style>
                <Style ss:ID="m382807124">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                    <NumberFormat ss:Format="#,##0"/>
                </Style>
                <Style ss:ID="m382807144">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                </Style>
                <Style ss:ID="m382807164">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                </Style>
                <Style ss:ID="s16">
                    <Alignment ss:Horizontal="Left" ss:Vertical="Bottom"/>
                </Style>
                <Style ss:ID="s17">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
                </Style>
                <Style ss:ID="s18">
                    <NumberFormat ss:Format="#,##0.0000"/>
                </Style>
                <Style ss:ID="s19">
                    <NumberFormat ss:Format="#,##0"/>
                </Style>
                <Style ss:ID="s20">
                    <Alignment ss:Horizontal="Right" ss:Vertical="Bottom"/>
                    <NumberFormat ss:Format="#,##0"/>
                </Style>
                <Style ss:ID="s21">
                    <Alignment ss:Horizontal="Right" ss:Vertical="Bottom"/>
                    <NumberFormat ss:Format="#,##0.0000"/>
                </Style>
                <Style ss:ID="s40">
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                </Style>
                <Style ss:ID="s41">
                    <Alignment ss:Horizontal="Left" ss:Vertical="Bottom"/>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                </Style>
                <Style ss:ID="s42">
                    <Alignment ss:Horizontal="Right" ss:Vertical="Bottom"/>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                    <NumberFormat ss:Format="#,##0"/>
                </Style>
                <Style ss:ID="s43">
                    <Alignment ss:Horizontal="Right" ss:Vertical="Bottom"/>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                    <NumberFormat ss:Format="#,##0.0000"/>
                </Style>
                <Style ss:ID="s44">
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                    <NumberFormat ss:Format="#,##0"/>
                </Style>
                <Style ss:ID="s45">
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman" ss:Bold="1"/>
                    <NumberFormat ss:Format="#,##0.0000"/>
                </Style>
                <Style ss:ID="s46">
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman"
                          ss:Size="12" ss:Bold="1"/>
                </Style>
                <Style ss:ID="s47">
                    <Alignment ss:Horizontal="Left" ss:Vertical="Bottom"/>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman"
                          ss:Size="12" ss:Bold="1"/>
                </Style>
                <Style ss:ID="s48">
                    <Alignment ss:Horizontal="Right" ss:Vertical="Bottom"/>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman"
                          ss:Size="12" ss:Bold="1"/>
                    <NumberFormat ss:Format="#,##0"/>
                </Style>
                <Style ss:ID="s49">
                    <Alignment ss:Horizontal="Right" ss:Vertical="Bottom"/>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman"
                          ss:Size="12" ss:Bold="1"/>
                    <NumberFormat ss:Format="#,##0.0000"/>
                </Style>
                <Style ss:ID="s50">
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman"
                          ss:Size="12" ss:Bold="1"/>
                    <NumberFormat ss:Format="#,##0"/>
                </Style>
                <Style ss:ID="s51">
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman"
                          ss:Size="12" ss:Bold="1"/>
                    <NumberFormat ss:Format="#,##0.0000"/>
                </Style>
                <Style ss:ID="s52">
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                </Style>
                <Style ss:ID="s53">
                    <Alignment ss:Horizontal="Left" ss:Vertical="Bottom"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                </Style>
                <Style ss:ID="s54">
                    <Alignment ss:Horizontal="Right" ss:Vertical="Bottom"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <NumberFormat ss:Format="#,##0"/>
                </Style>
                <Style ss:ID="s55">
                    <Alignment ss:Horizontal="Right" ss:Vertical="Bottom"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <NumberFormat ss:Format="#,##0.0000"/>
                </Style>
                <Style ss:ID="s56">
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <NumberFormat ss:Format="#,##0"/>
                </Style>
                <Style ss:ID="s57">
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <NumberFormat ss:Format="#,##0.0000"/>
                </Style>
                <Style ss:ID="s58">
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman"
                          ss:Color="#FF0000"/>
                </Style>
                <Style ss:ID="s59">
                    <Alignment ss:Horizontal="Left" ss:Vertical="Bottom"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman"
                          ss:Color="#FF0000"/>
                </Style>
                <Style ss:ID="s60">
                    <Alignment ss:Horizontal="Right" ss:Vertical="Bottom"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman"
                          ss:Color="#FF0000"/>
                    <NumberFormat ss:Format="#,##0"/>
                </Style>
                <Style ss:ID="s61">
                    <Alignment ss:Horizontal="Right" ss:Vertical="Bottom"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman"
                          ss:Color="#FF0000"/>
                    <NumberFormat ss:Format="#,##0.0000"/>
                </Style>
                <Style ss:ID="s62">
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman"
                          ss:Color="#FF0000"/>
                    <NumberFormat ss:Format="#,##0"/>
                </Style>
                <Style ss:ID="s63">
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
                        <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman"
                          ss:Color="#FF0000"/>
                    <NumberFormat ss:Format="#,##0.0000"/>
                </Style>
                <Style ss:ID="s64">
                    <Font ss:FontName="Times New Roman" x:CharSet="204" x:Family="Roman"
                          ss:Color="#FF0000"/>
                </Style>
                <Style ss:ID="s71">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Bottom"/>
                    <Font ss:FontName="Times New Roman" x:Family="Roman" ss:Size="20" ss:Bold="1"/>
                </Style>
                <Style ss:ID="s72">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Bottom"/>
                    <Font ss:FontName="Times New Roman" x:Family="Roman" ss:Size="14" ss:Bold="1"/>
                </Style>
                <Style ss:ID="s73">
                    <Alignment ss:Horizontal="Center" ss:Vertical="Bottom"/>
                    <Borders>
                        <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
                    </Borders>
                    <Font ss:FontName="Times New Roman" x:Family="Roman" ss:Size="12" ss:Bold="1"/>
                </Style>
            </Styles>
            <Worksheet ss:Name="Актив">
                <Table ss:ExpandedColumnCount="16" ss:ExpandedRowCount="116" x:FullColumns="1" x:FullRows="1" ss:DefaultColumnWidth="42">
                    <Column ss:AutoFitWidth="0" ss:Width="26.25"/>
                    <Column ss:AutoFitWidth="0" ss:Width="72"/>
                    <Column ss:StyleID="s16" ss:AutoFitWidth="0" ss:Width="186"/>
                    <Column ss:StyleID="s20" ss:Hidden="1" ss:AutoFitWidth="0" ss:Width="74.25"/>
                    <Column ss:StyleID="s20" ss:Hidden="1" ss:AutoFitWidth="0" ss:Width="9"/>
                    <Column ss:StyleID="s21" ss:AutoFitWidth="0" ss:Width="60"/>
                    <Column ss:StyleID="s19" ss:AutoFitWidth="0" ss:Width="78.75"/>
                    <Column ss:StyleID="s18" ss:AutoFitWidth="0" ss:Width="72"/>
                    <Column ss:StyleID="s18" ss:AutoFitWidth="0" ss:Width="66.75"/>
                    <Column ss:StyleID="s18" ss:AutoFitWidth="0" ss:Width="60.75"/>
                    <Column ss:AutoFitWidth="0" ss:Width="57"/>
                    <Column ss:AutoFitWidth="0" ss:Width="73.5"/>
                    <Column ss:Index="16" ss:AutoFitWidth="0" ss:Width="42.75"/>

                    <xsl:apply-templates select="/report/head" />

                    <Row />
                    <Row ss:AutoFitHeight="0" ss:StyleID="s17">
                        <Cell ss:MergeDown="2" ss:StyleID="m382807144"><Data ss:Type="String">п/п</Data></Cell>
                        <Cell ss:MergeDown="2" ss:StyleID="m382807164"><Data ss:Type="String">№ счетчиков</Data></Cell>
                        <Cell ss:MergeDown="2" ss:StyleID="m382806864"><Data ss:Type="String">Наименование объектов</Data></Cell>
                        <Cell ss:MergeDown="2" ss:StyleID="m382807124"><Data ss:Type="String">Показ. на конец периода</Data></Cell>
                        <Cell ss:MergeDown="2" ss:StyleID="m382807044"><Data ss:Type="String">Показ. на начала периода</Data></Cell>
                        <Cell ss:MergeDown="2" ss:StyleID="m382807084"><Data ss:Type="String">Коэф-т счетчиков</Data></Cell>
                        <Cell ss:MergeDown="2" ss:StyleID="m382806884"><Data ss:Type="String">К-во э/э,  учтенн. Счетчиком,         кВт*час</Data></Cell>
                        <Cell ss:MergeDown="2" ss:StyleID="m382807024"><Data ss:Type="String">Доля полученной (отпущенной) электроэнергии</Data></Cell>
                        <Cell ss:MergeDown="2" ss:StyleID="m382807004"><Data ss:Type="String">Средне-квадратичная  погрешность</Data></Cell>
                        <Cell ss:MergeDown="2" ss:StyleID="m382807104"><Data ss:Type="String">Допустимый небаланс</Data></Cell>
                    </Row>
                    <Row  />
                    <Row />

                    <xsl:apply-templates select="/report/division" />

                    <xsl:apply-templates select="/report/footer" />
                </Table>
            </Worksheet>
        </Workbook>
    </xsl:template>

    <xsl:template match="head">
        <Row ss:Height="25.5">
            <Cell ss:MergeAcross="9" ss:StyleID="s71"><Data ss:Type="String">АКТ</Data></Cell>
        </Row>

        <Row ss:Height="15.75">
            <Cell ss:MergeAcross="9" ss:StyleID="s73">
                <Data ss:Type="String">
                    <xsl:value-of select="concat('за период с ', period/@start-date, ' по ', period/@end-date)" />
                </Data>
            </Cell>
        </Row>

        <Row ss:AutoFitHeight="0" ss:Height="18.75">
            <Cell ss:MergeAcross="9" ss:MergeDown="1" ss:StyleID="m382806984">
                <Data ss:Type="String">
                    <xsl:value-of select="energy-object/@name" />
                </Data>
            </Cell>
        </Row>
    </xsl:template>

    <xsl:template match="division">
        <Row ss:StyleID="s46">
            <Cell><Data ss:Type="String"><xsl:value-of select="concat(@code, ' ', @name)"/></Data></Cell>
            <Cell ss:Index="3" ss:StyleID="s47"></Cell>
            <Cell ss:StyleID="s48"></Cell>
            <Cell ss:StyleID="s48"></Cell>
            <Cell ss:StyleID="s49"></Cell>
            <Cell ss:StyleID="s50"></Cell>
            <Cell ss:StyleID="s51"/>
            <Cell ss:StyleID="s51"/>
            <Cell ss:StyleID="s51"/>
        </Row>

        <xsl:apply-templates select="section" />

        <Row />
        <Row ss:Height="15.75" ss:StyleID="s46">
            <Cell><Data ss:Type="String"><xsl:value-of select="concat('Всего по разделу',  ' ', @code)" /></Data></Cell>
            <Cell ss:Index="3" ss:StyleID="s47"></Cell>
            <Cell ss:StyleID="s48"></Cell>
            <Cell ss:StyleID="s48"></Cell>
            <Cell ss:StyleID="s49"></Cell>
            <Cell ss:StyleID="s50"><Data ss:Type="Number"><xsl:value-of select="sum(section/mp[@total='true']/amount)" /></Data></Cell>
            <Cell ss:StyleID="s51"/>
            <Cell ss:StyleID="s51"/>
            <Cell ss:StyleID="s51"/>
        </Row>
    </xsl:template>

    <xsl:template match="section">
        <Row />
        <Row ss:StyleID="s40">
            <Cell><Data ss:Type="String"><xsl:value-of select="concat(@code, ' ', @name)"/></Data></Cell>
            <Cell ss:Index="3" ss:StyleID="s41"></Cell>
            <Cell ss:StyleID="s42"></Cell>
            <Cell ss:StyleID="s42"></Cell>
            <Cell ss:StyleID="s43"></Cell>
            <Cell ss:StyleID="s44"></Cell>
            <Cell ss:StyleID="s45"/>
            <Cell ss:StyleID="s45"/>
            <Cell ss:StyleID="s45"/>
        </Row>

        <xsl:apply-templates select="mp" />

        <Row ss:StyleID="s40">
            <Cell><Data ss:Type="String"><xsl:value-of select="concat('Итого по подразделу',  ' ', @code)" /></Data></Cell>
            <Cell ss:Index="3" ss:StyleID="s41"></Cell>
            <Cell ss:StyleID="s42"></Cell>
            <Cell ss:StyleID="s42"></Cell>
            <Cell ss:StyleID="s43"></Cell>
            <Cell ss:StyleID="s44"><Data ss:Type="Number"><xsl:value-of select="sum(./mp[@total='true']/amount)" /></Data></Cell>
            <Cell ss:StyleID="s45"/>
            <Cell ss:StyleID="s45"/>
            <Cell ss:StyleID="s45"/>
        </Row>
    </xsl:template>

    <xsl:template match="mp">
        <Row>
            <Cell ss:StyleID="s52"><Data ss:Type="Number">1</Data></Cell>
            <Cell ss:StyleID="s52"><Data ss:Type="String"><xsl:value-of select="serial" /></Data></Cell>
            <Cell ss:StyleID="s53"><Data ss:Type="String"><xsl:value-of select="name" /></Data></Cell>
            <Cell ss:StyleID="s54"></Cell>
            <Cell ss:StyleID="s54"></Cell>
            <Cell ss:StyleID="s55"><Data ss:Type="Number"><xsl:value-of select="rate" /></Data></Cell>
            <Cell ss:StyleID="s56"><Data ss:Type="Number"><xsl:value-of select="amount" /></Data></Cell>
            <Cell ss:StyleID="s57"><Data ss:Type="Number"><xsl:value-of select="proportion" /></Data></Cell>
            <Cell ss:StyleID="s57"><Data ss:Type="Number"><xsl:value-of select="error" /></Data></Cell>
            <Cell ss:StyleID="s57"><Data ss:Type="Number"><xsl:value-of select="under-count" /></Data></Cell>
        </Row>
    </xsl:template>
</xsl:stylesheet>
