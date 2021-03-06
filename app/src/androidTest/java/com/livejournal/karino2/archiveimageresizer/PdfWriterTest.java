package com.livejournal.karino2.archiveimageresizer;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.ApplicationTestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

import crl.android.pdfwriter.PDFWriter;
import crl.android.pdfwriter.XObjectImage;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class PdfWriterTest extends ApplicationTestCase<Application> {
    public PdfWriterTest() {
        super(Application.class);
    }

    @Override
    protected void tearDown() throws Exception {
        XObjectImage.RESET_IMAGE_COUNT();
        super.tearDown();
    }

    Context testContext;
    public void setTestContext(Context tstContext) {
        testContext = tstContext;
    }

    // need to handle carriage return.
    boolean match(String regExp, String target) {
        Pattern p = Pattern.compile(regExp, Pattern.DOTALL);
        return p.matcher(target).matches();
    }

    public void testRegExpMultiLine() {
        assertTrue(match("  .*", "  /ID [<649FD939DDE9E17D1D51A96D05FC5AEB> <649FD939DDE9E17D1D51A96D05FC5AEB>]"));
        assertTrue(match("  /ID .*", "  /ID [<649FD939DDE9E17D1D51A96D05FC5AEB> <649FD939DDE9E17D1D51A96D05FC5AEB>]\n"));
    }

    public void testPdfWriterStream() throws IOException {
        AssetManager am = testContext.getAssets();
        Bitmap starImg = BitmapFactory.decodeStream(am.open("CRL-star.jpg"));
        Bitmap bmpImg = BitmapFactory.decodeStream(am.open("CRL-24bits.bmp"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PDFWriter writer = new PDFWriter(800, 600, outputStream);
        writer.writeHeader();
        writer.writeCatalogStream();
        writer.writePagesHeader(2);

        writer.writeImagePage(starImg);
        writer.newOrphanPage();
        writer.writeImagePage(bmpImg);

        writer.writeFooter();

        String actual = outputStream.toString("ISO-8859-1");
        verifyLiteralResult(actual);
    }

    public void testPdfWriterTwoImage() throws IOException {
        AssetManager am = testContext.getAssets();
        Bitmap starImg = BitmapFactory.decodeStream(am.open("CRL-star.jpg"));
        Bitmap bmpImg = BitmapFactory.decodeStream(am.open("CRL-24bits.bmp"));

        PDFWriter writer = new PDFWriter(800, 600);
        writer.addImage(0, 0, starImg);
        writer.newPage();
        writer.addImage(0, 0, bmpImg);

        String actual = writer.asString();
        verifyLiteralResult(actual);
    }

    private void verifyLiteralResult(String actual) {
        String expectUntilID = getExpectUntilID();

        // "%PDF-1.4\n"
        String beforeBinary = "%PDF-1.4\n%";
        assertEquals(beforeBinary, actual.substring(0, beforeBinary.length()));
        int afterBinaryPos = "%PDF-1.4\n%����\n".length();
        assertEquals(expectUntilID.substring(afterBinaryPos), actual.substring(afterBinaryPos, expectUntilID.length()));

        String tmp = actual.substring(expectUntilID.length());

        assertTrue(match("  /ID \\[<[0-9A-Z]+> <[0-9A-Z]+>\\].*", actual.substring(expectUntilID.length())));
        // buf.append("  /ID [<649FD939DDE9E17D1D51A96D05FC5AEB> <649FD939DDE9E17D1D51A96D05FC5AEB>]\n" );

        String expectAfterID = getExpectAfterID();
        assertEquals(expectAfterID, actual.substring(actual.length()-expectAfterID.length()));
    }

    String getExpectUntilID() {
        StringBuffer buf = new StringBuffer();
        buf.append("%PDF-1.4\n");
        buf.append("%����\n");
        buf.append(        "1 0 obj\n" );
buf.append(                "<<\n" );
buf.append(                "  /Type /Catalog\n" );
buf.append(                "  /Pages 2 0 R\n" );
buf.append(                ">>\n" );
buf.append(                "endobj\n" );
buf.append(                "\n" );
buf.append(                "2 0 obj\n" );
buf.append(                "<<\n" );
buf.append(                "  /Type /Pages\n" );
buf.append(                "  /MediaBox [ 0 0 800 600 ]\n" );
buf.append(                "  /Count 2\n" );
buf.append(                "  /Kids [ 3 0 R 7 0 R ]\n" );
buf.append(                ">>\n" );
buf.append(                "endobj\n" );
buf.append(                "\n" );
buf.append(                "3 0 obj\n" );
buf.append(                "<<\n" );
buf.append(                "  /Type /Page\n" );
buf.append(                "  /Parent 2 0 R\n" );
buf.append(                "  /Resources <<\n" );
buf.append(                "    /Font <<\n" );
buf.append(                "      /F1 4 0 R\n" );
buf.append(                "    >>\n" );
buf.append(                "    /XObject <<\n" );
buf.append(                "      /img1 6 0 R\n" );
buf.append(                "    >>\n" );
buf.append(                "  >>\n" );
buf.append(                "  /Contents 5 0 R\n" );
buf.append(                ">>\n" );
buf.append(                "endobj\n" );
buf.append(                "\n" );
buf.append(                "4 0 obj\n" );
buf.append(                "<<\n" );
buf.append(                "  /Type /Font\n" );
buf.append(                "  /Subtype /Type1\n" );
buf.append(                "  /BaseFont /Times-Roman\n" );
buf.append(                "  /Encoding /WinAnsiEncoding\n" );
buf.append(                ">>\n" );
buf.append(                "endobj\n" );
buf.append(                "\n" );
buf.append(                "5 0 obj\n" );
buf.append(                "<<\n" );
buf.append(                "  /Length 61\n" );
buf.append(                ">>\n" );
buf.append(                "stream\n" );
buf.append(                "q\n" );
buf.append(                "1 0 0 1 0 0 cm\n" );
buf.append(                "1 0 0 1 0 0 cm\n" );
buf.append(                "135 0 0 75 0 0 cm\n" );
buf.append(                "/img1 Do\n" );
buf.append(                "Q\n" );
buf.append(                "endstream\n" );
buf.append(                "endobj\n" );
buf.append(                "\n" );
buf.append(                "6 0 obj\n" );
buf.append(                "<<\n" );
buf.append(                " /Type /XObject\n" );
buf.append(                " /Subtype /Image\n" );
buf.append(                " /Filter [/ASCII85Decode /FlateDecode]\n" );
buf.append(                " /Width 135\n" );
buf.append(                " /Height 75\n" );
buf.append(                " /BitsPerComponent 8\n" );
buf.append(                " /Interpolate false\n" );
buf.append(                " /ColorSpace /DeviceRGB\n" );
buf.append(                " /Length 36982\n" );
buf.append(                ">>\n" );
buf.append(                "stream\n" );
buf.append(                "GQ@iCG$'V!q>^KnhZ*HPs8DBcqsX=[kjSQ?s8;Tjq>L-hqY'sWs8VWhs7H-bp&G$is8Duss8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8Drs\n" );
buf.append(                "rVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rfs8VZis7ZKmq#CBjrr)Qfp&+jbq>^*ds8)cpq>]pVo`+merVZ'_s8W)up](9as8Vlos8Vljs7$!_rmq)Ks82irs8;E_jeh\\-bk1)3s5q&_kH!V\"WrLbXp>N%Al)XIAX7bSPnD^55k,.h5W:J`=ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9\n" );
buf.append(                "ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9ptq_=hk0]$TD7<9o&QS;jIu>0U\\EiBp>VY:\n" );
buf.append(                "igoc$ReGg4jQ`K.n>cBEVW[?En(aT!kb\\=;X8h.ZlK\"u4k-4F<YjCMjj7',Js8)cms6fmIn*d7RrU9+Ni;*?>q#BXPs7--coD&@cpA4abs8V`hs7ZBgqu?Qms8)ZfoD&@cq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlo\n" );
buf.append(                "s7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos7lWoq#CBhs8Vlos8W#ls8Vlos7lWmo`+j^s8Vujp@SCas8VZ_s8)WmoD&=bqu6Qimf3(Vs7lBhqY'mRs8DBcs60L\\lf[K7r:fg_pAb0hnc.XVrq?B!g=rIQj.>)hQJV2'eF8RZi2+ugVqC^9i;')!m%2_bUYb.7h!1`eje^Pi\n" );
buf.append(                "V:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)\n" );
buf.append(                "f&`aZje^PiV:Ob)f&`aZje^PiV:Ob)f&`[[iM,)dT%i1lg#/@SjJ:l+Q.+`dhXI2ml^d#&S(?YmfArd]k*Xo^R+0ocfAELTnAirLk5,,XrVucU])V^%s8Duss6KC<V>pMjf)4Xos8VWhqt0pgs8N&Ps5*eQo`+7Np%\\ReqY^BkqYpNns7lWhrr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]l\n" );
buf.append(                "rqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9frr2]lrqQKlq#:9mq>^Kks82]nq#(0er;ZWls8Dips82idq>9a_s7cQnqrd5Do_&7apA4gZs8;BdrVHQPm.C>Ms8V`\\f:Mm9kkb;Yq>^0Ngn!3pge>=t\n" );
buf.append(                "2S3ER\\k4E^7Okr9;RC*/ptu,Kr:9mfs8Mrrs8UmKrpp*hs8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8Dut\n" );
buf.append(                "rVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&ts8DutrVuors8W&tp&\"XZrr;Wjs763io)JaRoBlPWn,NFKs82irqu?]Ol-)>'cQl?C+AUW;9^!9^\\2?Fo5?ILAebfC8pZ\\^Ps6omcs8Vins5:0Vs6W%U=s^OH0>0Fo_*'$[3\\n<OoDdnNrVPp\\s8N&un+H]ZI\\&i5TLE5#7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7\n" );
buf.append(                "]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]L?2?7:3\\r?bAg7]Mi=O8mT(q=MR1'_+J(I@rSmBn,N:^s7Z6\\s8DfooCpTc<nnlk\n" );
buf.append(                "kT34H.7NA:3oV&KkojF6pAOdds750Qnc.qTrr;fooCD\"8k5X0e\\P=0`9-V8>(VW3P`C_Md<VZUWs8)cds8W,ns8Vin6\"?QV?bSU2]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb\n" );
buf.append(                "@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_Fj4]fT]:4(>lb@_G*8WBXaq5@_SqA&U`AZ:nbnn,34Zs7l9eqtL*`s7c0.,YHu=8E(LMc71p:,!=L.51U`Gs82T_rqQNPYPnLfs82irs82NNVY0u[.)/4q]fdLS80ki0)T=ue_FjY(rVu]ns8DutqtT:Oqb(e52dX0a@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<\n" );
buf.append(                "_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI6=@Ys@)>6<_EqYI\n" );
buf.append(                "6=@Z!=h6n+[6%p-3+0Kb?GfcR[IWngs8M`lrVulsqu?WdfH<f>,Y-r86/NkQhB>2@+?\\I*70!)]r;ZZ]jK&=ZqZ$Ths8VuijJMVMngM7B,/$,^_+5ia3uZ((+2'f3s7?9gs8W&ts8)W[s8PW$Ys)L24^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue\n" );
buf.append(                "@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<4^bue@(nd3^-,l<87BS*@_X^+[PhX23*a*i@]T)!s8;ogs8Duss82ipo@P0Xfd')L)aW@*6J`kOh&ni9,Xh\\.rVZ]mmGt>JrVHQopAb0ip>i+Ds7.SQ7O#N/+MgSo]K7%N5paE0p]'X\\rr)N`\n" );
buf.append(                "qu?Nkn,ND\\@BE..]g-&=5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;\n" );
buf.append(                "5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0Ki;5[q>j@_4m7]0TW:5[hAd@(/I<_`M558ReF^s8V]jnGiIerr;lqrU\\Ya9%bI[jVpMC,!t-06/<YGh'>Dgq>L6kqsEIFs8Dfos7ZKmqtB*Jp&FS2^IBE\\4WqR3,eQ2W`BbWY3Vi^Hs8W&qs8W,qrTs:S?\"g-=@B*%.\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*\n" );
buf.append(                "?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fij;\\kQMB9jYD*?Fj'B_bOIL5ub'Y>e*I2[p<^Qm.LDMqu?]ps8N&qs8DQA+A1<48*([Uh]bDD+[Op,4P_8Ns7uWks823E\n" );
buf.append(                "X8i.ps8Vfms82NNVY0u[.)/4q]fdLS80ki0)T=ui]h82%qY9g_n+Qb\\pA+Oaqq^31q=ajaqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQl\n" );
buf.append(                "q>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3fqu?Khs7uQlq>:3eqZ$Bhs8;lrr;HZnrVu!CmJd.WrVtjVrVulsqu?WdfH<f>,Y-r86/NkQhB>2@+?\\I*70!)]r;ZZ]jK&=ZqZ$Ths8VuijJMVMngM7B,/$,^_+5ia3uZ((.Ci7Os8Dlos7lWos7cB`q<kbucF9ac]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[\n" );
buf.append(                "VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`]r&Z[VS^$D_S)h`\\#$LBUUIe0]=4BD]r&`^XMXl'\n" );
buf.append(                "q>^9ds8Duss82ipo@P0Xfd')L)aW@*6J`kOh&ni9,Xh\\.rVZ]mmGt>JrVHQopAb0ip>i+Ds7.SQ7O#N/+MgSo]K7%N5q]lGqu?Hdr;66gs8Vrqs8Vl*XN.`;X1k4?^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R\n" );
buf.append(                "^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:p5R^7W-RT>&%3^:U#I_5\"caU;Fd<^qH8M\\tZ_7pAb0ks8W&trr;lqrU\\Ya9%bI[jVpMC,!t-06/<YGh'>Dgq>L6kqsEIFs8Dfos7ZKmqtB*Jp&FS2^IBE\\4WqR3,eQ2W`Bbud5l:TMqu?N^rr<#ps8Vuk\\\"ps^V7OC5^qQGV^7W3T\n" );
buf.append(                "T>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815^qQGV^7W3TT>815\n" );
buf.append(                "^qQGV^7W3TT>815^qQGV^7W3TT>8+4aM=Ij^6caZTZG!D^q?PVs82ikq#CBms8N&qs8DQA+A1<48*([Uh]bDD+[Op,4P_8Ns7uWks823EX8i.ps8Vfms82NNVY0u[.)/4q]fdLS80ki0)T=un]0l/ho`\"ghrr)Zmqu?Zms1bJ>\\>-pWT>JF:_SMk]_4ST\\T>JF:_SMk]_4ST\\T>JF:_SMk]_4ST\\T>JF:_SMk]_4ST\\T>JF:_SMk]_4ST\\T>JF:_SMk]_4ST]TYnI4^;m(k]:$=KTYnaD`jV,@_4ST\\T>J%$\\&G,^_P\"fYRCpS:aM\"(W\n" );
buf.append(                "_P\"f_T>JF:_SMk]_4ST\\T>JF:_SMk]_4ST\\T>JF:_SMk]_4ST\\T>JF:_SMk]_4ST\\T>JF:_SMk]_4ST\\T>JF:_SMk]_4ST]TYndFa1[tV_4ST\\T>J:2^;cth_P\"f`TYnI4^;m(k]:$=KTYnaD`jV,@_4ST\\T>JF:_SMk]_4ST\\T>JF:_SMk]_4ST\\T>JF:_SMk]_4ST\\T>JF:_SMk]_4ST^S]8XBa1n:a^S8NUV7sRA^&Rd*s7lWorVulsqu?WdfH<f>,Y-r86/NkQhB>2@+?\\I*70!)]r;ZZ]jK&=ZqZ$Ths8VuijJMVMngM7B,/$,^\n" );
buf.append(                "_+5ia3uZ((.C_Y3s7--gs7l?[s82irrVs@#Xh:XQ]V<0RU;+@8^V$;U]V<0RU;+@8^V$;U]V<0RU;+@8^V$;U]V<0RU;+@8^V$;U]V<0RU;+@8^V$;U]V<0RU;+@8^V$;U^8/TUT=hS$\\@S?H\\tHaKTt\\LHaLn7^]V<0[X2qoH^:KuN\\tHaES%-#$]Y:2X]V<0RU;+@8^V$;U]V<0RU;+@8^V$;U]V<0RU;+@8^V$;U]V<0RU;+@8^V$;U]V<0RU;+@8^V$;U]V<0RU;+@8^V$;U\\tHaES%-50_RlMU]:lsRV8BX0\\@S?H^8/TUT=hS$\n" );
buf.append(                "\\@S?H\\tHaKTt\\LHaLn7^]:lsNTt\\.4^:U)Q]:lsNTt\\.4^:U)Q]:lsNTt\\.4^:U)Q]:lsNTt\\.4^:U)Q^7W3WU;OR8]sj]F[%Xn:WOi-%s8Vurs8Duss82ipo@P0Xfd')L)aW@*6J`kOh&ni9,Xh\\.rVZ]mmGt>JrVHQopAb0ip>i+Ds7.SQ7O#N/+MgSo]K7%N5qTW8n,MqXrr;urq>^?lrr)l0W3NOu\\[eTJ\\YcpJVRa77\\[eTJ\\YcpJVRa77\\[eTJ\\YcpJVRa77\\[eTJ\\YcpJVRa77\\[eTJ\\YcpJVRa77\\[eTJ\\YcpJVRa77\\[.s8\n" );
buf.append(                "\\>?^GVRaC?]sXTBYar#2WP#^9\\$r0B]W&QJSZoMr[CrTR\\YcpIV7<k)ZalsD\\YcpJVRa77\\[eTJ\\YcpJVRa77\\[eTJ\\YcpJVRa77\\[eTJ\\YcpJVRa77\\[eTJ\\YcpJVRa77\\[eTJ\\YcpJVRa77\\[n]M[A((AWP#[7[^Ms>]W&QOU:%S1]!J'9\\>?^GVRaC?]sXTBYar#2WP#^9\\%ATN];W?RW4T[?]=Y#R];W?RW4T[?]=Y#R];W?RW4T[?]=Y#R];W?RW4T[?]=OfK]r/NRVRa4:\\[nrT]X>HGnFl\\Pr;Z`qrr;lqrU\\Ya9%bI[jVpMC\n" );
buf.append(                ",!t-06/<YGh'>Dgq>L6kqsEIFs8Dfos7ZKmqtB*Jp&FS2^IBE\\4WqR3,eQ2W`Bc#`6M1<OrVHQos82W_q>C3ks7Q9hq>^Kls8Vurs82irqu?]ns8Vurs82irqu?]ns8Vurs82irqu?]ns8Vurs82irqu?]ns8Vurs82irqu?]ns8Vurs82irqu?]ns8Vrqs8)cqp&>!gs8Vurs7lWop](9fs8Vlos82irqu?]hrr;ips8)cqqu?]ns8Vurs82irqu?]ns8Vurs82irqu?]ns8Vurs82irqu?]ns8Vurs82irqu?]ns8Vurs82irqu?]n\n" );
buf.append(                "s8Vurs82irqu?]ls8Vurs7cQnqZ$Tls8Vins82irq>^Kls8Vrqs8)cqp&>!gs8Vurs7lWop](9hs8Vops7u]pq>^Kjs8Vops7u]pq>^Kjs8Vops7u]pq>^Kjs8Vops7u]pq>^Kks8Vrqs7cQnq#CBjrq,dQs8W,us8W,ts8N&qs8DQA+A1<48*([Uh]bDD+[Op,4P_8Ns7uWks823EX8i.ps8Vfms82NNVY0u[.)/4q]fdLS80ki0)T=up\\46,jp@e+Us8Vrqqu?Tos829Rs7lWorVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rr\n" );
buf.append(                "s8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8Drkp%J7Wq>^Hor;-<hqu#gGli6tas8N&urr<#ts6T%:rVQNjqYU<ls8)Ndp\\\"7[rVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVl`kqu?Zqrqu`or;HZps8;`js8Drkp%J7Wq>^Hor;-<hqu#gGli6tas8N&urr<#ts8W)us8N&urr<#ts8W)us8N&urr<#ts8W)us8N&urr<#ts8W)us8N&u\n" );
buf.append(                "rr<#ts8W)us8N&us8Dorr;Q`pp\\FO_rVulsqu?WdfH<f>,Y-r86/NkQhB>2@+?\\I*70!)]r;ZZ]jK&=ZqZ$Ths8VuijJMVMngM7B,/$,^_+5ia3uZ((0\"+7?s7QEis8W,qs8Du[m-5Z-'*/(@!!3-#!WW3%!!!'#!!3-#!WW3%!!!'#!!3-#!WW3%!!!'#!!3-#!WW3%!!!'#!!3-#!WW3%!!!'#!!3-#!WW3%!!!'#\"U>/:$O?_5!!!$\"!!*'6(),oskPkM[rr;r\\lK8is'*&%5!!*'\"!<<E6#R(G9!!3-#!WW3%!!!'#!!3-#!WW3%\n" );
buf.append(                "!!!'#!!3-#!WW3%!!!'#!!3-#!WW3%!!!'#!!3-#!WW3%!!!'#!!3-#!WW3%!!!'#!sJ`+!W`6\"rVZZlr;QTms8;p!!!<B+\"U>/:$O?_5!!!$\"!!*'6(),oskPkM[rr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrr;rrs8DrsrVliqrVZNlrqu`nqtg0arVcWgqY0sgr;Q`ps8Duss82ipo@P0Xfd')L)aW@*6J`kOh&ni9,Xh\\.rVZ]mmGt>JrVHQopAb0ip>i+Ds7.SQ7O#N/+MgSo]K7%N5qf];p&FgerVufop](6m\n" );
buf.append(                "q>^C!!<<0$$NL2-!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!<W<%!<<3*!WW6$!!**!s8Dcjq\"ag^rr;os!<<*#!!<B+!!*'#!rr<%!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"\"U>2*qY1!hrVliqq\"j^g#6Y#-!!!$\"!<W<%!<<3*!WW6$!!**!s8Dcjq#(-gr;QTk\n" );
buf.append(                "rqlWmqu$Hjr;QTkrqlWmqu$Hjr;QTkrqlWmqu$Hjr;QTkrqlWmqu?]ns8Vurs8Dilq>UBmrr2oqrVccrs8W&trr;lqrU\\Ya9%bI[jVpMC,!t-06/<YGh'>Dgq>L6kqsEIFs8Dfos7ZKmqtB*Jp&FS2^IBE\\4WqR3,eQ2W`Bc)`7J6]Ur;$Bls7u]nrqQNk\"98E%!!`K)!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!'$!X&T*!<<-&!WW6$rr<#ts8W)u\n" );
buf.append(                "s8N&u!!*'#!s/H'!!39*!!30$!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"!!*'\"!<<*#!!!$\"r;QZns8Musrquco!!*'\"!<<*#!!!'$!X&T*!<<-&!WW6$rr<#ts8W)us8N&urr<#ts8W)us8N&urr<#ts8W)us8N&urr<#ts8W)us8N&urr<#ts8W&sq>UEjrVZNhq>1-js8W)uqt^0irVuors8N&qs8DQA+A1<48*([Uh]bDD+[Op,4P_8Ns7uWks823EX8i.p\n" );
buf.append(                "s8Vfms82NNVY0u[.)/4q]fdLS80ki0)T=ul\\ju;jq#:6lrr)WlrVu]nqZQs\"!!!<*!!!*'!s&E'!!!!%\"9\\])!!ii9zzzzzzzzzzzzz!!!'%!WW3#!WiE'z!<<*!rVc]ps8W-!rrE-$z!!!$#!<<*\"!<E3$!!!91#QOi)!!!!$!s8N'!!**$zzz!<E3%!<E3%!<<*\"zzzzzz!!!!\"!<E0#!<<*!s8W,r\n" );
buf.append(                "qtpEnrr<$!!WiE(!<E0#!!**$z!!!$#!WW3\"s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rVulsqu?WdfH<f>,Y-r86/NkQhB>2@+?\\I*70!)]r;ZZ]jK&=ZqZ$Ths8VuijJMVMngM7B,/$,^_+5ia3uZ((._8.?s7lTls8Muls8Duns8)s\"!!!!*!WiE)!Wi?%z#6tJ6z!!<<*!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%\n" );
buf.append(                "!<E3%!<E3%!<E3%!<E3%!!!!'\"pPA?#m183\"pP87\"9eDjp&G'ls8W-!s8N*#!<<*\"!!!!\"!<E0#!!**$!WiE'!!!*'!sA`0!s8W3#mgb=\"TSN&!WiE)!WiB'!<<*\"zzz!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!!!!*#mpP-r;?Kjr;?Kdp@\\Id!!**$!<E3$!!!$#!<<*\"!!!!\"!<N6#s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8Duss82ipo@P0Xfd')L)aW@*6J`kOh&ni9,Xh\\.rVZ]mmGt>JrVHQopAb0ip>i+Ds7.SQ7O#N/+MgSo]K7%N5q9H9nc/C`rVulqq#C<mq>^<qz#ljr*z!!!$#!<WE+!!!!$!s8T+!W`<&!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<N<(zz!!<<*#6tM-p@eLbrr<#us8W*\"!<E0#z!<E3$!!!$#!<<*\"!!!!'\"pP)/!<<*\"z!!!*'!s&E'!<E3&!Wi?%z!!!!\"\n" );
buf.append(                "!<E<+\"9AN(!<E3%!<E3%!<E3%!<E3%!<E3%!<N<(#RC_3q>($hrr<#us8W*%\"9\\])!!**$!!!!\"!<E0#z!<E6$s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W&trr;lqrU\\Ya9%bI[jVpMC,!t-06/<YGh'>Dgq>L6kqsEIFs8Dfos7ZKmqtB*Jp&FS2^IBE\\4WqR3,eQ2W`Bbub8+HWSrr)lrrV6Eks7u]l\"TSN&!!rW*!!<<*#mgn>!Wi?%\n" );
buf.append(                "!!``6!s8W*zzzzzzzzzzzzz!!EE-!WiE-\"pP&-zs8W-!s8W-!s8W-!!<E3$z!!**$!!!!\"!<EB/\"p>&3z!!!6/#6Fr.!WiE'zz!<E3%!<E0#zzzzzzzzzzs8W-!s8Vifp\\Xjb!!!!$!s8Q)!<<*\"!<E3$z!!**$s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W,ts8N&qs8DQA+A1<48*([Uh]bDD+[Op,4P_8Ns7uWks823EX8i.ps8Vfms82NNVY0u[.)/4q]fdLS80ki0)T=ul\\ju;jq#:6lrr)WlrVu]nqZQs\"!!!<0\"pP&-z!!!!*#mh\"K$ig8-'bqGc!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W1#6tG;\"onW'!<E3'!s95O'^tMZqtp9js8W-!rrE-$z!!!$#!<<*\"!<E3$\n" );
buf.append(                "z!\"onW!<E3&!Wi?%!!EE-#RC\\:!<E9)!sA`0!s8W+!<E0#z!<E3'!s8W-!s8W-!s8W-!s8W-!s8W-!s8W1#6thQ&ao)Ts8W-!s8W-!rr<$!!!!!\"!<E0#!!**$z!!!$#!WW3\"s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rVulsqu?WdfH<f>,Y-r86/NkQhB>2@+?\\I*70!)]r;ZZ]jK&=ZqZ$Ths8VuijJMVMngM7B,/$,^_+5ia\n" );
buf.append(                "3uZ((._8.?s7lTls8Muls8Duns8)s\"!!!!*!!!!%\"9\\i1\"98E%z!!!WE''86:r;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjq\"XXXp@eOds8N#ss8W,cm-OcPs8Dops8W-!s8N*#!<<*\"!!!!\"!<E0#!!**$!!!!7()H3#lMpnas8W-!s8Vcbp%eFZrVc]mr;?Hhqtp9fr;?KkrVc`prr<#ur;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?KjrVc]XkNDmFs7uKdr;?Kfq\"P+##Qk/2!<E3$!!!$#!<<*\"\n" );
buf.append(                "!!!!\"!<N6#s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8Duss82ipo@P0Xfd')L)aW@*6J`kOh&ni9,Xh\\.rVZ]mmGt>JrVHQopAb0ip>i+Ds7.SQ7O#N/+MgSo]K7%N5q9H9nc/C`rVulqq#C<mq>^<qz#mU\\?!WiE'z!!``6!!!!\"!<N6#s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8N#srVc]p\n" );
buf.append(                "s8Vojq>^Kps8W,qqYL6ls8W-!s8W*\"!<E0#z!<E3$!!!$#!<`N.!!!#trVc`prqcKfs8W,rqtpEns8W-!s8W-!s8W-!s8W-!s8W,sr;?Bdq>^Kps8W-!s8W-!s8W-!s8W-!s8W-!s82]js8W-!s8W-!s8W-!q\"XUf\"U+l+!!**$!!!!\"!<E0#z!<E6$s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W&trr;lqrU\\Ya9%bI[jVpMC,!t-0\n" );
buf.append(                "6/<YGh'>Dgq>L6kqsEIFs8Dfos7ZKmqtB*Jp&FS2^IBE\\4WqR3,eQ2W`Bbub8+HWSrr)lrrV6Eks7u]l\"TSN&!!rZ,!<<*\"#RC\\9zz\"pP;0r;?Tps8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rVc]ps8W-!s8W-!rVc]ps8W-!s8W-!!<E3$z!!**$!!!!\"!<E3%!<E3%s8W,trVccrs8Dops8W-!s8W)trr2ors8W-!s8W&rrUfaSkND$ah;/,2s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W)trr<#uqYL'gs8W)trr<#u!<E3$!!!$#!<<*\"!<E3$z!!**$s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W,ts8N&qs8DQA+A1<48*([Uh]bDD+[Op,4P_8Ns7uWks823EX8i.ps8Vfms82NNVY0u[.)/4q]fdLS80ki0)T=ul\\ju;jq#:6lrr)WlrVu]nqZQs\"!!!<*!!!$#!<E3%!!!!\"!<E0#!!33'rVc]ps8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rrE-$z!!!$#!<<*\"!<E3%!<E0#!<2uss8W,trVc`prr<#ur;?Kms8W-!s8W-!s8W,pq>'pbqi[,*M2@)dqtpEns8W-!s8W,rqtpBlrr<#ur;?KkrVc`prr2ors8W-!s8W)trW*$#!<E3%!<E0#!!**$z!!!$#!WW3\"s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!rVulsqu?WdfH<f>,Y-r86/NkQhB>2@+?\\I*70!)]r;ZZ]jK&=ZqZ$Ths8VuijJMVMngM7B,/$,^_+5ia3uZ((._8.?s7lTls8Muls8Duns8)s\"!!!!*!!!!\"!<E3%!<<*\"!<E3$!!!'%!r`)rs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8N*#!<<*\"!!!!\"!<E0#!!**$!!!!%\"9ec)s8W-!s8W-!s8W-!s8N#s\n" );
buf.append(                "s8W-!s8Vlhq\"sscs8W,Gd*Q`NE+39chVR,hs8Vunqu?]rs8W,sr;?Tps8DoprVc]orr2orrr<#us8W,urr)p!!<E3%!<E3$!!!$#!<<*\"!!!!\"!<N6#s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8Duss82ipo@P0Xfd')L)aW@*6J`kOh&ni9,Xh\\.rVZ]mmGt>JrVHQopAb0ip>i+Ds7.SQ7O#N/+MgSo]K7%N5q9H9nc/C`rVulqq#C<mq>^<q\n" );
buf.append(                "z#ljr*!<E3%!<E0#!!**$!!!!#!Wr?!rVuots8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W*\"!<E0#z!<E3$!!!$#!<N<(\"9\\l,rr2lprVc]nrr2orrr2lprV?9bs8W-!s8W-!s7uKdN/W^.B4ksoC6XsInaZ/Ls8Vunqu?]rrr2oss8Vunqu-Klrr2orrr2rts8W-!rr2lt!<E3%!<E3%!!!!\"!<E0#z!<E6$s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W&trr;lqrU\\Ya9%bI[jVpMC,!t-06/<YGh'>Dgq>L6kqsEIFs8Dfos7ZKmqtB*Jp&FS2^IBE\\4WqR3,eQ2W`Bbub8+HWSrr)lrrV6Eks7u]l\"TSN&!!rW*!!**$!<E3$!!!$#!<<*\"!WiH%rVccrs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "!<E3$z!!**$!!!!\"!<E0#zr;?Kms8W#pr;?Kjs8W,qqYL6ls7uKds8W,trVb3odqii:BP;'`>?bfUC\"Ltgs8W,sr;?9^pA=aas8W-!s8W&rrVlfqrr2oss8W-!s8N#s!<E3%!<E3%!<<*\"!<E3$z!!**$s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W,ts8N&qs8DQA+A1<48*([Uh]bDD+[Op,4P_8Ns7uWks823EX8i.ps8Vfm\n" );
buf.append(                "s82NNVY0u[.)/4q]fdLS80ki0)T=ul\\ju;jq#:6lrr)WlrVu]nqZQs\"!!!<*!!!$#!<E3%!!!!\"!<E0#!!33'rVc]ps8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rrE-$z!!!$#!<<*\"!<E3(\"9\\c-!rVuos8W-!s8W-!s8N#ss8W-!s8W#pr;ZfsmdBM4S\"!_j?>F=lD/F,r?='.lSEfD)s8W-!s8W-!s8)Tgqtp9hrVc`prr2ors8W-!s8W)t\n" );
buf.append(                "rW*$#!<E3%!<E0#!!**$z!!!$#!WW3\"s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rVulsqu?WdfH<f>,Y-r86/NkQhB>2@+?\\I*70!)]r;ZZ]jK&=ZqZ$Ths8VuijJMVMngM7B,/$,^_+5ia3uZ((._8.?s7lTls8Muls8Duns8)s\"!!!!*!!!!\"!<E3%!<<*\"!<E3$!!!'%!r`)rs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8N*#!<<*\"!!!!\"!<E0#!!**$!!!!&\"U4_tq=sa[s8W,lp%?b]d`0;HXK8LkR@/\\[K788u@:<SWB4kacA7B\"WCMR]l?=&#,I\"mB1PEV3:Za7E]^XguerVc]orr2orrr<#us8W,urr)p!!<E3%!<E3$!!!$#!<<*\"!!!!\"!<N6#s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8Dus\n" );
buf.append(                "s82ipo@P0Xfd')L)aW@*6J`kOh&ni9,Xh\\.rVZ]mmGt>JrVHQopAb0ip>i+Ds7.SQ7O#N/+MgSo]K7%N5q9H9nc/C`rVulqq#C<mq>^<qz#ljr*!<E3%!<E0#!!**$!!!!#!Wr?!rVuots8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W*\"!<E0#z!<E3$!!!$#!<<*\"$47.?s8W-!s8W-!k2tg!TV,b'@VfjsCi!ouA7T4]A7/eQBk_9k@q0.a\n" );
buf.append(                "B520r?XI/SBk_3mB4tslBk_9qBk`K`MuEVQrr2orrr2rts8W-!rr2lt!<E3%!<E3%!!!!\"!<E0#z!<E6$s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W&trr;lqrU\\Ya9%bI[jVpMC,!t-06/<YGh'>Dgq>L6kqsEIFs8Dfos7ZKmqtB*Jp&FS2^IBE\\4WqR3,eQ2W`Bbub8+HWSrr)lrrV6Eks7u]l\"TSN&!!rW*!!**$!<E3$!!!$#!<<*\"\n" );
buf.append(                "!WiH%rVccrs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!!<E3$z!!**$!!!!\"!<E6'!WW3#qYL'dr;?Tps8W-!iSiaOLko_XBObO^@U`eU@Ua\"aBP(jgB4kje@q0(]ARo=^@:<SVAnGakBjY4SMi3M]mdC&PrVlfqrr2oss8W-!s8N#s!<E3%!<E3%!<<*\"!<E3$z!!**$s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W,ts8N&qs8DQA+A1<48*([Uh]bDD+[Op,4P_8Ns7uWks823EX8i.ps8Vfms82NNVY0u[.)/4q]fdLS80ki0)T=ul\\ju;jq#:6lrr)WlrVu]nqZQs\"!!!<+!<E0#!!33'zzzs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W&rrVuotqtp9js8W-!s7Z0[s8W-!s8W-!rrE-$z!!!$#!<<*\"!<E3$z\n" );
buf.append(                "!<2uss8W-!s8W-!s8W-!rr2nPLPK5D?Y3n^AS#FaAS#RiBk1abAnGXdAS#RiBk:jeC2.Ke>$@2VQ0dl(s8W,urr2rts8W-!s8W-!s8W)trVuouz!!!-)\"98E%!s8W*z!<<*!rr2oss8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W&rrVuotqtp9js8W-!s7Z0[s8W-!rVulsqu?WdfH<f>,Y-r86/NkQhB>2@+?\\I*70!)]r;ZZ]jK&=ZqZ$Ths8VuijJMVMngM7B,/$,^_+5ia3uZ((\n" );
buf.append(                "._8.?s7lTls8Muls8Duns8)s\"!!!!*z!!!91#QOi)!!!!%\"9\\l3\"o7uirr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2ors8W,qqYKpZpA=aaqYL'dr;?Tps8Dops8W-!s8N*#!<<*\"!!!!\"!<E0#!!**$z!!*#trr<#us8W-!s8W-!s8N#sl07GsPa#lrBkhBt?!U`HA7TRqDI?j[BP;'f@:<_YAR]+XR$a9\"iSjh:s8W-!rr2oss8W-!s8W-!s8W,urr)ltz!!!!'\"pP>=#Qb&/\"U,&4\n" );
buf.append(                "\"9eAho`+skrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2ors8W,qqYKpZpA=aaqYL'dr;?Tps8Dops8Duss82ipo@P0Xfd')L)aW@*6J`kOh&ni9,Xh\\.rVZ]mmGt>JrVHQopAb0ip>i+Ds7.SQ7O#N/+MgSo]K7%N5q9H9nc/C`rVulqq#C<mq>^<qz#m(/0!!!!&\"U+l+!!**$!!!!#!Wr2nq>UBmrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrqu]ls8W-!s8W-!\n" );
buf.append(                "s8Dopq>'jds8W-!s8W-!s8W*\"!<E0#z!<E3$!!!$#!<<*\"!!!#urr2rts8W-!s8W-!s8W)trr<#ul07GXH$N_&?>=4iB4kjc@:<qeCM@Ks@:<SZC2.s:GN\\/Ps8W,urr2rts8N#ss8W-!s8W-!s8W-!rr2lsz!!**$zz!!33'#6tM-p@eI`rVlfqrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrr2orrqu]ls8W-!s8W-!s8Dopq>'jds8W-!s8W&trr;lqrU\\Ya9%bI[jVpMC,!t-06/<YG\n" );
buf.append(                "h'>Dgq>L6kqsEIFs8Dfos7ZKmqtB*Jp&FS2^IBE\\4WqR3,eQ2W`Bbub8+HWSrr)lrrV6Eks7u]l\"TSN&!!rf4\"TSN&z!!!?5$31&+!s8Z(rVccrs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8;fmrVc]iq\"Xmhs8W-!qYL'aq>('js8W-!!<E3$z!!**$!!!!\"!<E0#zrr2oss8W-!s8W-!s8W,urr2flqtp9fI=6NTAS#XmCM.9mAnGXa@U`_Q?tEn\\@q0\"pH@#Vks8;fmqtp9f\n" );
buf.append(                "qtpBlrr<#us8W-!s8W-!s8N#szz!!NN0!s8W1#6t5/zs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8;fmrVc]iq\"Xmhs8W-!qYL'aq>('hs8N&qs8DQA+A1<48*([Uh]bDD+[Op,4P_8Ns7uWks823EX8i.ps8Vfms82NNVY0u[.)/4q]fdLS80ki0)T=ul\\ju;jq#:6lrr)WlrVu]nqZQs\"!!!<*!!!$#!=B/@!<E3$z!#ZXlk2th(r;?Kjr;?Kjr;?Kjr;?Kj\n" );
buf.append(                "r;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kep\\4O\\qZ$Tqr;?KkrVc*Lm/R+cs8W-!s8W-!rrE-$z!!!$#!<<*\"!<E3$z!<2uss8W-!s8W-!s8W-!rr2oss8VBLl=FO<?smAVBk_!a@:j+`@U`eZB4l!qCLgsdBk_;KmHsoPrr<#urVc]orr2rts8W-!s8W-!s8W)trVuou!!!!%\"9\\f/!rr<$z!!!TC&a\\lNq>'jar;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kep\\4O\\qZ$Tq\n" );
buf.append(                "r;?KkrVc*Lm/R+cs8W-!rVulsqu?WdfH<f>,Y-r86/NkQhB>2@+?\\I*70!)]r;ZZ]jK&=ZqZ$Ths8VuijJMVMngM7B,/$,^_+5ia3uZ((._8.?s7lTls8Muls8Duns8)s\"!!!!*#mgn<zz!s8W0\"pP&-!#HFf\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"pP8=$46b5z!!!!5'GTcpkl:\\_s8W-!s8N*#!<<*\"!!!!\"!<E0#!!**$z!!*#trr<#us8W-!s8W-!s8N#sr;?KJ\n" );
buf.append(                "gtZpjEb&]kAS#F]@:=\"iD/F-,C2.Ke>$>NMAnbsnf@SXSs8W&rrVuotrr2oss8W-!s8W-!s8W,urr)ltzz!!!3-\"p+i-#RC\\9!!!fO(BOX>\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"9\\i1\"pP8=$46b5z!!!!5'GTcpkl:\\_s8Duss82ipo@P0Xfd')L)aW@*6J`kOh&ni9,Xh\\.rVZ]mmGt>JrVHQopAb0ip>i+Ds7.SQ7O#N/+MgSo]K7%N5q9H9nc/C`rVulqq#C<mq>^<qz\n" );
buf.append(                "#ljr*!!!!%\"9\\])z#6tJ:\"9\\])zzzzzzzzzzzzz!s8W*!!!0+\"TSN&!<E6$s8Vunqu?]rs8W*\"!<E0#z!<E3$!!!$#!<<*\"!!!#urr2rts8W-!s8W-!s8W)trr<#uZEgcQD/E`k@U`eUQ^=&jf@S?pc]XHSDf9Q*AS#XmCV7<mr;?Kep\\4^fs8N#ss8W-!s8W-!s8W-!rr2lszz!!!!'\"pP&-!!**$z!!!-)\"98E%zzz\n" );
buf.append(                "zzzzzzzzz!s8W*!!!0+\"TSN&!<E6$s8Vunqu?Wprr;lqrU\\Ya9%bI[jVpMC,!t-06/<YGh'>Dgq>L6kqsEIFs8Dfos7ZKmqtB*Jp&FS2^IBE\\4WqR3,eQ2W`Bbub8+HWSrr)lrrV6Eks7u]l\"TSN&!!rW*!!WW3z!!!0+\"TSN&zzzzzzzzzzzzzzz!WiE'!!!$#!<<*\"rVc]nrVccrs8W-!!<E3$\n" );
buf.append(                "z!!**$!!!!\"!<E0#zrr2oss8W-!s8W-!s8W,urr2lprM--_AS#G$I=98\\b4bQ&s8W,urr2ZdpWCY$H?smMB4n';V>9r_s8W,sr;?Qnrr<#us8W-!s8W-!s8N#sz!!!'%!WiE)zz!!NN0!!!!\"!<E0#zzzzzzzzzzzzz!WiE'!!!$#!<<*\"rVc]nrVccps8N&qs8DQA+A1<48*([Uh]bDD+[Op,4P_8Ns7uWks823EX8i.ps8Vfms82NN\n" );
buf.append(                "VY0u[.)/4q]fdLS80ki0)T=uo]h7_mp&G$kr;63frVucpr<**#!!!<*zzzzzzzzzzzzzzzzzz!!!'%!WW3#!WiE'z!<<*!rVc]nrVccrrr<$!!!!!#!Wi?%!!33'!!!!\"!<E0#!<<*!s8W,sr;?Tps8;fms8W,np\\281]#;P*qYL'gs8Vojq>^Kps8W,urr2rts7uKd`5KU'\\$u,+p](9ns8W-!s8W#pr;Zfsr;?Kms8W-!rr<$!\n" );
buf.append(                "!<E3$zzzzzzzzzzzzzzzzzz!!!'%!WW3#!WiE'z!<<*!rVc]prVulsqu?WdfH<f>,Y-r86/NkQhB>2@+?\\I*70!)]r;ZZ]jK&=ZqZ$Ths8VuijJMVMngM7B,/$,^_+5ia3uZ((/A+R@s7>sWrVlcjs7uQdrV7!4\"TSN/!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%\n" );
buf.append(                "!<E3%!<E3%!!!!'\"pPA?#m183\"pP87\"9eDjp&G'ls8W,lp%8Fj\"9o&7!s8W3#mge?\"onW'!!!!,$Ocn1r;-9drVc]hp\\4O\\qZ$Tqs8W,hnaZ;Fp&G'ls8W,urr2rts8Doprr2orrr2rts8)Tgn*f`Fs8W)trr<#us8W,qqYKs\\p\\k'hqYL'dr;6p1$NL/,!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!!!!'\"pPA?#m183\"pP87\"9eDjp&G'ls8Duss82ip\n" );
buf.append(                "o@P0Xfd')L)aW@*6J`kOh&ni9,Xh\\.rVZ]mmGt>JrVHQopAb0ip>i+Ds7.SQ7O#N/+MgSo]K7%N5q0?6nc/Carr<#uqZ$Kmo_S7k#m1/-#lt&-!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<N<(zz!!<<*#6tM-p@eLbrr2orp@e1b#6t>5!rr<$z!!!'%!WiE)#mgq5q\"XgdrVuots8W-!s8W)trqlTis8W-!s8W&rrVQKhs8W-!s8W&rrVuot\n" );
buf.append(                "q>'jcrr2rts8W-!s8W,qqYL6ls8N#ss8W-!s8W-!s8Dopq\"XUj#mgY7!W`<&!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<E3%!<N<(zz!!<<*#6tM-p@eLbrr;rsrr;lqrU\\Ya9%bI[jVpMC,!t-06/<YGh'>Dgq>L6kqsEIFs8Dfos7ZKmqtB*Jp&FS2^IBE\\4WqR3,eQ2W`Bc,j9C2]Mqtg?fp[\\@_s82ip\"98E%!!rW*zzzzz\n" );
buf.append(                "zzzzzzzzzzzzz!!EE-!WiE-\"pP&-zs8W-!s8W-!s8W-!z!!!3-\"p+i-\"9\\i-zzs8W-!s8Vlhq#('fqYL'gs8W&rrVuots8W,trVccrs8N#sr;?Kms8W-!s8N#sr;?Kms8W-!s8W-!rr2oqrVccrs8)Tgr;?Kfq\"Xmhs8W-!zzzzzzzzzzzzzzzzz\n" );
buf.append(                "zzz!!EE-!WiE-\"pP&-zs8W-!s8W,ts8N&qs8DQA+A1<48*([Uh]bDD+[Op,4P_8Ns7uWks823EX8i.ps8Vfms82NNVY0u[.)/4q]fdLS80ki0)T=uh[R9N`pAb-ls8VrqrVu-NlPL9E!X0&9!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W1#6tG;\"onW'!<E3'!s95O'^tMZqtp9fqtojNmh?HM!s8W+!<E0#!!WW3#6tJ=#6tnU'CP;V\n" );
buf.append(                "s8W-!s8W-!s8;fmqtp9js8W&rrVc]ns8W-!s8W)trr)fos8W,trVccrs8W-!r;?Kms8W-!s8W-!qtp9fqtp<hr;Zfss8W-!s8VNTmLp6I#6tJ9!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W1#6tG;\"onW'!<E3'!s95O'^tMZqtp9jrVulsqu?WdfH<f>,Y-r86/NkQhB>2@+?\\I*70!)]r;ZZ]jK&=ZqZ$Ths8VuijJMVMngM7B,/$,^_+5ia3uZ((0\"t!K\n" );
buf.append(                "s7lEbs8Dlir:]p_s8D?Kiq`NPr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjq\"XXXp@eOds8N#ss8W,cm-OcPs8DoprVc]ps8VHPm/R+crr2oss8VfdpA\"FXrVc]Ykii'Hs7lBarVc]jq>('js8W-!qtp9dq>(!frVuotrVc]nrVccrs8W-!s8W-!s8W&rrV-'\\rr2oqrVccrs8W-!s8W-!s8Vojq>L9jq\"XXas8V<Hkl(JYr;?Kjr;?Kjr;?Kjr;?Kjr;?Kj\n" );
buf.append(                "r;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjr;?Kjq\"XXXp@eOds8N#ss8W,cm-OcPs8Dops8Duss82ipo@P0Xfd')L)aW@*6J`kOh&ni9,Xh\\.rVZ]mmGt>JrVHQopAb0ip>i+Ds7.SQ7O#N/+MgSo]K7%N5qBQ<nc/C[q#CBoqZ$Noqu?WprqQ6as8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8N#srVc]ps8Vojq>^Kp\n" );
buf.append(                "s8W,qqYL6ls8W-!qYL'gs8W-!s7uKds8W,trVc`prqlTis8W-!s8W)trr<#uq>'jds8W-!s8W-!s8W-!s8W-!s8N#ss8W-!s8W-!s8W-!rr2oss8W-!s8W-!s8W,trVccrs8W-!s8W,pq>('js8N#ss8W-!s8Vunqu?]rs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8N#srVc]ps8Vojq>^Kps8W,qqYL6ls8W&trr;lqrU\\Ya9%bI[jVpMC,!t-06/<YGh'>Dg\n" );
buf.append(                "q>L6kqsEIFs8Dfos7ZKmqtB*Jp&FS2^IBE\\4WqR3,eQ2W`Bbr`7e$HPs8N&us8)cos7Q9`s8Mfhr;Zfss8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rVc]ps8W-!s8W-!rVc]ps8W-!s8Dops8W-!s8W-!s8Dops8W-!s8W)trr<#uqYL'gs8W-!s8W-!rr2orrr2inr;Zfsqtp9dq>('js8W-!qtp9irr2lprVZTks8W,oq\"Xmhs8N#sqYL'gs8W)t\n" );
buf.append(                "rr2ors8W-!s8W-!s8)Tgs8W,urr2rts8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rVc]ps8W-!s8W-!rVc]ps8W,ts8N&qs8DQA+A1<48*([Uh]bDD+[Op,4P_8Ns7uWks823EX8i.ps8Vfms82NNVY0u[.)/4q]fdLS80ki0)T=uc^/On.rr2ops8)Bbs8VBas8V`@s75par;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Ki\n" );
buf.append(                "s8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nr;-HkqZ$Kis8;`nqXOC]pAapfs8N&u\n" );
buf.append(                "r;$Bfg[bCEo)JairVulsqu?WdfH<f>,Y-r86/NkQhB>2@+?\\I*70!)]r;ZZ]jK&=ZqZ$Ths8VuijJMVMngM7B,/$,^_+5ia3uZ((+2pJ>s7cQms8Vrqs8)Tgo@Mom'$c(/f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y\n" );
buf.append(                "!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)f7a4VK`K_Y!7uo)l$WZfLB-@c!7HN#b_#rMQR2[kiW&WOs8Duss82ipo@P0Xfd')L)aW@*6J`kOh&ni9,Xh\\.rVZ]mmGt>JrVHQopAb0ip>i+Ds7.SQ7O#N/+MgSo]K7%N5q0W;qZ$Khr;ZWhs8W&tqYTU+O:^#p#2aq<\n" );
buf.append(                "hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$K$@@hgc'k\n" );
buf.append(                "J-t1a$K$@@hgc'kJ-t1a$K$@@hgc'kJ-t1a$J9k4c\\MS^K)s\\^!7Z`)d>f+EnEBoNs8W&trr;lqrU\\Ya9%bI[jVpMC,!t-06/<YGh'>Dgq>L6kqsEIFs8Dfos7ZKmqtB*Jp&FS2^IBE\\4WqR3,eQ2W`Bbl`1&h4?p](*bs7Z9gpAY*Wc\\W1oKbrEp!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW\n" );
buf.append(                "!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jSW!82u)fn0:XK)jkc):XBLl?X!(ILG(`&(ZFQs7u![qX=I_s8N&qs8DQA+A1<48*([Uh]bDD+[Op,4P_8Ns7uWks823EX8i.ps8Vfms82NNVY0u[\n" );
buf.append(                ".)/4q]fdLS80ki0)T=uk_*IntrV?BkqtC'ip](9drSqr3ok4m\"HiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3Hq\n" );
buf.append(                "HiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HqHiiMZ!pG\"5k^3HpEXU`M!;(F7l@/fgIfRQF$31%ho`+XbrVulsqu?WdfH<f>,Y-r86/NkQhB>2@+?\\I*70!)]r;ZZ]jK&=ZqZ$Ths8VuijJMVMngM7B,/$,^_+5ia3uZ((.(NOZrr;ops8Dors8Vllr8?+f'&/'Ni.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b\n" );
buf.append(                "$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAi.)0mJ-t4b$K-FAh223ZNWA0o!SiJ6f7jsgO!jtum/R%\\s8Duss82ipo@P0X\n" );
buf.append(                "fd')L)aW@*6J`kOh&ni9,Xh\\.rVZ]mmGt>JrVHQopAb0ip>i+Ds7.SQ7O#N/+MgSo]K7%N5psAurVu$Zs7H0fq\"\"IbjT#8[o!SMnf)>X=rVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEd\n" );
buf.append(                "s7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnbrLXrVuEds7-'fnc/LdqZ$Bkp&4pemJlh6s6@Jrr;ZKhs8W&trr;lqrU\\Ya9%bI[jVpMC,!t-06/<YGh'>Dgq>L6kqsEIFs8Dfos7ZKmqtB*Jp&FS2^IBE\\4WqR3,eQ2W`Bb]_6N$l`s8)cqq>^KpoBlS!s8\">Ir$21>\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\n" );
buf.append(                "\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$rVcotrWE,u\"9&3$\n" );
buf.append(                "rVcotrWE3\"%eBW\"qY^EcrrW&s*WQ(*r;Y7Ds6Tdas8N&qs8DQA+A1<48*([Uh]bDD+[Op,4P_8Ns7uWks823EX8i.ps8Vfhs7l!HVs=<N0\"=D(\\3D.M7Ol/5-Ghi!^-(udlMpnJqu?-[s7?*ep\\Omis7l]prrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2ut\n" );
buf.append(                "rrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2utrrE&u!<2uurr2uur;cls!<;ips8N6\"quHHj!WW)ks8Mios8)cnnc/@NhC24d(.-gs5NO.\\ne4@H*^f*:9)nMWs8Di]jL+7Ps8Vcds8D`_lDF+OoIdjM.D.nd_bD;h597L*/@Rn;s8N&p\n" );
buf.append(                "s8Vrqrq6<fp\\t'f!WW-\"rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88\n" );
buf.append(                "rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88rr3E+rs\\o,%0$88r;R!#rrW,p&-)Y=o(isdrtk_5p%\\R]s8W-!r;$Bho[>EiiZq.=.7;318'rJFfdK;B.n9R6p&G$foAR%Is8W,kq>^*_qr+@Fs7.\\O66Ncu)p2\"u`C:if9ddYTmf3=_rVu]lr;Zfor;Z'Urt4u)#QO]%s82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82ls\n" );
buf.append(                "quHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<;rss82lsquHcp!<2j2s8N6%quQit!rW'\"q>VE6rp0U^rU0^cp\\Xgep@Z]T9'[BeiYk&D*^fH66.I#;b:#IPs7Q?j\n" );
buf.append(                "o(:_=q=spgo_S4[qV^_7j8\\Tl`'G9N0HJ2+,.]cY\\Nh^O;>C1ks7ZKmp&G'fs8W)Rqu%Z-q#pQo!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDou\n" );
buf.append(                "qYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrW&s!rDouqYpWnrrVrp\"o/)tq#:?krrVoo$MFENs8:CFqWmSLs8W)rs7u<0.n\\bG7Fr_Feg3B1+?9$88(]4Ss8)9]s7-!LZN'n's7Z?iqYg'GV!e9M1VQ@(ZnioG9-q;5-,Do%\\k2AjlhpeJp]'=PqYU3gs8W,Ss8VKds7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfm\n" );
buf.append(                "s7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8Vfms7ZKmpAb0ds8VfmrV?KmqZ$Bks8Mlor9a@\\\n" );
buf.append(                "g\\_!Mq>^Kos8Dulq#BgLc7V`A*^/=\"3o_MNjX*7b&P1_-9)nPTq#BsPk,S=SqZ$Bes8Df_hk]Z5o16i73OVs=]j`G!:+!JL9u:H*oBZ>Ss7lWmr;HZjs8W)unb)qVrr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#t\n" );
buf.append(                "rr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#trr2rsrr;uss8N#ts8V]jrqQNks8W#ss8;osrV6Ejs8Mrrs8N&uo)&IHj3gG0cRhl@,=pH28_51Ya<k!36t62_o(i=Xm-(MKrVQWmpAF^QrSO[Uo)Ho0f'<5!mf3=ds7cQnqSW?gs7QElq#CBoo`\"X`rr<#uq>^Hls7QElp&G'b\n" );
buf.append(                "s8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcls7QElp&G'bs8Vcl\n" );
buf.append(                "s7QElp&G'bs8Vcls7QElp&G'bs8Vcls7Z9go_SU]rr2Wkq\"Ogaq>^Kjrr;fks6K^br;Q`]s8W,Zo)J.Rs7QElq>^Kos8VTgs466.s8W)uqX*.;s8Dops7H3gnaYV>s7H>]jjBp^i/lRVQJ(8\\hrfI\\b-9\"VUWN5(ht3Mjo:+LqT[_tggteaVkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-Vk\n" );
buf.append(                "UXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&rgZkG-VkUXnV'f&r[Wk+1)dS_<%og#J[Xje18rT\\%kkf&W(Oj.+ZVLZ[Hle_HtWiMG;hRb?V[hW'aXgU5SYk5YJ]rqlECW;lblr;ZEhqXF+Ciqi92s7u-`\n" );
buf.append(                "qXFO`oDe[Vs8DQhrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?j\n" );
buf.append(                "p](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lrV-?jp](3ds8D]lr;-HmpAasQrV5LDmG@m>pAb$iqYS+qmBc9N[d`grm-V.Fm'>a8XQJ]Jkio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5S\n" );
buf.append(                "kio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7l*'=8YNY5Skio;7\n" );
buf.append(                "l*'=8YNY5SjlEl6kd9R<Yj:b`l0PbQrQbHCi;`cUr:^!eqYpNns8W&ts8Dutqu?]ps8W#rs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`o\n" );
buf.append(                "rr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;oqs8;lrr;Q`orr;uos8Vfms7?9jnbr@Rs8Moqrr;dUendstream\n" );
buf.append(                "endobj\n" );
buf.append(                "\n" );
buf.append(                "7 0 obj\n" );
buf.append(                "<<\n" );
buf.append(                "  /Type /Page\n" );
buf.append(                "  /Parent 2 0 R\n" );
buf.append(                "  /Resources <<\n" );
buf.append(                "    /Font <<\n" );
buf.append(                "      /F1 8 0 R\n" );
buf.append(                "    >>\n" );
buf.append(                "    /XObject <<\n" );
buf.append(                "      /img2 10 0 R\n" );
buf.append(                "    >>\n" );
buf.append(                "  >>\n" );
buf.append(                "  /Contents 9 0 R\n" );
buf.append(                ">>\n" );
buf.append(                "endobj\n" );
buf.append(                "\n" );
buf.append(                "8 0 obj\n" );
buf.append(                "<<\n" );
buf.append(                "  /Type /Font\n" );
buf.append(                "  /Subtype /Type1\n" );
buf.append(                "  /BaseFont /Times-Roman\n" );
buf.append(                "  /Encoding /WinAnsiEncoding\n" );
buf.append(                ">>\n" );
buf.append(                "endobj\n" );
buf.append(                "\n" );
buf.append(                "9 0 obj\n" );
buf.append(                "<<\n" );
buf.append(                "  /Length 61\n" );
buf.append(                ">>\n" );
buf.append(                "stream\n" );
buf.append(                "q\n" );
buf.append(                "1 0 0 1 0 0 cm\n" );
buf.append(                "1 0 0 1 0 0 cm\n" );
buf.append(                "135 0 0 75 0 0 cm\n" );
buf.append(                "/img2 Do\n" );
buf.append(                "Q\n" );
buf.append(                "endstream\n" );
buf.append(                "endobj\n" );
buf.append(                "\n" );
buf.append(                "10 0 obj\n" );
buf.append(                "<<\n" );
buf.append(                " /Type /XObject\n" );
buf.append(                " /Subtype /Image\n" );
buf.append(                " /Filter [/ASCII85Decode /FlateDecode]\n" );
buf.append(                " /Width 135\n" );
buf.append(                " /Height 75\n" );
buf.append(                " /BitsPerComponent 8\n" );
buf.append(                " /Interpolate false\n" );
buf.append(                " /ColorSpace /DeviceRGB\n" );
buf.append(                " /Length 33730\n" );
buf.append(                ">>\n" );
buf.append(                "stream\n" );
buf.append(                "GQ@iCG$'V$s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W,ljIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5\n" );
buf.append(                "p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9\n" );
buf.append(                "jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MYCs8W-!s8W-!p#MYCs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s7PHCs8W-!s8W-!s7PHCs8W+eg$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>j\n" );
buf.append(                "RG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kn\n" );
buf.append(                "g$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>js8W-!s8VcQSH&Whs8W-!s8VcQSH&Whs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B\n" );
buf.append(                "+M^5b^d'$X5Tmj-+M^8Bs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!s8W-!s8W*a@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8\n" );
buf.append(                "^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_D-s8W-!s8W-!s8W-!s8V\"\"5i<bM\n" );
buf.append(                "i#k;C+@+j-5i<bMi#k;bs8W-!s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am\n" );
buf.append(                "@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);Cs8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQSH&Whs8W-!s8VcQSH&Wh+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!s$0u-5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8\n" );
buf.append(                "^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C\n" );
buf.append(                "5[_Am@),$8^d);C5[_Am@),$8_#OH7s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b^d'$X5Tmj-+M^8Bs8W-!s8W-!s8W-!s8P>l^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am\n" );
buf.append(                "@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@)2gBs8W-!s8W-!s8W-!s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W*a@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C\n" );
buf.append(                "5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_D-s8W-!s8W-!s8W-!s8V\"\"5i<bMi#k;C+@+j-5i<bMi#k;bs8W-!s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am\n" );
buf.append(                "@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);C5[_Am@),$8^d);Cs8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQ\n" );
buf.append(                "SH&Whs8W-!s8VcQSH&Wh+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b^d'$X5Tmj-+M^8Bs8W-!s8W-!s8W-!s8Tm-^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKY\n" );
buf.append(                "TYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYUU-\n" );
buf.append(                "s8W-!s8W-!s8W-!s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!s8W-!s8W,7TYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY\n" );
buf.append(                "^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nALCs8W-!s8W-!s8W-!s8V\"\"5i<bMi#k;C+@+j-5i<bMi#k;bs8W-!s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!^nAKYTYS@9^qcVY^nAKY\n" );
buf.append(                "TYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9\n" );
buf.append(                "^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVYs8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQSH&Whs8W-!s8VcQSH&Wh+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!s1k;C^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY\n" );
buf.append(                "^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9_#OH7s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b\n" );
buf.append(                "^d'$X5Tmj-+M^8Bs8W-!s8W-!s8W-!s8Tm-^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9\n" );
buf.append(                "^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYUU-s8W-!s8W-!s8W-!s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!s8W-!s8W,7TYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY\n" );
buf.append(                "^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nAKYTYS@9^qcVY^nALCs8W-!s8W-!s8W-!s8V\"\"5i<bMi#k;C\n" );
buf.append(                "+@+j-5i<bMi#k;bs8W-!s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQSH&Whs8W-!s8VcQSH&Wh+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b^d'$X5Tmj-+M^8Bs8W-!s8W-!s8W-!s8N'!zzzzzzzzzzzzzzzzzzzzzzz!!*'!s8W-!s8W-!s8N'!zzzzzzzzz\n" );
buf.append(                "zzzzzzzzzzzzzz!!*'!s8W-!s8W-!s8N'!zzzzz!!*'!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W*!zzzzzzzzzzzzzzzzzzzzzzz!!!$!s8W-!s8W-!s8W*!zzzzzzzzzzzzzzzzzzzzzzz!!!$!s8W-!s8W-!s8W*!zzzzz!!!$!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8V\"\"5i<bMi#k;C+@+j-5i<bMi#k;bs8W-!s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!zzzzzzzzzzzzzzzzzzzzzzzzs8W-!s8W-!\n" );
buf.append(                "s8W-!zzzzzzzzzzzzzzzzzzzzzzzzs8W-!s8W-!s8W-!zzzzzzs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQSH&Wh\n" );
buf.append(                "s8W-!s8VcQSH&Wh+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!rr<$!zzzzzzzzzzzzzzzzzzzzzzz!<<*!s8W-!s8W-!rr<$!zzzzzzzzzzzzzzzzzzzzzzz!<<*!s8W-!\n" );
buf.append(                "s8W-!rr<$!zzzzz!<<*!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b^d'$X5Tmj-+M^8Bs8W-!s8W-!s8W-!s8N'!zzzzzzzzzzzzzzz\n" );
buf.append(                "zzzzzzzz!!*'!s8W-!s8W-!s8N'!zzzzzzzzzzzzzzzzzzzzzzz!!*'!s8W-!s8W-!s8N'!zzzzz!!*'!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!s8W-!s8W*!zzzzzzzzzzzzzzzzzzzzzzz!!!$!s8W-!s8W-!s8W*!zzzzzzzzzzzzzzz\n" );
buf.append(                "zzzzzzzz!!!$!s8W-!s8W-!s8W*!zzzzz!!!$!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8V\"\"5i<bMi#k;C+@+j-5i<bMi#k;bs8W-!s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!zzzzz\n" );
buf.append(                "zzzzzzzzzzzzzzzzzzzs8W-!s8W-!s8W-!zzzzzzzzzzzzzzzzzzzzzzzzs8W-!s8W-!s8W-!zzzzzzs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQSH&Whs8W-!s8VcQSH&Wh+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!rr<$!zzzzzzzzzzzzzzzzzzzzzzz!<<*!s8W-!s8W-!rr<$!zzzz\n" );
buf.append(                "zzzzzzzzzzzzzzzzzzz!<<*!s8W-!s8W-!rr<$!zzzzz!<<*!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b^d'$X\n" );
buf.append(                "5Tmj-+M^8Bs8W-!s8W-!s8W-!s8N'!zzzzzz!<<*!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8N'!zzzzz!!*'!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8N'!zzzz\n" );
buf.append(                "z!!*'!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!s8W-!s8W*!zzzzzz!!*'!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W*!zzzzz!!!$!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W*!zzzzz!!!$!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8V\"\"5i<bMi#k;C+@+j-\n" );
buf.append(                "5i<bMi#k;bs8W-!s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!zzzzzz!!!$!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!zzzzzzs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!zzzzzzs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQSH&Whs8W-!s8VcQSH&Wh+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!rr<$!zzzzzzs8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rr<$!zzzzz!<<*!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rr<$!zzzzz!<<*!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b^d'$X5Tmj-+M^8Bs8W-!s8W-!s8W-!s8N'!zzzzzz!<<*!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8N'!zzzzz!!*'!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8N'!zzzzz!!*'!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!s8W-!s8W*!\n" );
buf.append(                "zzzzzz!!*'!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W*!zzzzz!!!$!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W*!zzzzz!!!$!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8V\"\"5i<bMi#k;C+@+j-5i<bMi#k;bs8W-!s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!zzzzzz!!!$!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "zzzzzzs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!zzzzzzs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQSH&Whs8W-!\n" );
buf.append(                "s8VcQSH&Wh+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!rr<$!zzzzzzs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rr<$!zzzzz!<<*!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "rr<$!zzzzz!<<*!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b^d'$X5Tmj-+M^8Bs8W-!s8W-!s8W-!s8N'!zzzzzz!<<*!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8N'!zzzzz!!*'!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8N'!zzzzz!!*'!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!s8W-!s8W*!zzzzzz!!*'!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W*!zzzzz!!!$!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W*!zzzzz!!!$!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8V\"\"5i<bMi#k;C+@+j-5i<bMi#k;bs8W-!s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!zzzzzz\n" );
buf.append(                "!!!$!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!zzzzzzs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!zzzzzzs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQSH&Whs8W-!s8VcQSH&Wh+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!rr<$!zzzzzzs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rr<$!zzzzz\n" );
buf.append(                "!<<*!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rr<$!zzzzz!<<*!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b^d'$X5Tmj-\n" );
buf.append(                "+M^8Bs8W-!s8W-!s8W-!s8N'!zzzzzz!<<*!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8N'!zzzzz!!*'!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8N'!zzzzz\n" );
buf.append(                "!!*'!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!s8W-!s8W*!zzzzzz!!*'!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W*!zzzzz!!!$!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W*!zzzzz!!!$!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8V\"\"5i<bMi#k;C+@+j-5i<bM\n" );
buf.append(                "i#k;bs8W-!s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!zzzzzz!!!$!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!zzzzzzs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!zzzzzzs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQSH&Whs8W-!s8VcQSH&Wh+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!rr<$!zzzzzzs8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rr<$!zzzzz!<<*!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rr<$!zzzzz!<<*!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b^d'$X5Tmj-+M^8Bs8W-!s8W-!s8W-!s8N'!zzzzzzzzzzzzzzzzzzzzzzz!!*'!s8W-!s8W-!s8N'!zzzzz!!*'!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8N'!zzzzzzzzzzzzzzzzzzzzzzz!!*'!s8W-!s8W-!s8W-!s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!s8W-!s8W*!z\n" );
buf.append(                "zzzzzzzzzzzzzzzzzzzzzz!!!$!s8W-!s8W-!s8W*!zzzzz!!!$!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W*!zzzzzzzzzzz\n" );
buf.append(                "zzzzzzzzzzzz!!!$!s8W-!s8W-!s8W-!s8V\"\"5i<bMi#k;C+@+j-5i<bMi#k;bs8W-!s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!zzzzzzzzzzzzzzzzzzzzzzzzs8W-!s8W-!s8W-!z\n" );
buf.append(                "zzzzzs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!zzzzzzzzzzzzzzzzzzzzzzzzs8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQSH&Whs8W-!s8VcQ\n" );
buf.append(                "SH&Wh+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!rr<$!zzzzzzzzzzzzzzzzzzzzzzz!<<*!s8W-!s8W-!rr<$!zzzzz!<<*!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rr<$!\n" );
buf.append(                "zzzzzzzzzzzzzzzzzzzzzzz!<<*!s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b^d'$X5Tmj-+M^8Bs8W-!s8W-!s8W-!s8N'!zzzzzzzzzzzzzzzzz\n" );
buf.append(                "zzzzzz!!*'!s8W-!s8W-!s8N'!zzzzz!!*'!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8N'!zzzzzzzzzzzzzzzzzzzzzzz!!*'!s8W-!s8W-!s8W-!\n" );
buf.append(                "s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!s8W-!s8W*!zzzzzzzzzzzzzzzzzzzzzzz!!!$!s8W-!s8W-!s8W*!zzzzz!!!$!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W*!zzzzzzzzzzzzzzzzzzzzzzz!!!$!s8W-!s8W-!s8W-!s8V\"\"5i<bMi#k;C+@+j-5i<bMi#k;bs8W-!s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!zzzzzzz\n" );
buf.append(                "zzzzzzzzzzzzzzzzzs8W-!s8W-!s8W-!zzzzzzs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!zzzzzzzzzzzzzzzzz\n" );
buf.append(                "zzzzzzzs8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQSH&Whs8W-!s8VcQSH&Wh+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!rr<$!zzzzzzzzzzzzzzzzzzzzzzz!<<*!s8W-!s8W-!rr<$!zzzzz!<<*!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!rr<$!zzzzzzzzzzzzzzzzzzzzzzz!<<*!s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b^d'$X5Tmj-+M^8B\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8V\"\"5i<bMi#k;C+@+j-5i<bMi#k;b\n" );
buf.append(                "s8W-!s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQSH&Whs8W-!s8VcQSH&Wh+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b^d'$X5Tmj-+M^8Bs8W-!s8W-!s8W-!s8V#-!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX\n" );
buf.append(                "!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,oWLs8W-!s8W-!s8W-!s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!s8W-!s8W,WJ,nMX!8rA-\n" );
buf.append(                "i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gc\n" );
buf.append(                "J,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(h-s8W-!s8W-!s8W-!s8V\"\"5i<bMi#k;C+@+j-5i<bMi#k;bs8W-!s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX\n" );
buf.append(                "!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-s8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQSH&Whs8W-!s8VcQSH&Wh\n" );
buf.append(                "+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!s58D-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gc\n" );
buf.append(                "J,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!<<*!s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b^d'$X5Tmj-+M^8Bs8W-!s8W-!s8W-!s8V#-!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX\n" );
buf.append(                "!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,nMX!8rA-i.(gcJ,oWLs8W-!s8W-!s8W-!s55\"b\n" );
buf.append(                "i#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8V\"\"5i<bMi#k;C+@+j-5i<bMi#k;bs8W-!s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!\n" );
buf.append(                "!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!\n" );
buf.append(                "s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQSH&Whs8W-!s8VcQSH&Wh+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!s8W*!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!\n" );
buf.append(                "rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<*!s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b^d'$X5Tmj-+M^8Bs8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!\n" );
buf.append(                "s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!s8W-!s8W-!s8W-!s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8O4W5Tmj-+M^5b^d'$X5Tmj-s8W-!s8W-!s8W-!s8W-!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!\n" );
buf.append(                "rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!s8W-!s8W-!s8W-!s8V\"\"5i<bMi#k;C+@+j-5i<bMi#k;bs8W-!\n" );
buf.append(                "s7PHCs8W-!s8W-!s7PHCs8W*A^d'$X5Tmj-+M^5b^d'$X5l^las8W-!s8W-!s8W-!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!\n" );
buf.append(                "s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8N*!rrE*!!<<'!s8W-!s8W-!s8W-!s8W,W+@+j-5i<bMi#k;C+@+j-5i<bMs8W-!s8VcQSH&Whs8W-!s8VcQSH&Wh+M^5b^d'$X5Tmj-+M^5b^d.r\"s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!i#k;C+@+j-5i<bMi#k;C+@+j-5l^las8W,ljIH8Ms8W-!s8W,ljIH8Mruf*B+M^5b^d'$X5Tmj-+M^8Bs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s55\"bi#k;C+@+j-5i<bMi#k;C+@,u,s8W-!p#MYCs8W-!s8W-!p#MYCs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s7PHCs8W-!s8W-!s7PHCs8W+eg$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>j\n" );
buf.append(                "RG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>jRG$kng$5!YlC$>js8W-!s8VcQSH&Whs8W-!s8VcQSH&Whs8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W,ljIH8Ms8W-!s8W,ljIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5\n" );
buf.append(                "p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9jIGo(SFts5p#MY9\n" );
buf.append(                "jIGo(SFts5p#MY9jIGo(SFts5p#MYCs8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!\n" );
buf.append(                "s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8W-!s8SpAendstream\n" );
buf.append(                "endobj\n" );
buf.append(                "\n" );
buf.append(                "xref\r\n" );
buf.append(                "0 11\r\n" );
buf.append(                "0000000000 65536 f\r\n" );
buf.append(                "0000000015 00000 n\r\n" );
buf.append(                "0000000069 00000 n\r\n" );
buf.append(                "0000000169 00000 n\r\n" );
buf.append(                "0000000337 00000 n\r\n" );
buf.append(                "0000000445 00000 n\r\n" );
buf.append(                "0000000558 00000 n\r\n" );
buf.append(                "0000037755 00000 n\r\n" );
buf.append(                "0000037924 00000 n\r\n" );
buf.append(                "0000038032 00000 n\r\n" );
buf.append(                "0000038145 00000 n\r\n" );
buf.append(                "trailer\n" );
buf.append(                "<<\n" );
buf.append(                "  /Size 10\n" );
buf.append(                "  /Root 1 0 R\n" );

        return buf.toString();
    }

    String getExpectAfterID() {

        StringBuffer buf = new StringBuffer();

        // buf.append("  /ID [<649FD939DDE9E17D1D51A96D05FC5AEB> <649FD939DDE9E17D1D51A96D05FC5AEB>]\n" );
        buf.append(                ">>\n" );
        buf.append(                "startxref\n" );
        buf.append(                "72091\n" );
        buf.append(                "%%EOF\n");
        return buf.toString();
    }


}